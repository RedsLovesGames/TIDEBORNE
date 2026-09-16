#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

MODE="standard"
SKIP_DEPS="false"
SKIP_QODANA="false"
BASE_OVERRIDE=""

usage() {
    cat <<'EOF'
Tideborne local CI

Usage:
  bash scripts/local_ci.sh [options]

Modes:
  --fast        Dependency fetch + repository validation + Java compile + unit tests.
  --standard    Fast checks + clean build + release JAR validation + PMD/CPD/Semgrep + Qodana. Default.
  --full        Standard checks + all four P10.5 dedicated server/client runtime variants.

Options:
  --skip-deps       Reuse the pinned jars already present in dev/libs.
  --skip-qodana     Skip Qodana while still running PMD, CPD, Semgrep, and the Tideborne delta gate.
  --base <commit>   Use an explicit quality-delta baseline instead of resolving origin/dev.
  -h, --help        Show this help.

This runner intentionally does not run GameTests.
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --fast) MODE="fast"; shift ;;
        --standard) MODE="standard"; shift ;;
        --full) MODE="full"; shift ;;
        --skip-deps) SKIP_DEPS="true"; shift ;;
        --skip-qodana) SKIP_QODANA="true"; shift ;;
        --base)
            if [[ $# -lt 2 ]]; then
                echo "--base requires a commit/ref" >&2
                exit 2
            fi
            BASE_OVERRIDE="$2"
            shift 2
            ;;
        -h|--help) usage; exit 0 ;;
        *)
            echo "Unknown option: $1" >&2
            usage >&2
            exit 2
            ;;
    esac
done

TOTAL=0
PASSED=0
START_SECONDS=$SECONDS
LOCAL_CI_ROOT="$repo_root/build/local-ci"
LOG_ROOT="$LOCAL_CI_ROOT/logs"
QUALITY_ROOT="$repo_root/build/quality-gate"
mkdir -p "$LOG_ROOT" "$QUALITY_ROOT"

summary() {
    local status="$1"
    local elapsed=$((SECONDS - START_SECONDS))
    echo
    echo "============================================================"
    echo "TIDEBORNE LOCAL CI: ${PASSED}/${TOTAL} PASS | ${status} | ${elapsed}s"
    echo "Mode: ${MODE}"
    echo "Logs: ${LOG_ROOT}"
    echo "============================================================"
}

trap 'summary "FAIL"' ERR

require_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "Missing required command: $1" >&2
        if [[ -n "${2:-}" ]]; then
            echo "Install hint: $2" >&2
        fi
        exit 1
    fi
}

run_step() {
    local name="$1"
    shift
    local safe_name
    safe_name="$(printf '%s' "$name" | tr '[:upper:] ' '[:lower:]-' | tr -cd '[:alnum:]_.-')"
    local log="$LOG_ROOT/${safe_name}.log"

    TOTAL=$((TOTAL + 1))
    echo
    echo "[$TOTAL] $name"
    echo "------------------------------------------------------------"
    if "$@" 2>&1 | tee "$log"; then
        PASSED=$((PASSED + 1))
        echo "PASS: $name"
    else
        local rc=${PIPESTATUS[0]}
        echo "FAIL: $name (exit $rc)" >&2
        return "$rc"
    fi
}

check_java_21() {
    local version_line
    version_line="$(java -version 2>&1 | head -n1)"
    if ! printf '%s\n' "$version_line" | grep -Eq 'version "21([."]|$)|openjdk 21([. ]|$)'; then
        echo "Java 21 is required. Detected: $version_line" >&2
        return 1
    fi
    echo "$version_line"
}

check_docker() {
    docker info >/dev/null 2>&1 || {
        echo "Docker is installed but the daemon is not available." >&2
        echo "On Windows, start Docker Desktop and enable WSL integration for this distro." >&2
        return 1
    }
    docker version --format 'Docker client {{.Client.Version}} / server {{.Server.Version}}'
}

resolve_version() {
    awk -F= '$1 == "mod_version" {gsub(/[[:space:]]/, "", $2); print $2}' gradle.properties
}

gradle_cmd() {
    ./gradlew --no-daemon "$@"
}

fetch_dependencies() {
    bash scripts/fetch_ci_dependencies.sh
}

validate_repository() {
    bash scripts/validate_repository.sh
}

compile_java() {
    gradle_cmd compileJava --stacktrace
}

unit_tests() {
    gradle_cmd test --stacktrace
}

production_build() {
    gradle_cmd clean build --stacktrace
}

validate_artifact() {
    local version
    version="$(resolve_version)"
    bash scripts/validate_release_artifact.sh "build/libs/tideborne-${version}.jar"
}

quality_base() {
    if [[ -n "$BASE_OVERRIDE" ]]; then
        git rev-parse "$BASE_OVERRIDE^{commit}"
    else
        python3 scripts/quality_gate.py resolve-base --default-branch dev | tail -n1
    fi
}

ensure_semgrep() {
    local cache_root="${XDG_CACHE_HOME:-$HOME/.cache}/tideborne"
    local venv="$cache_root/semgrep-1.176.0"
    mkdir -p "$cache_root"
    if [[ ! -x "$venv/bin/semgrep" ]]; then
        echo "Installing Semgrep 1.176.0 into $venv"
        python3 -m venv "$venv"
        "$venv/bin/python" -m pip install --disable-pip-version-check --upgrade pip
        "$venv/bin/python" -m pip install --disable-pip-version-check 'semgrep==1.176.0'
    fi
    printf '%s\n' "$venv/bin/semgrep"
}

run_pmd() {
    rm -f pmd-report.sarif
    docker run --rm \
        -v "$repo_root:/src" \
        pmdcode/pmd:7.27.0 \
        check \
        --dir /src/src/main/java \
        --rulesets /src/.github/pmd/tideborne-ruleset.xml \
        --format sarif \
        --report-file /src/pmd-report.sarif \
        --no-fail-on-violation
    test -s pmd-report.sarif
}

run_cpd() {
    mkdir -p "$QUALITY_ROOT"
    docker run --rm \
        -v "$repo_root:/src" \
        pmdcode/pmd:7.27.0 \
        cpd \
        --minimum-tokens 90 \
        --language java \
        --dir /src/src/main/java \
        --format xml \
        --no-fail-on-violation \
        > "$QUALITY_ROOT/cpd.xml"
    test -s "$QUALITY_ROOT/cpd.xml"
}

run_semgrep() {
    local base="$1"
    local semgrep
    semgrep="$(ensure_semgrep)"
    rm -f "$QUALITY_ROOT/semgrep.sarif"
    "$semgrep" scan \
        --config .github/semgrep/tideborne-ai.yml \
        --baseline-commit "$base" \
        --sarif-output "$QUALITY_ROOT/semgrep.sarif" \
        --metrics=off \
        src/main/java
    test -s "$QUALITY_ROOT/semgrep.sarif"
}

aggregate_quality() {
    local base="$1"
    python3 scripts/quality_gate.py analyze \
        --base "$base" \
        --pmd pmd-report.sarif \
        --cpd build/quality-gate/cpd.xml \
        --semgrep build/quality-gate/semgrep.sarif \
        --json build/quality-gate/report.json \
        --markdown build/quality-gate/report.md
}

run_qodana() {
    local base="$1"
    local cache_root="${XDG_CACHE_HOME:-$HOME/.cache}/tideborne/qodana"
    local results="$repo_root/build/qodana"
    mkdir -p "$cache_root" "$results"
    docker run --rm \
        -v "$repo_root:/data/project" \
        -v "$results:/data/results" \
        -v "$cache_root:/data/cache" \
        jetbrains/qodana-jvm-community:2026.2 \
        --diff-start "$base"
}

copy_runtime_logs() {
    local name="$1"
    local dest="$LOCAL_CI_ROOT/runtime/$name"
    mkdir -p "$dest"
    [[ -f build/dedicated-server-smoke.log ]] && cp build/dedicated-server-smoke.log "$dest/server.log"
    [[ -f build/dedicated-client-smoke.log ]] && cp build/dedicated-client-smoke.log "$dest/client.log"
    if compgen -G 'build/test-results/test/*.xml' >/dev/null; then
        mkdir -p "$dest/test-results"
        cp build/test-results/test/*.xml "$dest/test-results/"
    fi
}

runtime_variant() {
    local name="$1"
    local apex="$2"
    local myths="$3"

    rm -f build/dedicated-server-smoke.log build/dedicated-client-smoke.log
    export ORG_GRADLE_PROJECT_includeApexRuntime="$apex"
    export ORG_GRADLE_PROJECT_includeMythsRuntime="$myths"

    gradle_cmd test --stacktrace
    CONNECT_CLIENT=true bash scripts/dedicated_server_smoke.sh
    copy_runtime_logs "$name"

    unset ORG_GRADLE_PROJECT_includeApexRuntime
    unset ORG_GRADLE_PROJECT_includeMythsRuntime
}

echo "Tideborne local CI"
echo "Repository: $repo_root"
echo "Mode: $MODE"
echo "Commit: $(git rev-parse HEAD)"
echo

if [[ "$MODE" != "fast" ]] && [[ -n "$(git status --porcelain --untracked-files=normal)" ]]; then
    echo "Standard/full local CI requires a clean committed working tree so changed-line quality" >&2
    echo "analysis matches the revision being reviewed. Commit or stash changes, or use --fast first." >&2
    exit 1
fi

require_command git "sudo apt-get install git"
require_command java "sudo apt-get install openjdk-21-jdk"
require_command python3 "sudo apt-get install python3 python3-venv"
require_command curl "sudo apt-get install curl"
require_command jq "sudo apt-get install jq"
require_command unzip "sudo apt-get install unzip"
require_command sha256sum "sudo apt-get install coreutils"
require_command strings "sudo apt-get install binutils"
require_command rg "sudo apt-get install ripgrep"
test -x ./gradlew || { echo "gradlew is missing or not executable." >&2; exit 1; }

run_step "Java 21 preflight" check_java_21

if [[ "$SKIP_DEPS" != "true" ]]; then
    run_step "Fetch pinned dependencies" fetch_dependencies
else
    echo "Skipping dependency fetch by request."
fi

run_step "Repository validation" validate_repository
run_step "Compile production Java" compile_java
run_step "Unit tests" unit_tests

if [[ "$MODE" != "fast" ]]; then
    require_command docker "Install Docker Desktop and enable WSL integration."
    run_step "Docker preflight" check_docker
    run_step "Clean production build" production_build
    run_step "Validate production JAR" validate_artifact

    BASE="$(quality_base)"
    echo "Quality delta baseline: $BASE"
    git cat-file -e "$BASE^{commit}"

    run_step "PMD 7.27.0" run_pmd
    run_step "CPD 7.27.0" run_cpd
    run_step "Semgrep 1.176.0" run_semgrep "$BASE"
    run_step "Tideborne changed-line quality gate" aggregate_quality "$BASE"

    if [[ "$SKIP_QODANA" != "true" ]]; then
        run_step "Qodana JVM Community 2026.2" run_qodana "$BASE"
    else
        echo "Skipping Qodana by request."
    fi
fi

if [[ "$MODE" == "full" ]]; then
    require_command xvfb-run "sudo apt-get install xvfb"
    run_step "Runtime required-only" runtime_variant "required-only" "false" "false"
    run_step "Runtime Apex-only" runtime_variant "apex-only" "true" "false"
    run_step "Runtime Myths-only" runtime_variant "myths-only" "false" "true"
    run_step "Runtime Apex + Myths" runtime_variant "apex-and-myths" "true" "true"
fi

trap - ERR
if [[ "$MODE" != "fast" ]]; then
    version="$(resolve_version)"
    artifact="build/libs/tideborne-${version}.jar"
    if [[ -f "$artifact" ]]; then
        echo
        echo "Validated artifact: $artifact"
        sha256sum "$artifact"
    fi
fi
summary "PASS"

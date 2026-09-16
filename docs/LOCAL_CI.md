# Tideborne Local CI

Tideborne's GitHub-hosted validation can be reproduced on a developer PC without using GitHub Actions minutes.

The local runner deliberately reuses the repository's existing validation scripts instead of maintaining a second set of rules.

## Windows setup

The Windows entry point is:

```powershell
.\scripts\local-ci.ps1
```

The wrapper runs the Linux validation environment through WSL. This is required for the existing dedicated server/client smoke test, which uses Linux features including Xvfb, `/proc/net/tcp`, and FIFOs.

### One-time prerequisites

1. Install WSL with Ubuntu from an elevated PowerShell:

```powershell
wsl --install -d Ubuntu
```

2. In Ubuntu/WSL, install the local CI tools:

```bash
sudo apt-get update
sudo apt-get install -y \
  openjdk-21-jdk \
  python3 \
  python3-venv \
  git \
  curl \
  jq \
  unzip \
  ripgrep \
  xvfb \
  binutils
```

3. Install Docker Desktop for Windows and enable WSL integration for the Ubuntu distribution.

The standard/full modes use Docker for the pinned PMD/CPD and Qodana versions. Semgrep is installed once into the user's WSL cache at `~/.cache/tideborne`.

## Commands

Quick pre-commit check that may run with uncommitted source:

```powershell
.\scripts\local-ci.ps1 -Fast
```

This runs:

- pinned dependency fetch
- repository validation
- Java 21 production compilation
- normal unit tests

Normal PR-equivalent review:

```powershell
.\scripts\local-ci.ps1
```

This adds:

- clean production build
- production JAR validation
- PMD 7.27.0
- CPD 7.27.0
- Semgrep 1.176.0
- Tideborne changed-line quality aggregation
- Qodana JVM Community 2026.2

Standard and full modes require a clean committed working tree because the quality gate compares changed lines against a Git commit baseline.

Complete release/runtime review:

```powershell
.\scripts\local-ci.ps1 -Full
```

This adds the P10.5 real server/client matrix:

- required-only
- Apex-only
- Myths-only
- Apex + Myths

Each runtime variant runs normal unit tests and `scripts/dedicated_server_smoke.sh` with a real client connection.

GameTests are intentionally not run.

### Useful options

Reuse already-downloaded pinned dependencies:

```powershell
.\scripts\local-ci.ps1 -SkipDependencies
```

Run deterministic PMD/CPD/Semgrep checks without Qodana:

```powershell
.\scripts\local-ci.ps1 -SkipQodana
```

Set an explicit quality-delta baseline:

```powershell
.\scripts\local-ci.ps1 -Base <commit-or-ref>
```

Linux/WSL users can call the underlying runner directly:

```bash
bash scripts/local_ci.sh --fast
bash scripts/local_ci.sh --standard
bash scripts/local_ci.sh --full
```

## Output

Step logs are written under:

```text
build/local-ci/logs/
```

Runtime logs are preserved per matrix variant under:

```text
build/local-ci/runtime/
```

Quality reports remain compatible with the existing GitHub quality gate:

```text
build/quality-gate/report.json
build/quality-gate/report.md
```

A successful standard/full run also prints the validated production JAR path and SHA-256.

The final console line is designed to be easy to paste into a PR or chat:

```text
TIDEBORNE LOCAL CI: N/N PASS | PASS
```

## GitHub Actions parity

| GitHub workflow | Local equivalent |
| --- | --- |
| Build Tideborne | default `local-ci.ps1` |
| Repository + Java 21 compile | default or `-Fast` |
| PMD + CPD + Semgrep delta | default |
| Qodana JVM delta | default |
| P10.5 Runtime Matrix | `-Full` |

The local runner does not publish GitHub check marks or workflow artifacts. It validates the same repository locally without consuming GitHub-hosted runner minutes.

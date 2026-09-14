#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

gradle_bin="${GRADLE_BIN:-./gradlew}"
smoke_dir="$repo_root/run/server"
smoke_log="$repo_root/build/dedicated-server-smoke.log"
input_fifo="$repo_root/build/dedicated-server-input.fifo"
server_pid=""
client_pid=""
client_log="$repo_root/build/dedicated-client-smoke.log"
client_argv_log="$repo_root/build/dedicated-client-argv.log"

mkdir -p "$smoke_dir" "$repo_root/build"
printf 'eula=true\n' > "$smoke_dir/eula.txt"
printf 'online-mode=false\nserver-ip=127.0.0.1\nserver-port=25565\nspawn-protection=0\n' > "$smoke_dir/server.properties"
rm -f "$input_fifo"
mkfifo "$input_fifo"
exec 3<>"$input_fifo"

cleanup() {
    if [[ -n "$client_pid" ]] && kill -0 "$client_pid" 2>/dev/null; then
        kill "$client_pid" 2>/dev/null || true
        wait "$client_pid" 2>/dev/null || true
    fi
    if [[ -n "$server_pid" ]] && kill -0 "$server_pid" 2>/dev/null; then
        kill "$server_pid" 2>/dev/null || true
        wait "$server_pid" 2>/dev/null || true
    fi
    exec 3>&- || true
    rm -f "$input_fifo"
}
trap cleanup EXIT

"$gradle_bin" runServer --console=plain --no-daemon <"$input_fifo" >"$smoke_log" 2>&1 &
server_pid=$!

ready=0
for _ in $(seq 1 240); do
    # Gradle buffers JavaExec output when redirected, so the server log may not be
    # observable until runServer exits. Inspect the kernel socket table instead.
    if awk '$2 ~ /:63DD$/ && $4 == "0A" { found = 1 } END { exit !found }' \
        /proc/net/tcp /proc/net/tcp6 2>/dev/null; then
        ready=1
        break
    fi
    if ! kill -0 "$server_pid" 2>/dev/null; then
        break
    fi
    sleep 1
done

if [[ "$ready" -ne 1 ]]; then
    echo 'Dedicated server did not reach ready state.' >&2
    sed -n '1,260p' "$smoke_log" >&2
    exit 1
fi

if [[ "${CONNECT_CLIENT:-false}" == "true" ]]; then
    if ! command -v xvfb-run >/dev/null 2>&1; then
        echo 'CONNECT_CLIENT=true requires xvfb-run.' >&2
        exit 1
    fi

    # Put the Quick Play arguments into Loom's client run configuration. This is
    # more deterministic than replacing JavaExec arguments from the Gradle CLI and
    # mirrors how Fabric expects Minecraft program arguments to be supplied.
    mkdir -p "$repo_root/run/client/quickPlay"
    : > "$client_argv_log"
    env TIDEBORNE_CI_QUICKPLAY_TARGET='localhost:25565' \
        timeout 180s xvfb-run -a "$gradle_bin" runClient --console=plain --no-daemon \
        >"$client_log" 2>&1 &
    client_pid=$!

    # Capture the real JavaExec child and any Loom argfile. This makes failures
    # distinguish a missing launch argument from a client-side Quick Play refusal.
    (
        for _ in $(seq 1 90); do
            for proc_dir in /proc/[0-9]*; do
                proc_cwd="$(readlink "$proc_dir/cwd" 2>/dev/null || true)"
                [[ "$proc_cwd" == "$repo_root/run/client" ]] || continue
                proc_pid="${proc_dir##*/}"
                printf 'pid=%s\ncwd=%s\n' "$proc_pid" "$proc_cwd" >> "$client_argv_log"
                mapfile -d '' -t proc_args < "$proc_dir/cmdline" 2>/dev/null || true
                printf 'argv:\n' >> "$client_argv_log"
                printf '  %s\n' "${proc_args[@]}" >> "$client_argv_log"
                for proc_arg in "${proc_args[@]}"; do
                    if [[ "$proc_arg" == @* ]]; then
                        argfile="${proc_arg#@}"
                        if [[ -f "$argfile" ]]; then
                            printf 'argfile=%s\n' "$argfile" >> "$client_argv_log"
                            sed 's/^/  /' "$argfile" >> "$client_argv_log"
                        fi
                    fi
                done
                exit 0
            done
            sleep 1
        done
        printf 'No run/client process was captured.\n' >> "$client_argv_log"
    ) &
    argv_capture_pid=$!

    connected=0
    for _ in $(seq 1 180); do
        if awk '$2 ~ /:63DD$/ && $4 == "01" { found = 1 } END { exit !found }' \
            /proc/net/tcp /proc/net/tcp6 2>/dev/null; then
            connected=1
            break
        fi
        if ! kill -0 "$client_pid" 2>/dev/null; then
            break
        fi
        sleep 1
    done
    wait "$argv_capture_pid" 2>/dev/null || true

    if [[ "$connected" -ne 1 ]]; then
        echo 'Client did not establish a connection to the dedicated server.' >&2
        cat "$client_argv_log" >&2 || true
        sed -n '1,220p' "$client_log" >&2
        sed -n '1,260p' "$smoke_log" >&2
        exit 1
    fi

    # Allow login/configuration and normal client initialization to complete.
    for _ in $(seq 1 30); do
        if ! kill -0 "$client_pid" 2>/dev/null; then
            break
        fi
        sleep 1
    done

    kill "$client_pid" 2>/dev/null || true
    wait "$client_pid" 2>/dev/null || true
    client_pid=""

    if rg -n 'Mixin apply failed|Could not execute entrypoint|NoClassDefFoundError|ClassNotFoundException|ExceptionInInitializerError|ReportedException' "$client_log"; then
        echo 'Dedicated client log contains a startup or classloading failure.' >&2
        exit 1
    fi
    if ! rg -q '\[Tideborne\] Unified client configuration and rendering systems initialized\.' "$client_log"; then
        echo 'Dedicated client did not finish Tideborne client initialization.' >&2
        sed -n '1,220p' "$client_log" >&2
        exit 1
    fi
fi

printf 'stop\n' >&3
for _ in $(seq 1 60); do
    if ! kill -0 "$server_pid" 2>/dev/null; then
        wait "$server_pid"
        server_pid=""
        if rg -n 'Mixin apply failed|Could not execute entrypoint|NoClassDefFoundError|ClassNotFoundException|ExceptionInInitializerError' "$smoke_log"; then
            echo 'Dedicated server log contains a startup or classloading failure.' >&2
            exit 1
        fi
        if [[ "${CONNECT_CLIENT:-false}" == "true" ]]; then
            if ! rg -q ' joined the game' "$smoke_log"; then
                echo 'Client connected at the socket layer but did not finish joining the dedicated server.' >&2
                sed -n '1,220p' "$client_log" >&2
                sed -n '1,260p' "$smoke_log" >&2
                exit 1
            fi
            echo "Dedicated client connection passed: $client_log"
        fi
        echo "Dedicated server smoke test passed: $smoke_log"
        exit 0
    fi
    sleep 1
done

echo 'Dedicated server did not stop cleanly.' >&2
exit 1

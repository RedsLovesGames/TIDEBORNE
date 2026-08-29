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
    if rg -q 'Done \([0-9.]+s\)!' "$smoke_log" 2>/dev/null; then
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

if rg -n 'Mixin apply failed|Could not execute entrypoint|NoClassDefFoundError|ClassNotFoundException|ExceptionInInitializerError' "$smoke_log"; then
    echo 'Dedicated server log contains a startup or classloading failure.' >&2
    exit 1
fi

if [[ "${CONNECT_CLIENT:-false}" == "true" ]]; then
    if ! command -v xvfb-run >/dev/null 2>&1; then
        echo 'CONNECT_CLIENT=true requires xvfb-run.' >&2
        exit 1
    fi

    timeout 180s xvfb-run -a "$gradle_bin" runClient --console=plain --no-daemon \
        --args='--server 127.0.0.1 --port 25565' >"$client_log" 2>&1 &
    client_pid=$!
    joined=0
    for _ in $(seq 1 180); do
        if rg -q ' joined the game' "$smoke_log" 2>/dev/null; then
            joined=1
            break
        fi
        if ! kill -0 "$client_pid" 2>/dev/null; then
            break
        fi
        sleep 1
    done

    if [[ "$joined" -ne 1 ]]; then
        echo 'Client did not connect to the dedicated server.' >&2
        sed -n '1,220p' "$client_log" >&2
        sed -n '1,260p' "$smoke_log" >&2
        exit 1
    fi

    kill "$client_pid" 2>/dev/null || true
    wait "$client_pid" 2>/dev/null || true
    client_pid=""
    echo "Dedicated client connection passed: $client_log"
fi

printf 'stop\n' >&3
for _ in $(seq 1 60); do
    if ! kill -0 "$server_pid" 2>/dev/null; then
        wait "$server_pid"
        server_pid=""
        echo "Dedicated server smoke test passed: $smoke_log"
        exit 0
    fi
    sleep 1
done

echo 'Dedicated server did not stop cleanly.' >&2
exit 1

#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "usage: $0 build/libs/tideborne-2.0.0.jar" >&2
    exit 2
fi

artifact="$1"
expected_name="tideborne-2.0.0.jar"

test -f "$artifact"
test "$(basename "$artifact")" = "$expected_name"
case "$artifact" in
    *-sources.jar|*-dev.jar) echo "Not a production artifact: $artifact" >&2; exit 1 ;;
esac

version="$(unzip -p "$artifact" fabric.mod.json | python3 -c 'import json,sys; print(json.load(sys.stdin)["version"])')"
test "$version" = "2.0.0"

unzip -l "$artifact" 'com/redslovesgames/tideborne/Tideborne.class' | rg -q 'Tideborne\.class'
unzip -l "$artifact" 'com/redslovesgames/tideborne/fishing/v2/SpecimenGenerator.class' | rg -q 'SpecimenGenerator\.class'
unzip -l "$artifact" 'com/redslovesgames/tideborne/fishing/v2/integration/TideSpeciesSelectionBridge.class' | rg -q 'TideSpeciesSelectionBridge\.class'

echo "Production release artifact passed: $artifact"
sha256sum "$artifact"

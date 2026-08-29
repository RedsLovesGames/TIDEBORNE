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
unzip -l "$artifact" 'com/redslovesgames/tideboundcompatibility/mixin/AnglingTableLeaderMixin.class' | rg -q 'AnglingTableLeaderMixin\.class'
unzip -l "$artifact" 'com/redslovesgames/tideboundcompatibility/mixin/AnglingTableScreenLeaderMixin.class' | rg -q 'AnglingTableScreenLeaderMixin\.class'

python3 - "$artifact" <<'PY'
import json
import sys
import zipfile

artifact = sys.argv[1]
with zipfile.ZipFile(artifact) as jar:
    refmap = json.loads(jar.read('tidebound_compatibility.refmap.json'))

expected = {
    'com/redslovesgames/tideboundcompatibility/mixin/AnglingTableLeaderMixin': {
        'getForgingSlotsManager': 'Lcom/li64/tide/client/gui/menus/AnglingTableMenu;method_48352()Lnet/minecraft/class_8047;',
        'updateResult': 'Lcom/li64/tide/client/gui/menus/AnglingTableMenu;method_24928()V',
    },
    'com/redslovesgames/tideboundcompatibility/mixin/AnglingTableScreenLeaderMixin': {
        'drawInvalidRecipeArrow': 'Lcom/li64/tide/client/gui/screens/AnglingTableScreen;method_48467(Lnet/minecraft/class_332;II)V',
    },
}

for section in (refmap.get('mappings', {}), refmap.get('data', {}).get('named:intermediary', {})):
    for mixin, members in expected.items():
        actual = section.get(mixin)
        if actual is None:
            raise SystemExit(f'Missing production refmap entry for {mixin}')
        for member, target in members.items():
            if actual.get(member) != target:
                raise SystemExit(f'Bad production refmap target for {mixin}.{member}: {actual.get(member)!r}')
PY

if unzip -p "$artifact" 'com/redslovesgames/tideboundcompatibility/fishing/AnglingTableLeaderSupport.class' \
    | strings | rg -q 'net\.minecraft\.(screen\.slot\.ForgingSlotsManager|item\.ItemStack)'; then
    echo "Production AnglingTableLeaderSupport still contains Yarn reflection class names" >&2
    exit 1
fi

if unzip -p "$artifact" 'com/redslovesgames/tideboundcompatibility/fishing/SteelLeaderAttachment.class' \
    | strings | rg -q 'getRodItem'; then
    echo "Production SteelLeaderAttachment still uses TideFishingHook.getRodItem for rod-stack state" >&2
    exit 1
fi

echo "Production release artifact passed: $artifact"
sha256sum "$artifact"

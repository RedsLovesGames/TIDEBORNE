#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "usage: $0 build/libs/tideborne-<version>.jar" >&2
    exit 2
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
artifact="$1"
expected_version="$(awk -F= '$1 == "mod_version" {print $2}' "$repo_root/gradle.properties" | tr -d '[:space:]')"
expected_name="tideborne-${expected_version}.jar"

if [[ ! "$expected_version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
    echo "gradle.properties mod_version is not an exact MAJOR.MINOR.PATCH semantic version: $expected_version" >&2
    exit 1
fi

if [[ ! -f "$artifact" ]]; then
    echo "Release artifact does not exist: $artifact" >&2
    exit 1
fi
if [[ "$(basename "$artifact")" != "$expected_name" ]]; then
    echo "Release artifact name mismatch: expected $expected_name, got $(basename "$artifact")" >&2
    exit 1
fi
case "$artifact" in
    *-sources.jar|*-dev.jar) echo "Not a production artifact: $artifact" >&2; exit 1 ;;
esac

version="$(unzip -p "$artifact" fabric.mod.json | python3 -c 'import json,sys; print(json.load(sys.stdin)["version"])')"
if [[ "$version" != "$expected_version" ]]; then
    echo "Release artifact version mismatch: expected $expected_version, fabric.mod.json reports $version" >&2
    exit 1
fi

require_class() {
    local class_path="$1"
    if ! unzip -l "$artifact" "$class_path" | grep -F "$(basename "$class_path")" >/dev/null; then
        echo "Missing required production class: $class_path" >&2
        exit 1
    fi
}

require_class 'com/redslovesgames/tideborne/Tideborne.class'
require_class 'com/redslovesgames/tideborne/fishing/specimen/SpecimenGenerator.class'
require_class 'com/redslovesgames/tideborne/fishing/SpeciesSelectionService.class'
require_class 'com/redslovesgames/tideborne/mixin/tide/AnglingTableLeaderMixin.class'
require_class 'com/redslovesgames/tideborne/mixin/tide/AnglingTableScreenLeaderMixin.class'
require_class 'com/redslovesgames/tideborne/mixin/journal/TeamProgressCanonicalJournalMixin.class'

python3 - "$artifact" <<'PY'
import json
import sys
import zipfile

artifact = sys.argv[1]
with zipfile.ZipFile(artifact) as jar:
    refmap = json.loads(jar.read('tidebound_compatibility.refmap.json'))

expected = {
    'AnglingTableLeaderMixin': {
        'getForgingSlotsManager': 'Lcom/li64/tide/client/gui/menus/AnglingTableMenu;method_48352()Lnet/minecraft/class_8047;',
        'updateResult': 'Lcom/li64/tide/client/gui/menus/AnglingTableMenu;method_24928()V',
    },
    'AnglingTableScreenLeaderMixin': {
        'drawInvalidRecipeArrow': 'Lcom/li64/tide/client/gui/screens/AnglingTableScreen;method_48467(Lnet/minecraft/class_332;II)V',
    },
}

for section_name, section in (
    ('mappings', refmap.get('mappings', {})),
    ('named:intermediary', refmap.get('data', {}).get('named:intermediary', {})),
):
    for simple_name, members in expected.items():
        candidates = [
            (key, value)
            for key, value in section.items()
            if key.replace('.', '/').rsplit('/', 1)[-1] == simple_name
        ]
        if len(candidates) != 1:
            keys = [key for key, _ in candidates]
            raise SystemExit(
                f'Expected exactly one production refmap entry for {simple_name} in {section_name}; found {keys}'
            )
        mixin, actual = candidates[0]
        for member, target in members.items():
            if actual.get(member) != target:
                raise SystemExit(f'Bad production refmap target for {mixin}.{member}: {actual.get(member)!r}')
PY

if unzip -p "$artifact" 'com/redslovesgames/tideborne/fishing/gear/AnglingTableLeaderSupport.class' \
    | strings | grep -E 'net\.minecraft\.(screen\.slot\.ForgingSlotsManager|item\.ItemStack)' >/dev/null; then
    echo "Production AnglingTableLeaderSupport still contains Yarn reflection class names" >&2
    exit 1
fi

if unzip -p "$artifact" 'com/redslovesgames/tideborne/fishing/gear/SteelLeaderAttachment.class' \
    | strings | grep 'getRodItem' >/dev/null; then
    echo "Production SteelLeaderAttachment still uses TideFishingHook.getRodItem for rod-stack state" >&2
    exit 1
fi

if unzip -p "$artifact" 'com/redslovesgames/tideborne/mixin/journal/TeamProgressCanonicalJournalMixin.class' \
    | strings | grep -E 'tideborne\$(storedLeaderboardScore|canonicalMergeRead|canonicalMergeWrite|canonicalContributorRead|canonicalContributorWrite|canonicalContributorRegistration|canonicalEventPresence|canonicalEventRead|canonicalEventFallback|canonicalTopFishRead)' >/dev/null; then
    echo "Production TeamProgressCanonicalJournalMixin still contains fragile Minecraft redirect handlers" >&2
    exit 1
fi

echo "Production release artifact passed for Tideborne ${expected_version}: $artifact"
sha256sum "$artifact"

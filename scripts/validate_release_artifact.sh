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
expected = {
    'tideborne.mixins.json',
    'tideborne.client.mixins.json',
    'tideborne.apex.mixins.json',
    'tideborne-refmap.json',
    'tideborne.apex.refmap.json',
}
retired = {
    'tide_traits.mixins.json',
    'tide_traits.client.mixins.json',
    'tide_team_journal.mixins.json',
    'tidebound_compatibility.mixins.json',
    'tidebound_compatibility.apex.mixins.json',
    'tide_team_journal.refmap.json',
    'tidebound_compatibility.refmap.json',
    'tideborne.refmap.json',
}
with zipfile.ZipFile(artifact) as jar:
    names = set(jar.namelist())
    missing = sorted(expected - names)
    if missing:
        raise SystemExit(f'Missing canonical mixin metadata: {missing}')
    leaked = sorted(retired & names)
    if leaked:
        raise SystemExit(f'Retired/overlapping mixin metadata leaked into artifact: {leaked}')
    fabric = json.loads(jar.read('fabric.mod.json'))
    common = json.loads(jar.read('tideborne.mixins.json'))
    client = json.loads(jar.read('tideborne.client.mixins.json'))
    apex = json.loads(jar.read('tideborne.apex.mixins.json'))
    generated = json.loads(jar.read('tideborne-refmap.json'))
    apex_refmap = json.loads(jar.read('tideborne.apex.refmap.json'))

expected_mixins = [
    'tideborne.mixins.json',
    {'config': 'tideborne.client.mixins.json', 'environment': 'client'},
    'tideborne.apex.mixins.json',
]
if fabric.get('mixins') != expected_mixins:
    raise SystemExit(f'Unexpected Fabric mixin config list: {fabric.get("mixins")!r}')
if not {'tide_traits', 'tide_team_journal', 'tidebound_compatibility'}.issubset(set(fabric.get('provides', []))):
    raise SystemExit('Historical compatibility provides were not preserved')

if common.get('package') != 'com.redslovesgames.tideborne.mixin':
    raise SystemExit(f'Unexpected common mixin package: {common.get("package")!r}')
if common.get('required') is not True or common.get('injectors', {}).get('defaultRequire') != 1:
    raise SystemExit('Common mixin strict failure semantics changed')
if common.get('refmap') != 'tideborne-refmap.json':
    raise SystemExit(f'Loom did not wire common config to generated refmap: {common.get("refmap")!r}')
for required in ('specimen.AnglersSatchelRecipeMixin', 'journal.TeamProgressCanonicalJournalMixin', 'tide.AnglingTableLeaderMixin'):
    if required not in common.get('mixins', []):
        raise SystemExit(f'Missing common mixin registration: {required}')
for required in ('journal.client.FishingJournalMixin', 'tide.AnglingTableScreenLeaderMixin'):
    if required not in common.get('client', []):
        raise SystemExit(f'Missing strict client mixin registration: {required}')

if client.get('package') != 'com.redslovesgames.tideborne.mixin':
    raise SystemExit(f'Unexpected client mixin package: {client.get("package")!r}')
if client.get('required') is not False or client.get('injectors', {}).get('defaultRequire') != 0:
    raise SystemExit('Specimen client tolerant failure semantics changed')
if client.get('refmap') != 'tideborne-refmap.json':
    raise SystemExit(f'Loom did not wire client config to generated refmap: {client.get("refmap")!r}')
if 'specimen.client.MinecraftMutationRenderingMixin' not in client.get('client', []):
    raise SystemExit('Canonical specimen client registration missing')

if apex.get('package') != 'com.redslovesgames.tideborne.mixin.compat.apex':
    raise SystemExit(f'Unexpected Apex mixin package: {apex.get("package")!r}')
if apex.get('required') is not False or apex.get('injectors', {}).get('defaultRequire') != 1:
    raise SystemExit('Optional Apex failure semantics changed')
if apex.get('plugin') != 'com.redslovesgames.tideborne.mixin.tide.OptionalCompatMixinPlugin':
    raise SystemExit('Optional Apex plugin boundary changed')
if apex.get('refmap') != 'tideborne.apex.refmap.json':
    raise SystemExit(f'Unexpected Apex refmap: {apex.get("refmap")!r}')

for section_name, section in (
    ('mappings', generated.get('mappings', {})),
    ('named:intermediary', generated.get('data', {}).get('named:intermediary', {})),
):
    if len(section) < 20:
        raise SystemExit(f'Generated Tideborne refmap unexpectedly sparse in {section_name}: {len(section)} entries')
    if any('tideteamjournal' in key or 'tideboundcompatibility' in key for key in section):
        raise SystemExit(f'Stale pre-unification class key in generated {section_name}')
    for key in (
        'com/redslovesgames/tideborne/mixin/journal/TideFishingHookMixin',
        'com/redslovesgames/tideborne/mixin/specimen/TideFishingHookMixin',
        'com/redslovesgames/tideborne/mixin/specimen/client/MinecraftMutationRenderingMixin',
        'com/redslovesgames/tideborne/mixin/tide/AnglingTableLeaderMixin',
        'com/redslovesgames/tideborne/mixin/tide/AnglingTableScreenLeaderMixin',
    ):
        if key not in section:
            raise SystemExit(f'Missing generated refmap entry {key} in {section_name}')

apex_key = 'com/redslovesgames/tideborne/mixin/compat/apex/GreatWhiteSharkMixin'
expected_apex_target = 'Lcom/acorsicanfrog/apexwaters/entity/GreatWhiteSharkEntity;method_5959()V'
for section_name, section in (
    ('mappings', apex_refmap.get('mappings', {})),
    ('named:intermediary', apex_refmap.get('data', {}).get('named:intermediary', {})),
):
    if set(section) != {apex_key}:
        raise SystemExit(f'Apex refmap must be integration-only in {section_name}: {sorted(section)}')
    if section[apex_key].get('registerGoals') != expected_apex_target:
        raise SystemExit(f'Bad Apex registerGoals mapping in {section_name}')
PY

python3 - "$artifact" <<'PY'
import json
import sys
import zipfile

artifact = sys.argv[1]
with zipfile.ZipFile(artifact) as jar:
    refmap = json.loads(jar.read('tideborne-refmap.json'))

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

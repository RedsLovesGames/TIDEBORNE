#!/usr/bin/env bash
set -euo pipefail

mkdir -p dev/libs

fetch_modrinth_primary() {
    local project_id="$1"
    local version_id="$2"
    local output="$3"
    local expected_sha="$4"
    local json url

    json="$(curl -fsSL "https://api.modrinth.com/v2/version/${version_id}")"
    test "$(printf '%s' "$json" | jq -r '.project_id')" = "$project_id"
    url="$(printf '%s' "$json" | jq -r '.files[] | select(.primary == true) | .url' | head -n1)"
    test -n "$url"
    curl -fsSL "$url" -o "$output"
    printf '%s  %s\n' "$expected_sha" "$output" | sha256sum --check --strict
}

# Tide 2.1.1 Fabric for Minecraft 1.21.1.
tide_json="$(curl -fsSLG \
    --data-urlencode 'game_versions=["1.21.1"]' \
    --data-urlencode 'loaders=["fabric"]' \
    --data-urlencode 'include_changelog=false' \
    'https://api.modrinth.com/v2/project/die1AF7i/version')"
tide_version_id="$(printf '%s' "$tide_json" | jq -r '[.[] | select(.name == "Tide 2.1.1 (Fabric 1.21.1)")][0].id // empty')"
tide_url="$(printf '%s' "$tide_json" | jq -r '[.[] | select(.name == "Tide 2.1.1 (Fabric 1.21.1)")][0].files[] | select(.primary == true) | .url' | head -n1)"
test -n "$tide_version_id"
test -n "$tide_url"
curl -fsSL "$tide_url" -o dev/libs/tide-fabric-1.21.1-2.1.1.jar
test "$(unzip -p dev/libs/tide-fabric-1.21.1-2.1.1.jar fabric.mod.json | jq -r '.version')" = '2.1.1'
echo '498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8  dev/libs/tide-fabric-1.21.1-2.1.1.jar' | sha256sum --check --strict

echo "Tide 2.1.1 Modrinth version: $tide_version_id"

# Apex Waters 1.1.1 Fabric 1.21.1.
curl -fsSL \
    'https://github.com/acorsicanfrog/apexwaters/releases/download/1.1.1/apexwaters-1.21.1-fabric-1.1.1.jar' \
    -o dev/libs/apex-waters-fabric-1.21.1-1.1.1.jar
echo '00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37  dev/libs/apex-waters-fabric-1.21.1-1.1.1.jar' | sha256sum --check --strict

# Myths of the Sea and exact compatible dependencies.
fetch_modrinth_primary 'WaCchT6K' '3UawtEXr' \
    dev/libs/myths-of-the-sea-fabric-1.21.1-1.3.0.jar \
    '55e0944707b91dc3ae15d8a5162184f8327521c4ace71d05f9c0985495222bab'
test "$(unzip -p dev/libs/myths-of-the-sea-fabric-1.21.1-1.3.0.jar fabric.mod.json | jq -r '.id')" = 'myths_of_the_sea'
test "$(unzip -p dev/libs/myths-of-the-sea-fabric-1.21.1-1.3.0.jar fabric.mod.json | jq -r '.version')" = '1.3.0'

fetch_modrinth_primary 'GkIc6rRo' 'kxmVyCru' \
    dev/libs/cerbons-api-fabric-1.21.1-1.3.0.jar \
    '0587dc8b69ed66aa98f5054a7cad23ed58fc437e7da316d5fc82491969caf03d'
fetch_modrinth_primary '8BmcQJ2H' 'dnJdtm0u' \
    dev/libs/geckolib-fabric-1.21.1-4.9.2.jar \
    'eac4cf55e1cb99b22cb91ea87c774d2a670c1711a80f4aff50e6ad56b6c58e7e'

# Loom 1.10 can remap these exact upstream artifacts after removing only the
# newer producer Loom version marker from ephemeral CI copies.
python3 - <<'PY'
import hashlib
import os
import re
import zipfile
from pathlib import Path

jars = [
    Path('dev/libs/myths-of-the-sea-fabric-1.21.1-1.3.0.jar'),
    Path('dev/libs/cerbons-api-fabric-1.21.1-1.3.0.jar'),
    Path('dev/libs/geckolib-fabric-1.21.1-4.9.2.jar'),
]
supported = (1, 10)

for jar in jars:
    with zipfile.ZipFile(jar, 'r') as zin:
        infos = zin.infolist()
        payloads = {info.filename: zin.read(info.filename) for info in infos}

    manifest_name = 'META-INF/MANIFEST.MF'
    manifest = payloads.get(manifest_name)
    if manifest is None:
        continue

    text = manifest.decode('utf-8')
    loom_match = re.search(r'(?im)^Fabric-Loom-Version:\s*([^\r\n]+)', text)
    remap_match = re.search(r'(?im)^Fabric-Loom-Mixin-Remap-Type:\s*([^\r\n]+)', text)
    if not loom_match or not remap_match or remap_match.group(1).strip().lower() != 'static':
        continue

    version = loom_match.group(1).strip()
    numeric = tuple(int(part) for part in version.split('.')[:2])
    if numeric <= supported:
        continue

    original_non_manifest = {
        name: hashlib.sha256(data).digest()
        for name, data in payloads.items()
        if name != manifest_name
    }
    payloads[manifest_name] = re.sub(
        r'(?im)^Fabric-Loom-Version:[^\r\n]*(?:\r\n|\n|\r)?',
        '',
        text,
        count=1,
    ).encode('utf-8')

    tmp = jar.with_suffix(jar.suffix + '.tmp')
    with zipfile.ZipFile(tmp, 'w') as zout:
        for info in infos:
            zout.writestr(info, payloads[info.filename])
    os.replace(tmp, jar)

    with zipfile.ZipFile(jar, 'r') as check:
        for name, digest in original_non_manifest.items():
            if hashlib.sha256(check.read(name)).digest() != digest:
                raise RuntimeError(f'{jar}: non-manifest entry changed: {name}')
        check_manifest = check.read(manifest_name).decode('utf-8')
        if re.search(r'(?im)^Fabric-Loom-Version:', check_manifest):
            raise RuntimeError(f'{jar}: Fabric-Loom-Version was not removed')
        if not re.search(r'(?im)^Fabric-Loom-Mixin-Remap-Type:\s*static\s*$', check_manifest):
            raise RuntimeError(f'{jar}: static mixin remap request was not preserved')

    print(f'{jar}: sanitized producer Loom {version} metadata for Loom 1.10 QA remapping')
PY

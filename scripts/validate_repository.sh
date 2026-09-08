#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if rg -n '\b(class|method|field)_[0-9]+' src/main/java; then
    echo 'Unresolved intermediary identifiers remain in maintained source.' >&2
    exit 1
fi

python3 - <<'PY'
import json
import re
from pathlib import Path

root = Path.cwd()
fabric_path = root / "src/main/resources/fabric.mod.json"
fabric = json.loads(fabric_path.read_text(encoding="utf-8"))
properties = {}
for line in (root / "gradle.properties").read_text(encoding="utf-8").splitlines():
    if "=" in line and not line.lstrip().startswith("#"):
        key, value = line.split("=", 1)
        properties[key.strip()] = value.strip()

mod_version = properties.get("mod_version", "")
if not re.fullmatch(r"(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)", mod_version):
    raise SystemExit("gradle.properties mod_version must be an exact MAJOR.MINOR.PATCH semantic version")
if fabric.get("version") != "${version}":
    raise SystemExit("fabric.mod.json must use the Gradle version placeholder")

entrypoints = fabric.get("entrypoints", {})
all_entrypoints = {
    value
    for values in entrypoints.values()
    for value in values
    if isinstance(value, str)
}
for class_name in sorted(all_entrypoints):
    source = root / "src/main/java" / (class_name.replace(".", "/") + ".java")
    if not source.is_file():
        raise SystemExit(f"Missing entrypoint source: {class_name}")

annotated_gametests = set()
fqcn_sources = {}
for source in (root / "src/main/java").rglob("*.java"):
    text = source.read_text(encoding="utf-8")
    package_match = re.search(r"(?m)^package\s+([\w.]+);", text)
    type_match = re.search(r"(?m)^public\s+(?:final\s+|abstract\s+)?(?:class|record|interface|enum)\s+(\w+)", text)
    if package_match and type_match:
        fqcn = package_match.group(1) + "." + type_match.group(1)
        if fqcn in fqcn_sources:
            raise SystemExit(f"Duplicate top-level type {fqcn}: {fqcn_sources[fqcn]} and {source}")
        fqcn_sources[fqcn] = source
        if "@GameTest" in text:
            annotated_gametests.add(fqcn)

registered_gametests = set(entrypoints.get("fabric-gametest", []))
missing_gametests = sorted(annotated_gametests - registered_gametests)
if missing_gametests:
    raise SystemExit("Unregistered GameTest classes: " + ", ".join(missing_gametests))

for mixin_entry in fabric.get("mixins", []):
    config_name = mixin_entry if isinstance(mixin_entry, str) else mixin_entry["config"]
    config_path = root / "src/main/resources" / config_name
    config = json.loads(config_path.read_text(encoding="utf-8"))
    package = config["package"]
    for group in ("mixins", "client", "server"):
        for relative_name in config.get(group, []):
            class_name = package + "." + relative_name
            source = root / "src/main/java" / (class_name.replace(".", "/") + ".java")
            if not source.is_file():
                raise SystemExit(f"Missing mixin source from {config_name}: {class_name}")

main_entrypoint = root / "src/main/java/com/redslovesgames/tideborne/Tideborne.java"
main_text = main_entrypoint.read_text(encoding="utf-8")
if re.search(r"(?m)^import\s+(?:net\.minecraft\.client|net\.fabricmc\.fabric\.api\.client)\.", main_text):
    raise SystemExit("The common Tideborne entrypoint imports a client-only API")

print(f"Repository validation passed for Tideborne {mod_version}: {len(fqcn_sources)} top-level types, "
      f"{len(annotated_gametests)} registered GameTest classes, all mixins and entrypoints resolved")
PY

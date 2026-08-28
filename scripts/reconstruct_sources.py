#!/usr/bin/env python3
"""Reconstruct Tideborne 1.3.57 maintained source from the authoritative bytecode tree.

The preferred input is the exact release JAR. For the one-time private GitHub recovery,
a content-identical repacked JAR is also accepted when its canonical content-tree digest
matches the pinned 1.3.57 baseline. ZIP container metadata is not treated as gameplay
state.
"""

from __future__ import annotations

import argparse
import gzip
import hashlib
import json
import re
import shutil
import struct
import subprocess
import sys
import urllib.request
import zipfile
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path

BASELINE_VERSION = "1.3.57"
EXPECTED_TIDEBORNE_SHA256 = "0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5"
EXPECTED_CONTENT_TREE_SHA256 = "5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6"
EXPECTED_FILE_COUNT = 442
EXPECTED_CLASS_COUNT = 272
VINEFLOWER_VERSION = "1.12.0"
YARN_VERSION = "1.21.1+build.3"

VINEFLOWER_URL = (
    "https://repo1.maven.org/maven2/org/vineflower/vineflower/"
    f"{VINEFLOWER_VERSION}/vineflower-{VINEFLOWER_VERSION}.jar"
)
YARN_TINY_URL = (
    "https://maven.fabricmc.net/net/fabricmc/yarn/"
    f"{YARN_VERSION}/yarn-{YARN_VERSION}-tiny.gz"
)

PROVENANCE_HEADER = """/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
"""


@dataclass(frozen=True)
class MappingSet:
    class_fq: dict[str, str]
    class_simple: dict[str, str]
    fields: dict[str, str]
    methods: dict[str, str]


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def content_tree_sha256(input_jar: Path) -> tuple[str, int, int]:
    """Hash path + uncompressed bytes, independent of ZIP ordering/timestamps."""
    digest = hashlib.sha256()
    with zipfile.ZipFile(input_jar) as archive:
        files = sorted(
            (info.filename, archive.read(info.filename))
            for info in archive.infolist()
            if not info.is_dir()
        )
    for name, data in files:
        encoded_name = name.encode("utf-8")
        digest.update(struct.pack(">I", len(encoded_name)))
        digest.update(encoded_name)
        digest.update(struct.pack(">Q", len(data)))
        digest.update(data)
    class_count = sum(name.endswith(".class") for name, _ in files)
    return digest.hexdigest(), len(files), class_count


def verify_baseline(input_jar: Path, allow_repacked: bool) -> dict[str, object]:
    jar_digest = sha256(input_jar)
    tree_digest, file_count, class_count = content_tree_sha256(input_jar)

    if jar_digest == EXPECTED_TIDEBORNE_SHA256:
        mode = "exact-release-jar"
    elif allow_repacked and (
        tree_digest == EXPECTED_CONTENT_TREE_SHA256
        and file_count == EXPECTED_FILE_COUNT
        and class_count == EXPECTED_CLASS_COUNT
    ):
        mode = "content-identical-repacked-jar"
    else:
        raise SystemExit(
            "Refusing to reconstruct from an unrecognized Tideborne input.\n"
            f"Expected release SHA-256: {EXPECTED_TIDEBORNE_SHA256}\n"
            f"Actual JAR SHA-256:       {jar_digest}\n"
            f"Expected tree SHA-256:    {EXPECTED_CONTENT_TREE_SHA256}\n"
            f"Actual tree SHA-256:      {tree_digest}\n"
            f"Expected files/classes:   {EXPECTED_FILE_COUNT}/{EXPECTED_CLASS_COUNT}\n"
            f"Actual files/classes:     {file_count}/{class_count}"
        )

    return {
        "verification_mode": mode,
        "jar_sha256": jar_digest,
        "content_tree_sha256": tree_digest,
        "file_count": file_count,
        "class_count": class_count,
    }


def download(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.exists() and destination.stat().st_size > 0:
        return
    print(f"Downloading {url}")
    temporary = destination.with_suffix(destination.suffix + ".part")
    request = urllib.request.Request(url, headers={"User-Agent": "Tideborne-Reconstruction/1.0"})
    with urllib.request.urlopen(request, timeout=60) as response, temporary.open("wb") as output:
        shutil.copyfileobj(response, output)
    temporary.replace(destination)


def safe_zip_destination(root: Path, member_name: str) -> Path:
    target = (root / member_name).resolve()
    root_resolved = root.resolve()
    if target != root_resolved and root_resolved not in target.parents:
        raise ValueError(f"Unsafe ZIP path: {member_name}")
    return target


def extract_non_class_resources(input_jar: Path, output_root: Path) -> int:
    count = 0
    with zipfile.ZipFile(input_jar) as archive:
        for info in archive.infolist():
            name = info.filename
            if info.is_dir() or name.endswith(".class"):
                continue
            upper = name.upper()
            if upper == "META-INF/MANIFEST.MF":
                continue
            if upper.startswith("META-INF/") and upper.endswith((".SF", ".RSA", ".DSA", ".EC")):
                continue
            target = safe_zip_destination(output_root, name)
            target.parent.mkdir(parents=True, exist_ok=True)
            with archive.open(info) as source, target.open("wb") as destination:
                shutil.copyfileobj(source, destination)
            count += 1
    return count


def run_vineflower(vineflower_jar: Path, input_jar: Path, output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    command = [
        "java", "-jar", str(vineflower_jar),
        "-dgs=1",
        "-rsy=1",
        "-lit=1",
        "-asc=1",
        "-ren=0",
        "--folder",
        str(input_jar), str(output_dir),
    ]
    print("Running Vineflower...")
    subprocess.run(command, check=True)


def locate_decompiler_output(output_dir: Path) -> Path:
    java_files = list(output_dir.rglob("*.java"))
    if java_files:
        return output_dir

    produced_jars = list(output_dir.glob("*.jar"))
    if len(produced_jars) != 1:
        raise RuntimeError(
            "Vineflower output contained neither Java sources nor one decompiled JAR. "
            f"Found {len(produced_jars)} JARs."
        )
    extracted = output_dir / "decompiled"
    extracted.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(produced_jars[0]) as archive:
        archive.extractall(extracted)
    return extracted


def load_yarn_mappings(tiny_gz: Path) -> MappingSet:
    """Read Tiny v2 Yarn mappings from intermediary to named."""
    class_fq: dict[str, str] = {}
    fields: dict[str, str] = {}
    methods: dict[str, str] = {}

    with gzip.open(tiny_gz, "rt", encoding="utf-8") as handle:
        header = handle.readline().rstrip("\n").split("\t")
        if len(header) < 5 or header[0] != "tiny" or header[1] != "2":
            raise RuntimeError("Expected Tiny v2 mappings")
        namespaces = header[3:]
        try:
            intermediary_index = namespaces.index("intermediary")
            named_index = namespaces.index("named")
        except ValueError as exc:
            raise RuntimeError(f"Mappings lack intermediary/named namespaces: {namespaces}") from exc

        for raw in handle:
            line = raw.rstrip("\n")
            if not line or line.startswith("#"):
                continue
            parts = line.split("\t")
            if parts[0] == "c":
                names = parts[1:]
                if max(intermediary_index, named_index) >= len(names):
                    continue
                intermediary = names[intermediary_index]
                named = names[named_index]
                if intermediary and named:
                    class_fq[intermediary.replace("/", ".")] = named.replace("/", ".")
            elif parts[0] == "" and len(parts) >= 5 and parts[1] in {"f", "m"}:
                kind = parts[1]
                names = parts[3:]
                if max(intermediary_index, named_index) >= len(names):
                    continue
                intermediary = names[intermediary_index]
                named = names[named_index]
                if not intermediary or not named:
                    continue
                if kind == "f" and intermediary.startswith("field_"):
                    fields[intermediary] = named
                elif kind == "m" and intermediary.startswith("method_"):
                    methods[intermediary] = named

    class_simple = {
        intermediary.rsplit(".", 1)[-1]: named.rsplit(".", 1)[-1]
        for intermediary, named in class_fq.items()
    }
    if not class_fq or not methods or not fields:
        raise RuntimeError(
            f"Unexpected empty Yarn maps: {len(class_fq)} classes, "
            f"{len(methods)} methods, {len(fields)} fields"
        )
    return MappingSet(class_fq, class_simple, fields, methods)


INTERMEDIARY_TOKEN = re.compile(r"\b(?:class|method|field)_\d+(?:\$\w+)?\b")


def remap_java_text(text: str, mappings: MappingSet) -> tuple[str, int]:
    replacements = 0

    for old, new in sorted(mappings.class_fq.items(), key=lambda item: len(item[0]), reverse=True):
        count = text.count(old)
        if count:
            text = text.replace(old, new)
            replacements += count

    token_map: dict[str, str] = {}
    token_map.update(mappings.class_simple)
    token_map.update(mappings.fields)
    token_map.update(mappings.methods)

    def replace_token(match: re.Match[str]) -> str:
        nonlocal replacements
        token = match.group(0)
        replacement = token_map.get(token)
        if replacement is None:
            return token
        replacements += 1
        return replacement

    text = INTERMEDIARY_TOKEN.sub(replace_token, text)
    return text, replacements


def copy_and_remap_sources(
    decompiled_root: Path, java_output: Path, mappings: MappingSet
) -> tuple[int, int, list[str]]:
    source_files = sorted(decompiled_root.rglob("*.java"))
    if not source_files:
        raise RuntimeError("No Java sources found in Vineflower output")

    total_replacements = 0
    unresolved: set[str] = set()
    for source in source_files:
        relative = source.relative_to(decompiled_root)
        destination = java_output / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        text = source.read_text(encoding="utf-8", errors="strict")
        text, replaced = remap_java_text(text, mappings)
        total_replacements += replaced
        unresolved.update(INTERMEDIARY_TOKEN.findall(text))
        if not text.startswith("/*\n * RECONSTRUCTED SOURCE BASELINE"):
            text = PROVENANCE_HEADER + text
        destination.write_text(text, encoding="utf-8", newline="\n")

    return len(source_files), total_replacements, sorted(unresolved)


def count_class_files(input_jar: Path) -> int:
    with zipfile.ZipFile(input_jar) as archive:
        return sum(1 for name in archive.namelist() if name.endswith(".class"))


def ensure_clean_directory(path: Path, force: bool) -> None:
    if path.exists() and any(path.iterdir()):
        if not force:
            raise RuntimeError(
                f"{path} is not empty. Re-run with --force to replace generated reconstruction output."
            )
        shutil.rmtree(path)
    path.mkdir(parents=True, exist_ok=True)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path, help="Path to Tideborne 1.3.57 JAR/content-equivalent repack")
    parser.add_argument("--repo", type=Path, default=Path.cwd(), help="Repository root (default: cwd)")
    parser.add_argument("--tools", type=Path, default=None, help="Tool cache directory")
    parser.add_argument("--force", action="store_true", help="Replace existing src/main/java and resources")
    parser.add_argument("--skip-download", action="store_true", help="Require tools/mappings to already exist")
    parser.add_argument(
        "--allow-repacked",
        action="store_true",
        help="Accept a JAR whose canonical content tree exactly matches the pinned 1.3.57 baseline",
    )
    args = parser.parse_args()

    repo = args.repo.resolve()
    jar = args.jar.resolve()
    if not jar.is_file():
        parser.error(f"JAR not found: {jar}")

    baseline_verification = verify_baseline(jar, args.allow_repacked)

    tools = (args.tools or repo / ".gradle" / "tideborne-tools").resolve()
    vineflower = tools / f"vineflower-{VINEFLOWER_VERSION}.jar"
    yarn_tiny = tools / f"yarn-{YARN_VERSION}-tiny.gz"
    if not args.skip_download:
        download(VINEFLOWER_URL, vineflower)
        download(YARN_TINY_URL, yarn_tiny)
    for required in (vineflower, yarn_tiny):
        if not required.is_file():
            raise SystemExit(f"Missing required reconstruction tool: {required}")

    java_output = repo / "src" / "main" / "java"
    resources_output = repo / "src" / "main" / "resources"
    ensure_clean_directory(java_output, args.force)
    ensure_clean_directory(resources_output, args.force)

    work_root = repo / "reconstruction" / "work"
    if work_root.exists():
        shutil.rmtree(work_root)
    decompiler_output = work_root / "vineflower"

    run_vineflower(vineflower, jar, decompiler_output)
    source_root = locate_decompiler_output(decompiler_output)
    mappings = load_yarn_mappings(yarn_tiny)
    source_count, replacements, unresolved = copy_and_remap_sources(source_root, java_output, mappings)
    resource_count = extract_non_class_resources(jar, resources_output)
    class_count = count_class_files(jar)

    manifest = {
        "schema": 2,
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "baseline": {
            "tideborne_version": BASELINE_VERSION,
            "expected_release_sha256": EXPECTED_TIDEBORNE_SHA256,
            "expected_content_tree_sha256": EXPECTED_CONTENT_TREE_SHA256,
            **baseline_verification,
        },
        "tools": {
            "vineflower": VINEFLOWER_VERSION,
            "yarn": YARN_VERSION,
        },
        "output": {
            "java_files": source_count,
            "resources": resource_count,
            "class_files": class_count,
            "intermediary_replacements": replacements,
            "unresolved_intermediary_tokens": unresolved,
        },
    }
    manifest_path = repo / "reconstruction" / "manifest.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")

    print(json.dumps(manifest, indent=2))
    if unresolved:
        print(
            f"WARNING: {len(unresolved)} unresolved intermediary identifiers remain. "
            "Inspect reconstruction/manifest.json before compiling.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

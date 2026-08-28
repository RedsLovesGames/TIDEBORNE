#!/usr/bin/env python3
"""Normalize mechanical Vineflower/Yarn reconstruction artifacts.

This script is intentionally narrow. It repairs source forms that are valid bytecode
patterns but invalid or poorly inferred Java after decompilation. Gameplay behavior is
not changed here.
"""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src" / "main" / "java"

SELF_CAST_TARGETS = (
    "Screen",
    "DrawContext",
    "TidePlayerData",
    "TideFishingHook",
    "FishData",
    "ItemStack",
    "FishDisplayBlockEntity",
    "MobEntity",
)


def rewrite(path: Path, text: str) -> str:
    # Yarn 1.21.1 RegistryEntry<T>.value() was emitted by the decompiler as the
    # Kotlin/JVM component-style intermediary name comp_349().
    text = text.replace(".comp_349()", ".value()")

    # Mixin source classes are intentionally not Java subclasses of their targets.
    # The standard source-safe cast is (Target)(Object)this.
    if "/mixin/" in path.as_posix():
        for target in SELF_CAST_TARGETS:
            text = text.replace(f"({target})this", f"({target})(Object)this")

    if path.name == "TideTraitsComponents.java":
        text = text.replace("ComponentType.builder().codec(Codec.STRING)", "ComponentType.<String>builder().codec(Codec.STRING)")
        text = text.replace("ComponentType.builder().codec(Codec.LONG)", "ComponentType.<Long>builder().codec(Codec.LONG)")
        text = text.replace("ComponentType.builder().codec(Codec.DOUBLE)", "ComponentType.<Double>builder().codec(Codec.DOUBLE)")
        text = text.replace("ComponentType.builder().codec(Codec.BOOL)", "ComponentType.<Boolean>builder().codec(Codec.BOOL)")

    if path.name == "SatchelRegistration.java":
        text = text.replace(
            "ComponentType.builder().codec(NbtCompound.CODEC)",
            "ComponentType.<NbtCompound>builder().codec(NbtCompound.CODEC)",
        )

    return text


def main() -> int:
    changed = 0
    for path in sorted(JAVA.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        updated = rewrite(path, original)
        if updated != original:
            path.write_text(updated, encoding="utf-8", newline="\n")
            print(f"repaired {path.relative_to(ROOT)}")
            changed += 1

    # Vineflower emitted an enum switch helper that is not referenced by the actual
    # reconstructed ItemRenderer mixin and cannot compile as standalone Java source.
    synthetic = JAVA / "com/redslovesgames/tidetraits/mixin/client/ItemRendererMutationTintMixin$1.java"
    if synthetic.exists():
        synthetic.unlink()
        print(f"removed {synthetic.relative_to(ROOT)}")
        changed += 1

    print(f"mechanical reconstruction repairs: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

# Tideborne 1.3.57 reconstruction provenance

## Authoritative artifacts

The current source-recovery effort is based on these supplied artifacts:

### Tideborne

- file: `reconstruction/reference/Tideborne-1.3.57-perfect-catch-trait-luck.jar`
- version: `1.3.57`
- SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- Java bytecode target: Java 21

### Tide

- file: `tide-fabric-1.21.1-2.1.1.jar`
- version: `2.1.1`
- SHA-256: `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- SHA-1: `85c71628d9699aa662c063b361c5e8ba72b3ec51`

### Tide Extra Compatibility

- file: `tide-extra-compatibility-2.2.0.jar`
- used as compatibility/data reference, not as a required compile dependency for Tideborne core

## Recovered Tideborne metadata

The authoritative JAR declares:

- mod id: `tideborne`
- Minecraft: exactly `1.21.1`
- Java: `>=21`
- Fabric Loader: `>=0.18.4`
- Fabric API: `>=0.116.15+1.21.1`
- Tide: exactly `2.1.1`
- Architectury: `>=13.0.8`
- FTB Library: `>=2101.1.30`
- FTB Teams: `>=2101.1.10`
- Cloth Config: `>=15.0.140`
- suggested: Myths of the Sea 1.3.0, Apex Waters 1.1.1, Mod Menu

Entrypoints:

- common: `com.redslovesgames.tideborne.Tideborne`
- client: `com.redslovesgames.tideborne.client.TideborneClient`
- Mod Menu: `com.redslovesgames.tideborne.client.TideborneModMenu`
- GameTest: `com.redslovesgames.tidetraits.gametest.TideTraitsGameTests`

Mixin configs:

- `tide_traits.mixins.json`
- `tide_traits.client.mixins.json`
- `tide_team_journal.mixins.json`
- `tidebound_compatibility.mixins.json`
- `tidebound_compatibility.apex.mixins.json`

## Reconstruction method

The source tree should be recovered in this order:

1. Preserve a cryptographic hash of the authoritative JAR.
2. Extract all non-class resources exactly.
3. Decompile bytecode with a pinned modern Java decompiler.
4. Remap Minecraft intermediary names to the pinned Yarn 1.21.1 mapping set.
5. Compile without changing semantics.
6. Resolve decompiler artifacts by comparing bytecode signatures and runtime behavior.
7. Add characterization tests around persistence, formulas, commands, networking, and mixin behavior.
8. Refactor package internals behind those tests.
9. Only then begin new-system work.

## What decompiled source does not prove

Decompiler output is evidence, not authority. It may alter or obscure:

- local variable names
- original comments
- switch expressions
- generic signatures
- lambda structure
- synthetic bridge methods
- exact source formatting
- intent behind constants

When a decompiled method is suspicious, compare it with `javap -c -p -s -l` output from the reference JAR before changing behavior.

## Resource preservation

The 1.3.57 JAR contains authentic Tideborne assets including:

- Satchel GUI textures
- trait/mutation badge textures
- size badge textures
- fish-render mutation masks
- sort icons
- Tide hook textures
- lang files
- item models
- recipes/data
- mixin configuration

Do not redraw or replace these during reconstruction. They are part of the behavioral/visual baseline.

## License note

The repository currently contains an MIT license chosen for the source repository. The distributed 1.3.57 JAR metadata reported `All Rights Reserved`. Before a public source release, the repository license and packaged `fabric.mod.json` license field should be intentionally reconciled by the project owner rather than changed accidentally during decompilation.

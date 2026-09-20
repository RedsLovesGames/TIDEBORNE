# Recipes

[← Fishing Gear & Integrations](FISHING_GEAR_AND_INTEGRATIONS.md) | [Wiki Home](README.md) | [Configuration & Commands →](CONFIGURATION_AND_COMMANDS.md)

This page lists the crafting recipes that are actually bundled in the Tideborne 2.1.0 JAR. Optional recipes only become meaningful when their required integration mod is present. A dot below means an empty crafting-grid slot.

## Angler's Satchel

Recipe ID: `tide_traits:anglers_satchel`

```text
F F F
F S F
F F F
```

- `F` = any item in `#tide:fish`
- `S` = `tide:fish_satchel`
- Output = `tide_traits:anglers_satchel`

An existing Tide Fish Satchel can also be converted by sneak-using it and paying the server-configured XP cost.

## Myths of the Sea recipes

### Tentacle Line

Shapeless:

- Kraken Tentacle
- Tide Fishing Line
- Output: `tidebound_compatibility:tentacle_line`

### Abaia Line

Shapeless:

- Abaia Fin
- Tide Fishing Line
- Output: `tidebound_compatibility:swift_line`

The internal ID remains `swift_line` for compatibility, while the player-facing name is **Abaia Line**.

### Seafarer's Hook

Shapeless:

- Hippocampus Eye
- Tide Fishing Hook
- Output: `tidebound_compatibility:seafarers_hook`

### Kujira Bone Fishing Rod

```text
. . B
. B S
B . S
```

- `B` = `myths_of_the_sea:bake_kujira_bone`
- `S` = string
- Output: `tidebound_compatibility:kujira_bone_fishing_rod`

### Leviathan Bait

Shapeless:

- 1 `myths_of_the_sea:leviathan_heart`
- Output: **12** `tidebound_compatibility:leviathan_bait`

## Apex Waters recipes

### Chum Bucket

```text
F F F
F B F
. F .
```

- `F` = an item in `#tidebound_compatibility:shark_food`
- `B` = bucket
- Output = `tidebound_compatibility:chum_bucket`

### Copper Leader

```text
. C .
C X C
. C .
```

- `C` = copper ingot
- `X` = chain
- Output = `tidebound_compatibility:copper_leader`

### Iron Leader

```text
. I I
I C I
I I .
```

- `I` = iron nugget
- `C` = chain
- Output = `tidebound_compatibility:steel_leader`

The registry ID is historically `steel_leader`, but the current player-facing item name is **Iron Leader**.

### Gold Leader

```text
. G G
G X G
G G .
```

- `G` = gold nugget
- `X` = chain
- Output = `tidebound_compatibility:gold_leader`

### Diamond Leader

```text
. D .
D X D
. D .
```

- `D` = diamond
- `X` = chain
- Output = `tidebound_compatibility:diamond_leader`

### Shark Tooth Hook

Shapeless:

- `tidebound_compatibility:shark_tooth`
- `tide:fishing_hook`
- Output = `tidebound_compatibility:shark_tooth_hook`

## Hall Record Display

Tideborne 2.1.0 registers `tideborne:hall_record_display`, but the release JAR does **not** include a standard crafting recipe for it. This page intentionally does not invent one. A server or modpack can provide its own recipe through a data pack if desired.

## Why some IDs still use old namespaces

Modern Tideborne ships as one mod, but old resource and registry namespaces are intentionally retained when changing them could break existing worlds. Seeing `tide_traits:` or `tidebound_compatibility:` in a recipe does not mean a separate legacy JAR is required.
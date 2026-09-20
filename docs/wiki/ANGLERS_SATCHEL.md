# Angler's Satchel

[← FishScore](FISHSCORE.md) | [Wiki Home](README.md) | [Team Journal & Records →](TEAM_JOURNAL_AND_RECORDS.md)

The **Angler's Satchel** is Tideborne's collection and progression upgrade for Tide's Fish Satchel. It keeps the original idea of portable fish storage, then adds configurable capacity progression, specimen-aware sorting, Auto-Stow, record tools, and protection for valuable catches.

## Getting one

### Crafting

The 2.1.0 recipe surrounds a Tide Fish Satchel with Tide fish:

```text
F F F
F S F
F F F
```

- `F` = any item in `#tide:fish`
- `S` = `tide:fish_satchel`

See [Recipes](RECIPES.md) for the exact recipe IDs and optional integration recipes.

### Converting an existing Fish Satchel

Tideborne also supports converting a Tide Fish Satchel into an Angler's Satchel by **sneak-using** it. The conversion charges a server-configured amount of raw experience points. The standard 2.1.0 default is **100 XP**.

## Capacity progression

Capacity is expressed as a multiplier of Tide's resolved base Fish Satchel capacity. Tideborne does not assume a fixed base slot count.

| Level | Default multiplier | Default XP cost |
| --- | ---: | ---: |
| Base | 1.0x | 0 |
| Capacity I | 1.5x | 150 XP |
| Capacity II | 2.0x | 450 XP |
| Capacity III | 3.0x | 1000 XP |

These values are configurable by the server.

## Feature upgrades

The 2.1.0 JAR defines six Satchel features. Their standard costs are raw vanilla XP points, not experience levels.

| Feature | Default cost | Purpose |
| --- | ---: | --- |
| Tackle Organizer | 100 XP | Unlocks advanced sorting and organization |
| Auto-Stow | 200 XP | Automatically routes eligible catches into the active Satchel |
| Record Keeper | 250 XP | Adds record-oriented Satchel functionality |
| Trait Scanner | 300 XP | Adds more specimen and trait information |
| Trophy Lock | 350 XP | Protects catches that match configured trophy rules |
| Shared Ledger | 400 XP | Connects Satchel progression to the team-record system |

Shared Ledger depends on Tideborne's team functionality being available.

## Sorting

The JAR exposes these specimen-aware sort keys:

- Alphabetical
- Mutation Rarity
- Percentile
- Rarity
- Region
- Size

Sorting can be combined with direction settings in the Satchel interface.

## Auto-Stow

Auto-Stow sends eligible catches to the player's active Angler's Satchel. Tideborne tracks whether a Satchel is active for this purpose, and the client UI reports when a Satchel becomes active or inactive.

Auto-Stow does not make the client authoritative over inventory state. Insertion and relevant validation remain server-side.

## Trophy Lock

Once Trophy Lock is purchased, the Satchel can automatically protect catches based on configured evidence. The default protection categories include:

- unusual/mutated specimens
- trophy-size catches
- legendary-size catches
- personal largest records
- personal smallest records
- Tide legendary-rarity fish

Protection is intended to prevent accidental loss or replacement of notable catches, not to change the specimen itself.

## Personal records

Tideborne can track personal largest and smallest catches and surface new-record messages when a specimen beats an existing record. These personal records are separate from team-owned records in the [Team Journal](TEAM_JOURNAL_AND_RECORDS.md).

## What the 2.1.0 Satchel does not include

The uploaded Tideborne 2.1.0 JAR does **not** contain the later development-only physical multi-preset tackle manager with up to 12 named rod/line/hook/bobber/bait/leader presets. That system appears in newer repository development notes, so it is intentionally excluded from this release wiki.

## Server configuration

Capacity multipliers, XP costs, conversion behavior, protection rules, and related Satchel settings are controlled by Tideborne's configuration. See [Configuration & Commands](CONFIGURATION_AND_COMMANDS.md).
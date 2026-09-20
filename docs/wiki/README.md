# Tideborne Wiki

> **Documentation target:** Tideborne 2.1.0 · Minecraft 1.21.1 · Fabric · Tide 2.1.1
>
> Verified against the shipped `tideborne-2.1.0.jar` (`SHA-256 cba4289f989d435aadb997e003019e2dc2c3c0068bef06e830177ab83d4c82f3`).

**Tide 2 does the fishing. Tideborne adds the specimen.** Tideborne turns individual Tide catches into persistent specimens with size, traits, FishScore, personal and team records, collection tools, and optional ocean-mod integrations. It is designed for players who want fishing to become a long-term collection and record-hunting system instead of a one-time journal checklist.

## Start here

| Page | Use it for |
| --- | --- |
| [Getting Started](GETTING_STARTED.md) | Requirements, installation, first steps, and what Tideborne changes |
| [Specimens & Traits](SPECIMENS_AND_TRAITS.md) | Body Type, Condition, Pigmentation, Perfect Specimens, Trait Luck, and Momentum |
| [FishScore](FISHSCORE.md) | Exact 1-3000 scoring formula and point values |
| [Angler's Satchel](ANGLERS_SATCHEL.md) | Crafting, conversion, capacity, upgrades, sorting, Auto-Stow, and Trophy Lock |
| [Team Journal & Records](TEAM_JOURNAL_AND_RECORDS.md) | FTB Teams sharing, leaderboards, history, record ownership, and `/ttj` commands |
| [Hall Record Display](HALL_RECORD_DISPLAY.md) | Personal and team history displays in the world |
| [Fishing Gear & Integrations](FISHING_GEAR_AND_INTEGRATIONS.md) | Tide gear behavior plus Myths of the Sea and Apex Waters content |
| [Recipes](RECIPES.md) | Tideborne 2.1.0 crafting recipes and optional-mod recipes |
| [Configuration & Commands](CONFIGURATION_AND_COMMANDS.md) | `config/tideborne.json`, player commands, and operator tools |
| [Updating & Compatibility](UPDATING_AND_COMPATIBILITY.md) | Moving from older Tideborne modules without breaking worlds |
| [FAQ](FAQ.md) | Quick answers to common questions |

## The core loop

1. **Catch** a Tide fish under Tide's normal habitat and eligibility rules.
2. **Generate** one server-authoritative specimen with a persistent size roll and independent trait axes.
3. **Fight** the fish through Tide's fishing minigame with Tideborne's canonical Strength and Tempo adjustments.
4. **Score** the specimen with FishScore V2.
5. **Keep** noteworthy fish in the Angler's Satchel, personal history, team records, or Hall Record Display.

## What Tideborne owns

Tideborne 2.1.0 provides the modern implementations of the systems historically distributed as Tide Traits, Tide Team Journal, and Tidebound Compatibility. The single JAR declares compatibility aliases for `tide_traits`, `tide_team_journal`, and `tidebound_compatibility`, so older serialized IDs can remain stable while the player-facing mod is Tideborne.

## Version boundaries

This wiki documents what is actually present in **Tideborne 2.1.0**. Development-only features that are not in the 2.1.0 JAR are intentionally excluded. In particular, the later physical multi-preset tackle manager described in newer development notes is not part of this 2.1.0 player wiki.

## External reference pages

The older public pages are still useful references while this wiki replaces them:

- [Tideborne overview](https://redslovesgames.github.io/random-info-pages/tideborne/)
- [Fishing System 2.0 reference](https://redslovesgames.github.io/random-info-pages/tide2/)
- [Legacy Tideborne 1.3.57 docs](https://redslovesgames.github.io/Tide-2-Addons/)

For current player documentation, prefer the pages in this folder.
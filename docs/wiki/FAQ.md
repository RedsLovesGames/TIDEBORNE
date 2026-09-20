# FAQ

[← Updating & Compatibility](UPDATING_AND_COMPATIBILITY.md) | [Wiki Home](README.md)

## Does Tideborne replace Tide?

No. Tideborne 2.1.0 requires **Tide 2.1.1**. Tide provides the core fishing content and species ecosystem. Tideborne adds persistent specimens, traits, scoring, records, storage progression, team systems, and integrations.

## Is Tideborne multiplayer-only?

No. The specimen, FishScore, Satchel, personal-record, and history systems are useful in singleplayer. Multiplayer adds the FTB Teams-based shared Journal and team-record experience.

## Why are FTB Library and FTB Teams required in singleplayer?

They are hard dependencies declared by the 2.1.0 JAR because the team and Journal infrastructure is part of the unified mod even when you are not actively using a multiplayer party.

## Do I need Myths of the Sea or Apex Waters?

No. They are optional integrations. Tideborne's core systems run without either mod.

## Can a fish have more than one rare trait?

Yes. Body Type, Condition, Pigmentation, and Specimen Quality are independent axes. A specimen can be Giant + Scarred + Iridescent + Perfect Specimen at the same time.

## Is Perfect Catch the same as Perfect Specimen?

No. **Perfect Catch** is a fishing-performance result. **Perfect Specimen** is a specimen-quality trait. A Perfect Catch can improve the opportunity for unusual traits and a Perfect Specimen, but they are not the same flag.

## Does Giant mean the fish got a second size roll?

No. Tideborne generates one natural size percentile. Giant or Dwarf then changes the physical result with a deterministic size multiplier. The canonical system avoids repeatedly rerolling the natural specimen.

## What is the difference between Fishing Luck and Trait Luck?

Fishing Luck biases which Tide species is selected. Trait Luck changes the probability of unusual specimen traits after the species has been selected.

## What is the maximum FishScore?

**3000.** See [FishScore](FISHSCORE.md) for the exact formula and point tables.

## Does holding a team record increase FishScore?

No. Record ownership and FishScore are separate systems. FishScore comes from species rarity, final percentile, and specimen traits.

## Can a Dwarf have a high FishScore?

Yes. Dwarf is worth the same Body Type points as Giant, and FishScore also considers rarity, final percentile, condition, pigmentation, and quality. Size-record competition and FishScore competition are intentionally different.

## How do I get an Angler's Satchel?

Craft one by surrounding a Tide Fish Satchel with eight Tide fish, or sneak-use a supported Tide Fish Satchel to convert it for the server-configured XP cost. See [Angler's Satchel](ANGLERS_SATCHEL.md).

## Are Satchel XP costs experience levels?

No. The costs are raw vanilla experience points.

## Does Tideborne 2.1.0 have the 12-preset tackle manager?

No. That system appears in later development notes, but the uploaded 2.1.0 release JAR does not contain those preset classes. This wiki intentionally documents the shipped 2.1.0 behavior instead of advertising development-only features.

## How do I open Team Records?

Use `/ttj`, `/ttj open`, or `/tideborne journal`.

## Why does the Iron Leader use `steel_leader` internally?

Compatibility. Its current player-facing name is **Iron Leader**, but the historical registry ID `tidebound_compatibility:steel_leader` is preserved so old worlds and serialized references do not break.

## Why do I still see `tide_traits:` and `tide_team_journal:` IDs?

Modern Tideborne consolidated the older modules into one mod, but changing saved registry, component, recipe, NBT, or network IDs can break existing worlds. The old namespaces remain where compatibility needs them.

## Can I install the old standalone Tide Traits or Team Journal JARs too?

No. Remove old standalone modules when using modern Tideborne. Tideborne 2.1.0 already declares compatibility aliases for the historical systems.

## Where is the config file?

`config/tideborne.json`

See [Configuration & Commands](CONFIGURATION_AND_COMMANDS.md).

## Why can I not craft the Hall Record Display?

The 2.1.0 JAR registers the block but does not ship a standard JSON crafting recipe for it. A server or modpack can add one, or operators can provide access through normal creative/admin methods.

## Does Leviathan Bait ignore habitat requirements?

No. It guarantees a Tide fish instead of junk, treasure, or crates and improves fish-selection luck, but unusual fish still require their normal habitat and eligibility conditions.

## Why did a shark take my catch?

With Apex Waters integration enabled, sharks can participate in Tideborne's catch-loss system. Leaders provide increasing protection: Copper 30%, Iron 55%, Gold 75%, Diamond 95%.

## Where should I report bugs?

Use the Tideborne repository issue tracker and include your Tideborne, Tide, Minecraft, Fabric, and optional-mod versions plus the relevant log. For migration issues, keep a backup and include which older Tideborne version the world came from.
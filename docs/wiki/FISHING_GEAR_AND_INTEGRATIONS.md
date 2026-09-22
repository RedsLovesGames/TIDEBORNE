# Fishing Gear & Integrations

[← Hall Record Display](HALL_RECORD_DISPLAY.md) | [Wiki Home](README.md) | [Recipes →](RECIPES.md)

Tideborne does not replace Tide's equipment system. It reads Tide's supported rods, lines, hooks, bobbers, and bait through its canonical fishing-gear registry, then applies Tideborne's species, specimen, fight, and compatibility effects in one server-authoritative pipeline.

## Supported Tide gear

The 2.1.0 gear registry recognizes Tide's standard rod progression, base line and hook, specialty hooks, multiple Tide lines and bait types, and the supported Tide bobber set. Tideborne uses exact namespaced gear identity rather than relying on display names.

The important player-facing distinction is that gear effects can influence different stages:

- species-selection Fishing Luck
- specimen Trait Luck
- fight Strength and Tempo
- compatibility mechanics such as shark catch-loss protection

A bonus to one stage should not be assumed to affect all other stages.

## Myths of the Sea integration

When **Myths of the Sea 1.3.0** is installed, Tideborne exposes additional fishing equipment built from its creatures and materials.

### Tentacle Line

Crafted from a Kraken Tentacle and Tide Fishing Line. The in-game description calls it a line that seems to reach farther than it should.

### Abaia Line

The player-facing item is the **Abaia Line**. Its preserved internal registry ID is `tidebound_compatibility:swift_line` for compatibility with older worlds. It is crafted from an Abaia Fin and Tide Fishing Line.

### Seafarer's Hook

Crafted from a Hippocampus Eye and Tide Fishing Hook.

### Kujira Bone Fishing Rod

A specialized rod crafted from baked Kujira Bone and string.

### Leviathan Bait

Crafted from a Leviathan Heart, producing 12 bait. In the canonical Fishing System 2 gear path, Leviathan Bait:

- restricts the Tideborne catch category to **fish**, preventing junk, treasure, or crates from being selected through that path
- adds **+4 Fishing Luck** to canonical species weighting
- adds **+1 Trait Luck** to canonical specimen generation
- applies **1.30x Strength** and **1.20x Tempo** to the fight profile
- applies a **2.00x boss target-weight multiplier** in the canonical gear modifier object
- does not bypass the fish's normal habitat or eligibility conditions

Historical configuration and audit fields for older Leviathan tuning are still retained for compatibility. The values above describe the current Fishing System 2 modifier object used by the canonical gear path rather than those older tuning snapshots.

Myths of the Sea is optional. Tideborne must continue to run without resolving its classes when the mod is absent.

## Apex Waters integration

When **Apex Waters 1.1.1** is installed, Tideborne adds shark-related fishing risk and equipment.

### Chum Bucket

Chum is an Apex-oriented ocean mechanic. In 2.1.0 it only functions when Apex compatibility is active and must land in ocean water. If it does not land in valid water, the bucket is returned rather than silently consumed.

### Leaders

Leaders protect a caught fish from Apex-related shark catch loss. The item descriptions expose the protection values directly:

| Leader | Catch-loss protection | Tradeoff |
| --- | ---: | --- |
| Copper Leader | 30% | Very small handling cost |
| Iron Leader | 55% | Moderate handling cost |
| Gold Leader | 75% | Meaningful handling cost |
| Diamond Leader | 95% | Hardest handling |

The **Iron Leader** intentionally retains the historical registry ID `tidebound_compatibility:steel_leader`. Do not rename the ID in commands, data packs, or migrations just because the player-facing name is Iron Leader.

When a leader prevents the Apex catch-loss event, Tideborne reports that the leader held through the bite.

### Shark Tooth Hook

Crafted from a Great White Shark Tooth and Tide Fishing Hook.

## Optional integration rule

Myths of the Sea and Apex Waters are suggested dependencies, not hard requirements. Content that depends on either integration is hidden or inactive when the corresponding mod is not installed. The core specimen, FishScore, Satchel, Journal, and record systems do not require either optional mod.

## Recipes

All 2.1.0 crafting layouts for Tideborne-owned integration items are listed on [Recipes](RECIPES.md).
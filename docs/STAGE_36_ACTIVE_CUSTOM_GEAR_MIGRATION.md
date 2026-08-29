# Stage 36 - Active rods and Tideborne fishing modifier migration

Stage 36 audits the currently active runtime rod, hook, and custom Tideborne fishing effects and routes those effects through the canonical `FishingGearModifiers` model. Dead or superseded gear behavior is not revived.

## Active runtime gear audited

The active Tideborne-owned modifier surface is:

- Tentacle Line
- Swift Line
- Steel Leader, already migrated in Stage 35
- Seafarer's Hook
- Shark Tooth Hook
- Kujira Bone Fishing Rod

Leviathan Bait remains active runtime content but its Fishing System 2.0 luck and fight redesign is intentionally excluded from this stage because it has a dedicated later migration slice. Chum and plain Shark Tooth items do not currently provide fishing gear modifiers. No active `selection_quality` caller was found and no dead version of that behavior was restored.

## Canonical effect representation

`FishingGearEffects` now provides named multiplier axes for:

- `catch_zone_area`
- `minigame_speed`
- `fish_weight`
- `crate_weight`

These are separate from the first-class canonical `FishingGearModifiers.fishingLuck`, `traitLuck`, `strengthMultiplier`, and `tempoMultiplier` fields. Stage 36 does not reinterpret a weight bonus or minigame multiplier as Fishing Luck or Trait Luck.

`TideborneFishingGearModifiers` is the runtime adapter for the active Tideborne gear above. Optional compatibility remains gated by the existing `enableMythsCompat` and `enableApexCompat` configuration flags and the adapter imports no optional Myths of the Sea or Apex Waters classes.

## Preserved gameplay values

The migration preserves the existing configured behavior exactly:

| Gear | Canonical effect | Current default |
| --- | --- | ---: |
| Tentacle Line | catch-zone area | x1.45 |
| Tentacle Line | minigame speed | x1.18 |
| Swift Line | catch-zone area | x1.20 |
| Swift Line | minigame speed | x1.05 |
| Steel Leader | Stage 35 fallback behavior | x0.90 area, x1.05 speed, configured loss protection |
| Shark Tooth Hook | predatory or large fish weight | x2.50 |
| Shark Tooth Hook | very-small fish weight | x0.35 |
| Seafarer's Hook | eligible legendary ocean-night fish weight | x1.35 |
| Kujira Bone Fishing Rod | ocean crate weight | x1.20 |

The Shark Tooth Hook's two historical checks remain independent. If a fish satisfies both the predatory/large and very-small tag conditions, the canonical multiplier is `2.50 * 0.35 = 0.875`, matching the previous runtime arithmetic.

## Fight and line ordering

The canonical `FightProfile` remains the base fight calculation.

The minigame path now applies Tide's built-in line effects through the existing Stage 34 `TideFishingLineModifiers` adapter rather than repeating raw arithmetic in `FishingModifiers`:

- Copper: Tempo x0.90
- Iron: Strength x0.86
- Golden: Tempo x0.95
- Diamond: Strength x0.75

After the canonical fight plus built-in Tide line layer, Tideborne custom minigame modifiers preserve their existing branch precedence:

1. Tentacle Line
2. Swift Line
3. Steel Leader fallback

Leviathan Bait's existing selected-fish minigame adjustment remains after those modifiers and is unchanged in Stage 36.

## Luck separation

None of the currently active migrated Tentacle, Swift, Steel Leader, Shark Tooth Hook, Seafarer's Hook, or Kujira effects creates Fishing Luck or Trait Luck. Regression tests explicitly compose these effects with independent Fishing Luck and Trait Luck values and prove the two luck axes remain unchanged.

This is intentional. A later dedicated Leviathan Bait stage may add Fishing Luck and Trait Luck using the proper first-class canonical fields rather than a named weight modifier.

## Removed duplicate runtime arithmetic

After migration:

- `FishingModifiers.modifyFishWeight` consumes only the canonical fish-weight modifier result.
- `FishingModifiers.modifyCrateWeight` consumes only the canonical crate-weight modifier result.
- `FishingModifiers.modifyMinigame` consumes the Stage 34 built-in Tide line adapter plus the canonical Tideborne minigame modifier result.
- direct Tentacle, Swift, Shark Tooth Hook, Seafarer's Hook, Kujira, and built-in Tide line multiplier arithmetic has been removed from those callers.
- Steel Leader remains resolved through its Stage 35 adapter.

Attachment/storage/UI checks that do not calculate gameplay modifiers are not removed.

## Regression coverage

`TideborneFishingGearModifiersTest` covers:

- exact Tentacle Line area/speed values
- exact Swift Line area/speed values
- Tentacle > Swift > Steel Leader precedence
- exact Shark Tooth predatory/large and very-small multipliers
- exact overlapping Shark Tooth multiplier of 0.875
- exact Seafarer's Hook weight multiplier
- exact Kujira ocean crate multiplier
- neutral behavior when gear is absent, conditions are ineligible, or the relevant compatibility flag is disabled
- preservation of independent Fishing Luck and Trait Luck
- preservation of independent canonical Strength and Tempo when named Tideborne effects compose with other gear

The existing `TideFishingLineModifiersTest` continues to freeze Tide 2.1.1's four built-in line factors.

## Validation

Implementation commit: `930b40f82f7ccf5f7c9ce2168402e3bba3122b72`

GitHub Actions run `33244379590` completed successfully with:

- exact external dependency fetch
- reconstruction identifier validation
- clean Gradle build and unit tests
- Fabric GameTests
- built JAR artifact upload

## Stage boundary

Stage 36 migrates only currently active rod/hook/line modifier behavior identified by the audit. It does not implement the later Leviathan Bait Fishing Luck, Trait Luck, Strength, Tempo, or fish-only catch-pool redesign, and it does not revive dead legacy modifier paths.

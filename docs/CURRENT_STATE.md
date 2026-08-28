# Current development state

Updated: 2026-08-28

## Baseline and branch

- compatibility baseline: Tideborne 1.3.57
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- reconstruction branch: `reconstruct-1.3.57`
- `main` remains untouched at `41e53b052660e04e546b07b305c5047b3f646675`
- authoritative redesign contract: `docs/FISHING_SYSTEM_2_SPEC.md`

## Phase 0 is complete

The exact Tideborne 1.3.57 release artifact was recovered, verified, reconstructed into maintained source/resources, mechanically normalized, and built successfully.

Frozen verification anchors:

- release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- canonical files: 442
- canonical `.class` files: 272
- canonical non-class resources: 170
- reconstructed Java source files: 187

The reconstruction workflow verifies the exact release input before decompilation. The old incomplete Base64 reconstruction chunks are no longer part of the recovery path.

## Green reconstructed baseline

GitHub Actions build run `33152569860` successfully completed the repository build step and artifact upload for commit `e06387aa1df731ef2ae16059c6418e78f046c341`.

The workflow runs:

```text
./gradlew clean build --stacktrace
./gradlew runGametest --stacktrace
```

against Java 21 with exact external compile dependencies fetched in CI:

- Tide 2.1.1 Fabric 1.21.1, SHA-256 `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Apex Waters 1.1.1 Fabric 1.21.1, SHA-256 `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`

The reconstructed baseline was fast-forwarded onto `dev`. No merge conflict or history rewrite was required because the reconstruction work is a direct descendant of the previous `dev` head.

## Fishing System 2.0 execution state

Specification execution steps 1 through 4 are complete in the pure V2 domain layer and are now runtime-integrated on `dev`.

Pure V2 steps:

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. species selection and rarity-aware Fishing Luck
3. canonical `SpecimenData`, direct percentile, and size math
4. fight normalization and size fight scaling

Implementation root:

```text
src/main/java/com/redslovesgames/tideborne/fishing/v2/
```

The deterministic pure V2 suite remains 17 of 17 passing tests.

## Steps 1 through 4 runtime integration is green

Runtime validation completed on `dev` with GitHub Actions run `33159465388`. Both the clean Gradle build and Fabric GameTests passed.

Verified runtime contracts:

- `FishSelectorMixin` replaces only Tide `FishSelector#getResult`. Tide's top-level `TideFishingManager` category selector and `FishSelector.weight(context) = 85` remain unchanged, so ordinary Tide fish versus junk, crate, and treasure probability is unchanged.
- `TideSpeciesProfileAdapter` preserves Tide `shouldKeep` eligibility and existing fishing/compatibility weight modifiers while omitting the superseded legacy `selection_quality` adjustment from the canonical species-selection path.
- one server-owned Tide fishing context produces one canonical catch seed, one V2 species selection, and one `SpecimenGenerator` canonical generation call.
- `SpecimenGenerator` samples one canonical natural percentile and one matching base length, then deterministic trait finalization may transform physical size without another natural sample.
- the V2 bridge intentionally does not call Tide `FishData#getResult`, preventing Tide's independent `SizeData#getRandomLength` roll from becoming a hidden second natural size roll.
- `CanonicalSpecimenStorage` persists canonical specimen identity and mirrors the canonical final percentile/length into compatibility components before downstream catch handling.
- the canonical `FightProfile` explicitly drives minigame behavior, strength, tempo, and catch-zone baseline values.
- existing Tide copper, iron, golden, and diamond line effects still layer on top of the canonical fight baseline.
- existing Tideborne Tentacle Line, Swift Line, Steel Leader, Leviathan Bait, Perfect Catch skill detection, and related compatibility hooks remain in their current integration positions unless a later specification step replaces their behavior.
- canonical specimen components survive the current item-to-entity-to-item and bucket transfer paths. Full source stack serialization preserves canonical components, and the specimen transfer hooks preserve the compatibility mirrors.
- the invalid Mixin 0.8.7 injector on the `Bucketable` interface was replaced by a concrete `FishEntity#copyDataToStack` hook, allowing the GameTest server to start while preserving entity-to-bucket specimen transfer.
- legacy `CatchTraitService` natural-size sampling is bypassed for canonical specimens because canonical storage has already supplied the compatibility identity/seed/percentile state.
- legacy `PerfectCatchTraitBoost` now refuses to rewrite percentile or fish length when canonical specimen identity is present. The actual Fishing System 2.0 Perfect Catch reward redesign remains Step 8.

Runtime GameTests cover:

1. canonical specimen item/entity representation round trip
2. canonical specimen immunity to the legacy catch individualizer reroll path
3. canonical specimen immunity to the legacy Perfect Catch percentile/length rewrite

A focused search of the migrated runtime path found no remaining duplicate species selection, natural percentile, natural size, fight strength, or fight tempo calculation used by canonical V2 catches. Legacy sampled-size and trait paths still exist as guarded fallback compatibility for old/noncanonical catches and should remain until their stored-data callers are migrated.

## Deterministic trait RNG splitting

A stateless V2 `TraitRandom` utility is defined inside the Fishing System 2.0 package for specimen trait axes.

The utility:

- derives each trait decision directly from the canonical specimen seed plus a fixed salt
- exposes deterministic unit doubles in `[0, 1)` using the upper 53 bits of a mixed 64-bit value
- uses no mutable or shared RNG state
- gives Body Type event, Body Type variant, Body Type physical size, Condition, Pigmentation, and Perfect Specimen decisions reserved stable salts
- keeps event, variant, and physical-size decisions on separate salts
- guarantees that evaluating or adding an unrelated future salt does not consume state or shift existing outcomes

## Step 5 Body Type is complete

The V2 Body Type implementation now covers probability selection, canonical physical-size finalization, and canonical fight-profile modification.

Frozen behavior:

- canonical values are `NORMAL`, `GIANT`, and `DWARF`
- the Body Type event probability is exactly 5%; a failed event returns `NORMAL`
- the event and variant decisions use the reserved `TraitRandom.Salts.BODY_TYPE_EVENT` and `BODY_TYPE_VARIANT` streams
- after an event, Giant probability is `0.25 + 0.50 * (naturalPercentile / 100.0)`
- Giant therefore rises smoothly from 25% of Body Type events at P0 to 75% at P100, with 50% at P50
- there is no hard percentile threshold; Giant remains possible at low percentile and Dwarf remains possible at high percentile
- Giant physical size is sampled uniformly and deterministically from 1.10x to 1.30x using the independent `BODY_TYPE_SIZE` stream
- Dwarf physical size is sampled uniformly and deterministically from 0.60x to 0.82x using the same dedicated Body Type size stream
- Normal physical size is exactly 1.0x
- Body Type finalization always starts from canonical `baseLength`; it never rerolls or stacks a second base-size sample
- `basePercentile` remains the one natural specimen percentile generated with `baseLength`
- `finalLength` is `baseLength * bodyTypeSizeMultiplier`
- for species with a physical size distribution, `finalPercentile` is the deterministic CDF percentile of `finalLength`; it is size-adjusted, not independently sampled
- species represented by `NoPhysicalSizeDistribution` retain `finalPercentile == basePercentile` because there is no meaningful physical-size percentile to derive
- Body Type selection and physical size do not consume or depend on Condition, Pigmentation, or Quality streams
- the Tide V2 species bridge calls full `SpecimenGenerator.generate`, which performs exactly one base specimen sample and then applies Body Type finalization before canonical storage and fight-profile creation
- `FightProfileService` first computes the existing normalized species fight values and percentile fight scaling, then applies the Body Type modifiers to that already-computed canonical profile
- Giant applies Strength x1.08 and Tempo x0.95
- Dwarf applies Strength x0.92 and Tempo x1.08
- Normal applies no fight modification
- Body Type tempo reuses the existing final Tempo clamp, and catch-zone area is recalculated from the Body Type-adjusted canonical Strength through the existing bounded catch-zone model
- no second fight calculation path was introduced; `TideSpeciesSelectionBridge` still creates the one canonical `FightProfile`, and `FishingModifiers.modifyMinigame` still consumes that stored profile before layering Tide line effects and Tideborne compatibility effects

This interpretation of `finalPercentile` is explicit in `docs/FISHING_SYSTEM_2_SPEC.md`: the separate base and final size pairs exist so the natural specimen identity remains frozen while deterministic physical modifiers can change the final measured percentile without introducing a second random specimen roll.

Deterministic tests cover exact 5% configuration, repeatability, approximately 5% sampled event frequency, P75 versus P25 Giant bias, both variants across the percentile range, P50 balance, the documented bias formula, independence from other trait streams, physical multiplier bounds, deterministic multiplier values, exact Normal identity, Giant/Dwarf size direction, preserved base percentile, size-adjusted final percentile, no-physical-size fallback, exactly one base-size quantile sample during complete generation, exact Normal/Giant/Dwarf fight multiplier comparisons for otherwise identical specimens, preserved percentile fight scaling, catch-zone recomputation, and unchanged behavior.

Body Type fight implementation is commit `72c98ea3d3f79161d97720a5833d47eb4e2f4a33`. Validation is green with GitHub Actions run `33163133061`: `./gradlew clean build --stacktrace`, unit tests included by the Gradle build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Current execution gate

Steps 1 through 5 are complete for the implemented V2 pipeline, with Steps 1 through 4 runtime integration and the complete Body Type slice green on `dev`.

Do not begin Trait Luck, Perfect Catch redesign, Perfect Specimen, or FishScore V2 ahead of their queued slices. The next Step 6 slice is the independent Condition and Pigmentation axes.

## Later frozen slices

Execute in this order:

1. independent Condition and Pigmentation axes
2. Trait Luck, rarity compensation, and per-species Momentum
3. Perfect Catch redesign and percentile-based Perfect Specimen
4. FishScore V2 with the canonical linear 1 to 3000 mapping
5. deterministic migration and canonical `SpecimenData` adoption across persistence/UI/network systems
6. gear and Leviathan Bait progression
7. compatibility SpeciesProfiles
8. legacy cleanup
9. final build, migration, optional-mod, and runtime validation

See `docs/TODO.md` for checkbox-level execution state and `docs/FISHING_SYSTEM_2_SPEC.md` for frozen formulas and compatibility contracts.

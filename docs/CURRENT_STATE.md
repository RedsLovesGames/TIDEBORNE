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
- one server-owned Tide fishing context produces one canonical catch seed, one V2 species selection, and one `SpecimenGenerator.generateBase` call.
- `SpecimenGenerator` produces one canonical natural percentile and one canonical final length for the current Steps 1 through 4 model.
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

## Current execution gate

Steps 1 through 4 are runtime-integrated and green. Do not redo reconstruction or broaden cleanup before the next frozen slice.

The exact next implementation slice is Step 5: Body Type.

Do not begin Condition, Pigmentation, Trait Luck, Perfect Catch redesign, Perfect Specimen, or FishScore V2 until the preceding frozen slices are complete.

## Later frozen slices

Execute in this order:

1. Body Type
2. independent Condition and Pigmentation axes
3. Trait Luck, rarity compensation, and per-species Momentum
4. Perfect Catch redesign and percentile-based Perfect Specimen
5. FishScore V2 with the canonical linear 1 to 3000 mapping
6. deterministic migration and canonical `SpecimenData` adoption across persistence/UI/network systems
7. gear and Leviathan Bait progression
8. compatibility SpeciesProfiles
9. legacy cleanup
10. final build, migration, optional-mod, and runtime validation

See `docs/TODO.md` for checkbox-level execution state and `docs/FISHING_SYSTEM_2_SPEC.md` for frozen formulas and compatibility contracts.

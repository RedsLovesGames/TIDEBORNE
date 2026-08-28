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
```

against Java 21 with exact external compile dependencies fetched in CI:

- Tide 2.1.1 Fabric 1.21.1, SHA-256 `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Apex Waters 1.1.1 Fabric 1.21.1, SHA-256 `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`

Uploaded build artifact digest:

- `sha256:7000d9527a11f0b894d068d489d54fc8ebcd12f29161155168e3e91499cbcdbd`

The reconstructed baseline was then fast-forwarded onto `dev`. No merge conflict or history rewrite was required because the reconstruction work is a direct descendant of the previous `dev` head.

## Fishing System 2.0 execution state

Specification execution steps 1 through 4 exist and are tested in the pure V2 domain layer:

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. species selection and rarity-aware Fishing Luck
3. canonical `SpecimenData`, direct percentile, and size math
4. fight normalization and size fight scaling

Implementation root:

```text
src/main/java/com/redslovesgames/tideborne/fishing/v2/
```

The pure V2 layer currently contains:

- `CanonicalRarity`
- `FishingContext`
- `FishingEnvironment`
- `SpeciesEligibility`
- `SpeciesProfile`
- `SpeciesSelectionService`
- `SizeDistribution`
- `LogNormalSizeDistribution`
- `NormalDistributionMath`
- `SpecimenData`
- `SpecimenGenerator`
- `FightProfile`
- `FightProfileService`

The earlier deterministic V2 suite passed 17 of 17 tests. The full reconstructed project now also compiles and packages successfully.

## Current execution gate

The next work is runtime integration of V2 steps 1 through 4. Do not implement later mechanics on top of the old catch pipeline first.

Runtime integration order:

1. adapt Tide fish data into canonical `SpeciesProfile`
2. build one server-owned canonical `FishingContext` per catch attempt
3. use `SpeciesSelectionService` inside the fish pool while preserving Tide's overall fish/non-fish category probability
4. generate the base canonical specimen once with `SpecimenGenerator`
5. derive the normalized `FightProfile`
6. feed those values into the existing Tide minigame without redesigning the minigame
7. persist enough canonical specimen state to prevent downstream rerolls
8. add deterministic adapter/integration tests
9. remove superseded legacy rarity/selection/percentile/fight calculations only after their callers migrate

Known legacy integration points already identified:

- `tidetraits/mixin/TideFishingHookMixin.java` currently individualizes catches and assigns legacy specimen traits before the minigame flow finishes
- `tideboundcompatibility/mixin/TideFishingHookMixin.java` currently redirects Tide catch selection for Leviathan Bait and applies the old post-fight Perfect Catch trait boost
- `tideboundcompatibility/mixin/FishCatchMinigameMixin.java` currently modifies minigame behavior, catch-zone area, and speed
- Tide's `TideFishingManager.selectCatch(FishingContext)` delegates to its random selector, so V2 integration must replace/reweight only the fish-species choice rather than changing the overall fish category chance

## Later frozen slices

After runtime steps 1 through 4 are green, execute in this order:

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

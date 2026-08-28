# Current development state

Updated: 2026-08-28

## Baseline and branch

- compatibility baseline target: Tideborne 1.3.57
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- `main` remains untouched at `41e53b052660e04e546b07b305c5047b3f646675`
- authoritative redesign contract: `docs/FISHING_SYSTEM_2_SPEC.md`

## Fishing System 2.0 run boundary

This run stopped after specification execution steps 1 through 4:

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. species selection and Fishing Luck
3. `SpecimenData`, direct percentile, and size math
4. fight normalization and size fight scaling

Body Type generation, independent traits, Trait Luck, Momentum, Perfect Catch changes, Perfect Specimen, FishScore V2, migration, UI integration, and progression were not started.

## Verified implementation status

| Slice | Status | Verification |
|---|---|---|
| Authoritative Fishing System 2.0 specification | Complete | Persisted in `docs/FISHING_SYSTEM_2_SPEC.md` |
| Step 1 domain models and canonical rarity | Domain layer complete | Immutable `FishingContext`, canonical 1 through 5 star rarity, compatibility-ready eligibility/environment boundary, and `SpeciesProfile` |
| Step 2 species selection and Fishing Luck | Domain layer complete | Rarity-aware weighted selection implements `W' = W * (1 + C_R * sqrt(max(L, 0)))`; selection always returns one eligible fish and does not alter overall fish-catch probability |
| Step 3 specimen data and exact size math | Domain layer complete | Immutable canonical `SpecimenData`, deterministic seeded base generation, and direct lognormal CDF and quantile calculations replace sampled lookup behavior in the new layer |
| Step 4 fight normalization and size scaling | Domain layer complete | Formula-based Tempo normalization, bounded Strength, catch-zone calculation, external outlier clamps, and percentile Strength and Tempo multipliers |
| Deterministic unit tests | Green | 17 of 17 tests passed locally on Java 21 |
| Full repository build | Green | GitHub Actions run `33138932023` completed `./gradlew clean build --stacktrace` successfully on Java 21 and uploaded JAR artifacts |
| Gradle wrapper | Complete | Pinned Gradle 8.12 wrapper with distribution SHA-256 verification |
| Reconstructed 1.3.57 legacy integration | Blocked by missing baseline | Current `dev` still lacks the reconstructed legacy Java source, resources, tests, and complete reconstruction payload, so no legacy caller could be migrated or superseded code safely removed |

## Implemented files

The pure Fishing System 2.0 domain layer is under:

```text
src/main/java/com/redslovesgames/tideborne/fishing/v2/
```

It currently contains:

- `CanonicalRarity`
- `FishingContext`
- `FishingEnvironment`
- `SpeciesEligibility`
- `SpeciesProfile`
- `SpeciesSelectionService`
- `SizeDistribution`
- `LogNormalSizeDistribution`
- `SpecimenData`
- `SpecimenGenerator`
- `FightProfile`
- `FightProfileService`

Tests are under the matching `src/test/java` package.

## Commits on `dev`

- `e080d8fa9def6b52c80b0899fa72c144f8fba6be`: persist specification and baseline blocker state
- `dbbcd382f33fa7ed0dd428bd20e3162036e2127f`: implement Fishing System 2.0 domain steps 1 through 4
- `babba1747c71d9f08274b085b5e0b599daa7f6d7`: restore the Gradle wrapper and validate `dev` builds

## Reconstruction blocker evidence

The repository's staged 1.3.57 class archive remains incomplete:

- expected compressed archive SHA-256: `e7265ad9a6df36f79be098d47b5fcda570532e5488671e58a0047b6ad70f4ac6`
- committed partial payload SHA-256: `5e05d2065e4c41b72c5830cd17f70abbfc343b5358953ec5e476f264abb2825f`
- expected classes: 272
- readable classes before archive truncation: 91
- missing: remaining class chunks, resource chunks, reconstruction completion marker, reconstructed legacy Java source, reconstructed resources, baseline tests, and persistence fixtures

The green build verifies the new pure domain layer and current repository contents. It does not prove runtime integration with the missing 1.3.57 legacy code, Tide mixins, Satchel, Journal, records, history, teams, networking, or minigame callers.

## Exact next action

Restore the complete reconstructed Tideborne 1.3.57 `src/main/java`, `src/main/resources`, baseline tests, and persistence fixtures onto `dev`. Verify the pinned content-tree hash, then run the untouched baseline build.

After that baseline is green, inspect only the legacy rarity, selection, percentile, size, and fight paths. Adapt those callers to the completed `com.redslovesgames.tideborne.fishing.v2` domain layer, remove old `selection_quality` behavior only after every affected caller is migrated, and add integration and serialization tests. Do not start step 5 until this integration pass is green.

## Later work

The prioritized backlog is in `docs/TODO.md`. The next design slice after the integration gate is step 5, Body Type.


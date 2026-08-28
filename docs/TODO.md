# Fishing System 2.0 implementation backlog

The authoritative behavior and formulas are in `docs/FISHING_SYSTEM_2_SPEC.md`. This file tracks execution state only.

## Phase 0 - authoritative 1.3.57 reconstruction

- [x] Recover the exact Tideborne 1.3.57 release JAR.
- [x] Verify release JAR SHA-256 `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`.
- [x] Verify 442 actual files, 272 `.class` files, and canonical content-tree SHA-256 `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`.
- [x] Reconstruct complete maintained Java source and resources from the authoritative JAR.
- [x] Normalize mechanical Vineflower/Yarn reconstruction artifacts without changing gameplay behavior.
- [x] Compile against exact Tide 2.1.1 and Apex Waters 1.1.1 dependencies.
- [x] Run `./gradlew clean build --stacktrace` successfully. Green reconstruction run: `33152569860`.
- [x] Upload built JAR artifacts from the green reconstructed baseline.
- [x] Fast-forward the reconstructed runtime onto `dev` without touching `main`.

## Steps 1 through 4 - pure V2 domain layer

- [x] Persist the complete Fishing System 2.0 specification.
- [x] Implement `FishingContext`, `SpeciesProfile`, canonical rarity, and compatibility-ready eligibility/environment boundaries.
- [x] Implement `SpeciesSelectionService` and rarity-aware Fishing Luck.
- [x] Implement canonical immutable `SpecimenData`, deterministic base generation, and direct lognormal percentile/quantile math.
- [x] Implement normalized Strength/Tempo, catch-zone math, external clamps, and percentile fight scaling.
- [x] Add deterministic unit tests for the pure V2 mechanics.
- [x] Verify the existing V2 deterministic suite at 17 of 17 passing tests.

## Steps 1 through 4 - runtime integration complete

- [x] Add a Tide-to-V2 species adapter that produces canonical `SpeciesProfile` data without inventing unsupported fish.
- [x] Build one server-owned canonical `FishingContext` for each catch attempt.
- [x] Integrate `SpeciesSelectionService` only into Tide `FishSelector#getResult`, leaving the top-level Tide catch-category selector unchanged.
- [x] Verify Tide `FishSelector.weight(context) = 85` remains unchanged, preserving ordinary fish versus junk/crate/treasure category probability.
- [x] Preserve Tide `shouldKeep` eligibility plus current location, biome, dimension, weather, time, bait, and compatibility restrictions at the V2 boundary.
- [x] Preserve existing fishing and compatibility weight modifiers while removing affected legacy `selection_quality` behavior from the canonical species-selection path.
- [x] Generate one catch seed, select one species, and call `SpecimenGenerator.generateBase` once for the canonical catch.
- [x] Generate one canonical natural percentile and one canonical final size for the current Steps 1 through 4 specimen model.
- [x] Avoid Tide `FishData#getResult` in the V2 bridge so Tide cannot perform its independent hidden `getRandomLength` roll.
- [x] Persist canonical specimen identity and canonical percentile/length into the reconstructed item/component path before downstream catch handling.
- [x] Make canonical `FightProfile` behavior, Strength, Tempo, and catch-zone baseline drive the existing Tide minigame.
- [x] Preserve existing Tide copper, iron, golden, and diamond line effects on top of the canonical fight baseline.
- [x] Preserve Tideborne Tentacle Line, Swift Line, Steel Leader, Leviathan Bait, center-zone Perfect Catch detection, bait, rod, and compatibility hooks unless a later spec step explicitly replaces them.
- [x] Verify canonical specimen data survives current item/entity/item and entity/bucket transfer paths.
- [x] Replace the invalid Mixin 0.8.7 `Bucketable` interface injector with a concrete `FishEntity#copyDataToStack` transfer hook.
- [x] Verify the legacy catch individualizer cannot reroll canonical specimen seed/percentile state.
- [x] Prevent legacy `PerfectCatchTraitBoost` from rewriting canonical percentile or fish length.
- [x] Add deterministic adapter/integration coverage and three runtime GameTests for canonical transfer and no-reroll guarantees.
- [x] Run the relevant tests and verify the pure V2 deterministic suite remains 17 of 17 passing.
- [x] Run `./gradlew clean build --stacktrace` successfully after the runtime integration changes.
- [x] Run `./gradlew runGametest --stacktrace` successfully after the runtime integration changes. Green runtime run: `33159465388`.
- [x] Search the migrated runtime path for duplicate species-selection, percentile, size, Strength, and Tempo calculations. No active canonical duplicate remains; guarded legacy fallback remains for old/noncanonical catches until migration.

## Deterministic trait RNG splitting

- [x] Add stateless V2 trait random splitting derived from canonical specimen seed plus stable named salts.
- [x] Provide deterministic unit-double generation in `[0, 1)` without mutable shared RNG state.
- [x] Reserve independent event/variant salts for Body Type, Condition, Pigmentation, plus a Perfect Specimen salt.
- [x] Prove same seed plus same salt is exactly stable.
- [x] Prove different salts produce independent deterministic values.
- [x] Prove call order and unrelated future salts cannot shift existing outcomes.
- [x] Prove generated unit doubles remain inside valid bounds.
- [ ] Body Type itself remains intentionally unimplemented until the next slice.

## Step 5 - Body Type

- [ ] Implement independent `Normal`, `Giant`, and `Dwarf` Body Type generation using the frozen 5% event model and percentile bias.
- [ ] Implement Giant final-size multiplier 1.10 to 1.30 and Dwarf multiplier 0.60 to 0.82.
- [ ] Apply Giant fight modifiers Strength 1.08 and Tempo 0.95.
- [ ] Apply Dwarf fight modifiers Strength 0.92 and Tempo 1.08.
- [ ] Ensure Body Type uses the canonical specimen seed and never rerolls natural percentile.
- [ ] Add deterministic seeded Body Type tests.

## Step 6 - independent trait axes

- [ ] Separate Condition from Body Type.
- [ ] Implement Condition event 5% with Scarred 65% and Parasite-Ridden 35%.
- [ ] Add independent Pigmentation axis with event 1.5%, Albino 70%, Iridescent 30%.
- [ ] Keep Specimen Quality independent from Body Type, Condition, and Pigmentation.
- [ ] Prove compatible traits can stack and incompatible combinations are rejected deterministically.
- [ ] Add deterministic seeded tests for every axis.

## Step 7 - Trait Luck, rarity compensation, Momentum

- [ ] Implement rarity trait compensation multipliers: 1★ 1.00, 2★ 1.15, 3★ 1.40, 4★ 1.80, 5★ 2.40.
- [ ] Implement exact Trait Luck formula `P' = 1 - (1-P)^(1 + T/10)`.
- [ ] Keep Fishing Luck and Trait Luck mechanically separate.
- [ ] Implement per-species persisted Trait Momentum, approximately +1 temporary Trait Luck after a fully normal catch, capped around 15, substantially reduced after notable traits.
- [ ] Add deterministic probability, cap, persistence, and species-isolation tests.

## Step 8 - Perfect Catch and Perfect Specimen

- [ ] Preserve the existing center-zone Perfect Catch skill check.
- [ ] Implement the V2 Perfect Catch reward path. Canonical catches already bypass the legacy percentile/length rewrite.
- [ ] Make Perfect Catch grant +10 temporary Trait Luck.
- [ ] Make Perfect Catch multiply Body Type event chance by 1.25.
- [ ] Give Perfect Catch a substantial Perfect Specimen bonus without forcing it.
- [ ] Implement Perfect Specimen percentile curve: below 95 = 0%, 95 = 2%, 97.5 = 8%, 99 = 25%, 99.9+ = 60%, smooth interpolation between anchors.
- [ ] Add deterministic curve and Perfect Catch interaction tests.

## Step 9 - FishScore V2

- [ ] Implement raw score as Species + Specimen + Traits.
- [ ] Implement species points 50/100/175/250/350 for 1 through 5 stars.
- [ ] Implement specimen points `3 * finalPercentile`.
- [ ] Implement frozen trait bonuses: Scarred +20, Parasite +35, Giant +40, Dwarf +40, Albino +70, Iridescent +100, Perfect +100.
- [ ] Implement canonical linear normalization `round(1 + 2999 * normalized)` clamped to 1 through 3000.
- [ ] Prove canonical minimum Incandescent Larva = 1.
- [ ] Prove canonical maximum Dragon Fish at P100 with best compatible trait combination = 3000.
- [ ] Prove intermediate mapping is linear.
- [ ] Add deterministic scoring tests.

## Migration, progression, compatibility, cleanup

- [ ] Implement deterministic, idempotent legacy migration and score recalculation.
- [ ] Make canonical `SpecimenData` authoritative across persistence, Satchel, Journal, records, history, teams, UI, and networking.
- [ ] Integrate gear into the canonical context/fight pipeline.
- [ ] Rework Leviathan Bait to fish-only catches, +15 Fishing Luck, substantial Trait Luck, Strength 1.15, Tempo 1.15, with old `selection_quality` behavior removed.
- [ ] Build compatibility SpeciesProfiles for every actually supported Tide/compatibility fish.
- [ ] Remove dead legacy code only after all callers and stored data are migrated.
- [ ] Complete runtime smoke tests, migration rebuild validation, optional-mod absence safety, documentation, and release validation.
- [ ] Finish with `./gradlew build` green.

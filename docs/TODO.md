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
- [x] Implement canonical immutable `SpecimenData`, deterministic base generation, and direct percentile/quantile math.
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
- [x] Reserve an independent Body Type physical-size salt so size finalization cannot shift any trait decision stream.
- [x] Prove same seed plus same salt is exactly stable.
- [x] Prove different salts produce independent deterministic values.
- [x] Prove call order and unrelated future salts cannot shift existing outcomes.
- [x] Prove generated unit doubles remain inside valid bounds.
- [x] Implement the isolated Body Type probability-selection service using the reserved Body Type streams.

## Step 5 - Body Type

- [x] Implement independent `NORMAL`, `GIANT`, and `DWARF` Body Type selection using the frozen 5% base event model and smooth percentile bias.
- [x] Route canonical Body Type event probability through `TraitProbabilityService` in the fixed order: 5% base, species rarity compensation, Trait Luck, final bound.
- [x] Pass the server-owned `FishingContext.traitLuck()` into canonical specimen generation for Body Type selection.
- [x] Preserve the existing Body Type event and variant RNG salts and keep Giant/Dwarf percentile bias separate from event probability.
- [x] Reserve an explicit post-pipeline Body Type event multiplier parameter for the later Perfect Catch 1.25x rule without activating Perfect Catch behavior in this stage.
- [x] Add statistical Body Type event-rate coverage across multiple rarity and Trait Luck combinations, plus exact shared-pipeline and future-multiplier tests.
- [x] Implement Giant final-size multiplier 1.10 to 1.30 and Dwarf multiplier 0.60 to 0.82.
- [x] Apply Giant fight modifiers Strength 1.08 and Tempo 0.95.
- [x] Apply Dwarf fight modifiers Strength 0.92 and Tempo 1.08.
- [x] Ensure Body Type selection uses the canonical specimen seed and never rerolls natural percentile.
- [x] Keep `basePercentile` as the original natural roll and derive size-adjusted `finalPercentile` deterministically from `finalLength` through the species CDF, with no second random percentile.
- [x] Add deterministic seeded Body Type probability tests, including event rate, percentile bias, endpoint possibility, P50 balance, and trait-stream independence.
- [x] Add physical-size tests for multiplier bounds, deterministic multipliers, Normal identity, Giant/Dwarf direction, preserved natural percentile, and exactly one base-size sample.
- [x] Wire selected Body Type and physical size into canonical specimen finalization and the Tide V2 catch bridge.
- [x] Apply Body Type only after the existing canonical percentile fight scaling, without creating a second fight calculation path.
- [x] Compare otherwise identical Normal, Giant, and Dwarf specimens and prove the exact fight multipliers, catch-zone recomputation, preserved behavior, and unchanged Normal baseline.
- [x] Preserve the existing final Tempo clamp and canonical catch-zone clamping behavior after Body Type modification.
- [x] Verify the runtime minigame still consumes the canonical `FightProfile` before existing Tide line and Tideborne compatibility modifiers.
- [x] Make the server runtime order explicit: species selection, one base specimen sample, one Body Type sample, final physical size, canonical `FightProfile`, then persistence.
- [x] Persist Body Type as canonical `SPECIMEN_BODY_TYPE` state and mirror it to legacy `BODY_TYPE` only for compatibility.
- [x] Make canonical V2 Body Type authoritative in `TraitAxesRuntime`, including migration, display/edit reads, and physical-effect helpers.
- [x] Bypass the legacy P97/P3 Giant/Dwarf gates and legacy body-size multiplier for canonical V2 catches.
- [x] Preserve canonical Body Type explicitly in specimen transfer NBT and repair stale legacy mirrors from canonical state instead of rerolling.
- [x] Add GameTests proving a high-percentile canonical Dwarf survives item/entity/item transfer, direct transfer NBT, legacy individualization, and legacy Perfect Catch handling without changing Body Type.
- [x] Run `./gradlew clean build --stacktrace` and `./gradlew runGametest --stacktrace` successfully. Green Body Type fight run: `33163133061` on commit `72c98ea3d3f79161d97720a5833d47eb4e2f4a33`.

## Step 6 - independent trait axes

- [x] Separate Condition from Body Type.
- [x] Implement Condition event 5% with Scarred 65% and Parasite-Ridden 35%.
- [x] Derive Condition deterministically from the reserved independent `CONDITION_EVENT` and `CONDITION_VARIANT` specimen-seed salts.
- [x] Allow Body Type and Condition to stack with no mutual exclusion.
- [x] Persist exactly one canonical Condition per specimen and mirror it to legacy mutation state only for compatibility.
- [x] Prevent legacy mutation selection from rerolling Condition for canonical V2 specimens, including when the compatibility mirror is missing.
- [x] Add deterministic Condition tests for repeatability, approximately 5% event rate, approximately 65/35 subtype split, Body Type stacking, and scalar one-Condition state.
- [x] Verify canonical Condition survives the current item/entity/item transfer path and legacy catch handling. Green Condition run: `33165044512` on commit `023c9a01918f525c8a726862d7cd800bc587d9f3`.
- [x] Add independent Pigmentation axis with event 1.5%, Albino 70%, Iridescent 30%.
- [x] Derive Pigmentation deterministically from independent `PIGMENTATION_EVENT` and `PIGMENTATION_VARIANT` specimen-seed salts.
- [x] Allow Body Type, Condition, and Pigmentation to stack without legacy mutation exclusivity; seed `29894` proves `GIANT + PARASITE_RIDDEN + IRIDESCENT` in full canonical generation.
- [x] Persist exactly one canonical Pigmentation value and prove legacy catch individualization cannot reroll it.
- [x] Verify canonical Pigmentation survives item/entity/item representation transfer. Green Pigmentation run: `33165703632` on commit `7fd6181c09ca0e49dd598adf5fbb39d992eb29e3`.
- [x] Keep runtime generation order explicit and single-pass: base specimen, Body Type, final physical size, Condition, Pigmentation, FightProfile, persistence.
- [x] Keep Body Type, Condition, and Pigmentation server-authoritative in one canonical `SpecimenData`; legacy `CatchTraitService` and `TraitAxesRuntime` remain guarded compatibility fallbacks and do not regenerate canonical axes.
- [x] Preserve all three canonical axes explicitly in specimen transfer NBT, including snapshot-free fallback restoration, while mirroring Body Type and Condition only to their existing legacy compatibility components.
- [x] Add GameTests for stacked `GIANT + PARASITE_RIDDEN + IRIDESCENT` specimens through legacy handling and item/entity/bucket/entity/item plus explicit transfer-NBT round trips.
- [x] Run clean build, unit tests, Fabric GameTests, and artifact upload successfully for the runtime axis integration. Green run: `33166720586` on commit `774752af3b22f7a4dcb814602f48c21cd1895779`.
- [ ] Keep Specimen Quality independent from Body Type, Condition, and Pigmentation.
- [ ] Prove all intended compatible trait combinations can stack and incompatible combinations are rejected deterministically.
- [ ] Add deterministic seeded tests for every remaining independent axis.

## Step 7 - Trait Luck, rarity compensation, Momentum

- [x] Implement rarity trait compensation multipliers: 1★ 1.00, 2★ 1.15, 3★ 1.40, 4★ 1.80, 5★ 2.40.
- [x] Implement exact Trait Luck formula `P' = 1 - (1-P)^(1 + T/10)` as the pure `TraitLuckProbabilityService`.
- [x] Clamp numeric probability inputs to `[0,1]`, reject `NaN` probability, floor Trait Luck at `-10`, treat `NaN` Trait Luck as zero, and safely saturate extreme positive Trait Luck.
- [x] Centralize canonical trait-event probability calculation in `TraitProbabilityService` with fixed order: base probability, canonical species rarity compensation, Trait Luck transform, final bound.
- [x] Read rarity compensation only from the selected canonical `SpeciesProfile.rarity()` and keep the existing Fishing Luck coefficients separate and unchanged.
- [x] Keep Fishing Luck and Trait Luck mechanically separate, including a seeded species-selection regression proving Trait Luck does not affect species selection.
- [x] Keep deterministic trait RNG selection separate from Trait Luck probability calculation.
- [x] Wire Body Type to the shared canonical probability pipeline while leaving its Giant/Dwarf conditional subtype bias and deterministic RNG streams unchanged.
- [ ] Wire Trait Luck and rarity compensation into the remaining intended trait axes only in their dedicated integration slices.
- [ ] Implement per-species persisted Trait Momentum, approximately +1 temporary Trait Luck after a fully normal catch, capped around 15, substantially reduced after notable traits.
- [x] Add pure probability tests for T=0 identity, monotonic positive Trait Luck, valid output range, known numerical cases, exact 0/1 endpoints, probability validation/clamping, and negative/extreme Trait Luck behavior.
- [x] Add exact rarity multiplier tests for all five canonical rarities plus compensated zero-Trait-Luck and combined Trait Luck cases, including a test that freezes rarity-before-Trait-Luck ordering.
- [x] Add Body Type statistical event-rate tests for several canonical rarity/Trait Luck combinations and exact tests proving the shared pipeline calculation is used.
- [ ] Add Momentum cap, persistence, and species-isolation tests when Momentum is implemented.

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
- [ ] Prove intermediate FishScore mapping is linear.
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

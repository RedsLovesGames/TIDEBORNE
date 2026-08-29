# Current development state

Updated: 2026-08-29

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

The original deterministic pure V2 suite remains green, with additional Body Type, Condition, and Pigmentation coverage layered on top.

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
- the Body Type base event probability is exactly 5%; canonical event chance now applies selected-species rarity compensation and then Trait Luck through `TraitProbabilityService`; a failed adjusted event returns `NORMAL`
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

Deterministic tests cover exact 5% base configuration, repeatability, approximately 5% one-star zero-Trait-Luck event frequency, shared-pipeline event rates across several rarity and Trait Luck combinations, exact shared-pipeline equality, the reserved future Body Type event multiplier, P75 versus P25 Giant bias, both variants across the percentile range, P50 balance, the documented bias formula, independence from other trait streams, physical multiplier bounds, deterministic multiplier values, exact Normal identity, Giant/Dwarf size direction, preserved base percentile, size-adjusted final percentile, no-physical-size fallback, exactly one base-size quantile sample during complete generation, exact Normal/Giant/Dwarf fight multiplier comparisons for otherwise identical specimens, preserved percentile fight scaling, catch-zone recomputation, and unchanged behavior.

Body Type fight implementation is commit `72c98ea3d3f79161d97720a5833d47eb4e2f4a33`. Validation is green with GitHub Actions run `33163133061`: `./gradlew clean build --stacktrace`, unit tests included by the Gradle build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Body Type runtime authority is complete

The completed Body Type model is now wired into the server-authoritative V2 specimen lifecycle without adding a second trait decision path.

Runtime order is explicit and fixed:

```text
species selected
-> base specimen generated once
-> Body Type generated once
-> final physical size produced
-> canonical FightProfile generated
-> specimen persisted
```

Runtime authority and compatibility contracts:

- `SpecimenGenerator.generate` explicitly calls base generation first, then performs one deterministic Body Type selection, then applies the selected Body Type to physical size without another Body Type or natural-size sample.
- `TideSpeciesSelectionBridge` creates the canonical `FightProfile` from that finalized specimen before `CanonicalSpecimenStorage` persists it.
- `SpecimenData.bodyType` remains the immutable in-memory canonical value.
- `CanonicalSpecimenStorage` persists Body Type in the canonical `SPECIMEN_BODY_TYPE` component and mirrors the exact same value to legacy `BODY_TYPE` only for compatibility.
- `TraitAxesRuntime` recognizes canonical schema-v2 specimens and reads canonical Body Type first. For those specimens its migration, editing, and physical-effect helpers cannot generate or reapply a legacy Body Type.
- `TraitAxesRuntime.normalizeNew` returns before the old P97 Giant and P3 Dwarf gates and before the legacy body-size multiplier when canonical V2 specimen identity is present.
- interim schema-v2 stacks that predate `SPECIMEN_BODY_TYPE` are migrated by copying their already-persisted legacy `BODY_TYPE` into canonical storage once. This migration consumes no RNG.
- `SpecimenTransfer` writes an explicit `CanonicalBodyType` transfer field in addition to the existing compatibility `BodyType` field and generic source-stack snapshot. Restoration makes the canonical value authoritative and repairs a stale compatibility mirror rather than rerolling it.
- entity, item, bucket, serialization, UI/admin compatibility reads, and legacy physical-effect helpers therefore preserve canonical Body Type instead of deriving a new value.

The runtime GameTest fixture deliberately uses a P99.25 canonical Dwarf while corrupting only the legacy mirror to Giant. This places the specimen inside the old P97 Giant gate and proves canonical Dwarf survives direct transfer NBT, item/entity/item transfer, the legacy catch individualizer, and legacy Perfect Catch handling without changing.

Stage 6 implementation plus TODO state at commit `faeb4d3c102a976740749cb5af6017bbba76b38d` is green in GitHub Actions run `33164107173`. The exact-dependency clean Gradle build, unit tests included by the build, Fabric GameTests, and built-JAR artifact upload all completed successfully.

## Step 6 Condition axis is complete

The Condition portion of the independent trait-axis step is implemented without introducing Trait Luck, rarity compensation, Pigmentation generation, or any second mutation authority.

Frozen Condition behavior:

- canonical values are `NORMAL`, `SCARRED`, and `PARASITE_RIDDEN`
- the base Condition event probability is exactly 5%; a failed event returns `NORMAL`
- when the event triggers, Scarred is exactly 65% of the conditional split and Parasite-Ridden is exactly 35%
- event and subtype selection use the reserved `TraitRandom.Salts.CONDITION_EVENT` and `CONDITION_VARIANT` streams
- Condition does not consume Body Type event, variant, or size streams and Body Type does not consume Condition streams
- Body Type and Condition have no mutual exclusion; both notable values can exist on the same canonical specimen
- `SpecimenData.condition` is one required enum-valued field, so each canonical specimen has exactly one Condition state rather than a collection that could represent incompatible simultaneous conditions
- `SpecimenGenerator.generate` now samples the natural specimen once, finalizes Body Type physical size, then applies one deterministic Condition derived from the same canonical specimen seed through the independent Condition salts
- Condition application preserves species identity, natural percentile, base length, final physical size, Body Type, Pigmentation, Quality, Perfect Catch state, score fields, and provenance
- `CanonicalSpecimenStorage` persists the enum value to `SPECIMEN_CONDITION` and mirrors that exact value to legacy `MUTATION` only for compatibility
- `CatchTraitService.assignIfAbsent` now detects canonical V2 specimen identity before the legacy mutation selector can run; it repairs a missing/stale legacy mutation mirror from `SPECIMEN_CONDITION` and returns without consuming legacy RNG
- UI and compatibility code therefore do not own Condition generation; the canonical server-generated specimen value is persisted before those consumers run
- no Trait Luck or rarity compensation is applied in this slice, so the frozen 5% and 65/35 base probabilities are used directly

Deterministic Condition tests cover exact probability constants, repeatability, approximately 5% event frequency, approximately 65/35 triggered subtype frequency, independence from Body Type streams, a fixed seed that produces Body Type plus Condition simultaneously, repeated Condition application without accumulation, and full canonical generation preserving the one natural specimen sample.

The runtime GameTest fixture now carries canonical `DWARF` plus `SCARRED`. Item/entity/item transfer proves canonical Condition survives representation transfer, and the legacy-individualizer test deletes the `MUTATION` compatibility mirror before invocation. The test then proves `SCARRED` remains canonical and the compatibility mirror is restored to `scarred` without changing the canonical seed, percentile, Body Type, or Condition.

Condition implementation commit `023c9a01918f525c8a726862d7cd800bc587d9f3` is green in GitHub Actions run `33165044512`. The exact-dependency `./gradlew clean build --stacktrace`, unit tests included by the Gradle build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Step 6 Pigmentation axis is complete

The Pigmentation portion of the independent trait-axis step is implemented as a separate canonical axis and does not use the legacy mutually-exclusive mutation selector.

Frozen Pigmentation behavior:

- canonical values are `NORMAL`, `ALBINO`, and `IRIDESCENT`
- the base Pigmentation event probability is exactly 1.5%; a failed event returns `NORMAL`
- when the event triggers, Albino is exactly 70% of the conditional split and Iridescent is exactly 30%
- event and subtype selection use the reserved `TraitRandom.Salts.PIGMENTATION_EVENT` and `PIGMENTATION_VARIANT` streams
- Pigmentation does not consume or depend on Body Type or Condition streams, and those axes do not consume Pigmentation streams
- `SpecimenData.pigmentation` is one required enum-valued field, so every canonical specimen has exactly one Pigmentation state
- `SpecimenGenerator.generate` finalizes Body Type physical size, then Condition, then Pigmentation; all three decisions derive independently from the same immutable canonical specimen seed through separate salts
- there is no cross-axis mutual exclusion: deterministic seed `29894` produces `GIANT + PARASITE_RIDDEN + IRIDESCENT` in full canonical generation
- Pigmentation application preserves species identity, natural percentile, base and final length, final percentile, Body Type, Condition, Quality, Perfect Catch state, score fields, and provenance
- `CanonicalSpecimenStorage` persists Pigmentation to `SPECIMEN_PIGMENTATION`; it is not packed into legacy `MUTATION`, so legacy Condition compatibility state cannot make Pigmentation mutually exclusive
- `CatchTraitService.assignIfAbsent` exits through the canonical V2 path before legacy mutation selection and does not write `SPECIMEN_PIGMENTATION`, so canonical Pigmentation cannot be rerolled by the old mutation authority
- the existing generic canonical source-stack snapshot preserves `SPECIMEN_PIGMENTATION` through item/entity/item representation transfer without adding a second Pigmentation representation
- no Trait Luck, rarity compensation, Specimen Quality, Perfect Catch redesign, or scoring behavior is introduced by this slice

Deterministic Pigmentation tests cover exact probability constants, repeatability, approximately 1.5% event frequency, approximately 70/30 triggered subtype frequency, independence from Body Type and Condition streams, deterministic three-axis stacking, idempotent Pigmentation application, and preservation of the single natural specimen sample.

Runtime GameTests write a canonical `GIANT + PARASITE_RIDDEN + IRIDESCENT` specimen, deliberately replace the legacy `MUTATION` mirror with `albino`, invoke the legacy catch individualizer, and prove canonical Iridescent Pigmentation remains unchanged while only the Condition compatibility mirror is repaired. A second GameTest proves the same canonical Pigmentation survives item/entity/item representation transfer.

Pigmentation implementation commit `7fd6181c09ca0e49dd598adf5fbb39d992eb29e3` is green in GitHub Actions run `33165703632`. The exact-dependency `./gradlew clean build --stacktrace`, unit tests included by the Gradle build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Condition and Pigmentation runtime integration is complete

The three currently implemented canonical specimen axes are now confirmed end to end in the server-authoritative runtime path.

Runtime generation order is explicit and deterministic:

```text
species selected
-> base specimen generated once
-> Body Type generated once
-> final physical size produced
-> Condition generated once
-> Pigmentation generated once
-> canonical FightProfile generated
-> specimen persisted
```

Runtime authority and transfer contracts:

- `TideSpeciesSelectionBridge` makes one call to `SpecimenGenerator.generate` for the selected species and uses that single returned `SpecimenData` for both `CanonicalSpecimenStorage` and `CanonicalCatchStateManager`.
- `SpecimenGenerator.generate` uses the already-frozen independent trait salts and applies Body Type, Condition, and Pigmentation exactly once without shared mutable RNG state or a second natural specimen sample.
- `CanonicalSpecimenStorage` persists all three axes as `SPECIMEN_BODY_TYPE`, `SPECIMEN_CONDITION`, and `SPECIMEN_PIGMENTATION` before downstream legacy catch handling runs.
- only compatibility mirrors are written into legacy components: Body Type mirrors to `BODY_TYPE`, Condition mirrors to `MUTATION`, and Pigmentation remains canonical-only because there is no separate legacy Pigmentation component.
- `CatchTraitService.assignIfAbsent` exits before legacy mutation selection for canonical schema-v2 specimens, repairs the Condition compatibility mirror from canonical state, and does not regenerate Body Type, Condition, or Pigmentation.
- `TraitAxesRuntime` remains a guarded compatibility fallback for canonical specimens. Its canonical migration and normalization paths do not run legacy Body Type gates or mutation generation.
- `SpecimenTransfer` data version 4 now stores `CanonicalBodyType`, `CanonicalCondition`, and `CanonicalPigmentation` explicitly in addition to the generic source-stack snapshot. This makes all three axes survive even the snapshot-free transfer fallback instead of relying on generic component serialization alone.
- restoration makes canonical Body Type and Condition authoritative over their legacy compatibility mirrors, while Pigmentation restores only its canonical component and cannot become mutually exclusive with Condition.
- item, entity, bucket, entity reload, and direct transfer-NBT representations therefore retain the same stacked axes without another trait decision.

The runtime GameTests use the deterministic stacked fixture `GIANT + PARASITE_RIDDEN + IRIDESCENT`. They prove stale legacy Body Type/mutation values are repaired without changing canonical axes, and prove the same three values survive item/entity/bucket/entity/item conversion plus explicit transfer NBT without a registry-backed source-stack snapshot.

Runtime axis integration implementation is commit `774752af3b22f7a4dcb814602f48c21cd1895779`. GitHub Actions run `33166720586` is green: exact-dependency `./gradlew clean build --stacktrace`, unit tests included by the build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Pure Trait Luck probability math is complete

Stage 10 adds the isolated Fishing System 2.0 Trait Luck probability service without wiring Trait Luck into Body Type, Condition, Pigmentation, Specimen Quality, rarity compensation, Momentum, or any runtime trait generator.

Frozen probability behavior:

- `TraitLuckProbabilityService` implements exactly `P' = 1 - (1 - P)^(1 + T / 10)`
- numeric probability inputs clamp to `[0, 1]`; `NaN` probability is rejected because it cannot represent an event chance
- `P = 0` and `P = 1` remain exact fixed endpoints for every supported Trait Luck input
- Trait Luck below `-10` clamps to `-10`, keeping the exponent nonnegative and preventing invalid negative adjusted probabilities
- `NaN` Trait Luck behaves as zero; negative infinity reaches the `-10` floor; positive infinity safely saturates every nonzero, nonunit base probability to 1
- the implementation uses `log1p` and `expm1` for stable probability math while preserving exact T=0 and endpoint behavior
- the service contains no RNG state and performs no trait selection; deterministic trait selection remains the responsibility of the existing independent trait streams
- Fishing Luck and Trait Luck remain mechanically separate; `SpeciesSelectionService` production code is unchanged and continues to read only `FishingContext.fishingLuck()`
- a seeded regression compares species selection under identical Fishing Luck with Trait Luck 0 versus 1,000,000 and requires the selected species sequence to remain identical

Unit coverage includes T=0 identity, monotonic increase for positive Trait Luck, output range, the documented 1% numerical examples, additional known numerical cases, exact 0/1 endpoints, probability clamping and NaN rejection, and defined negative/extreme Trait Luck behavior.

Implementation commit `7606bc21d4d5489692e210227d8c7fdeac15f339` is validated by GitHub Actions run `33167369448`: `./gradlew clean build --stacktrace`, unit tests included by the Gradle build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload completed successfully.

## Rarity compensation probability path is complete

Stage 11 adds canonical rare-species trait compensation without wiring probability changes into the trait generators yet.

Frozen probability behavior:

- `CanonicalRarity` now stores the exact trait-event multipliers 1 star 1.00, 2 star 1.15, 3 star 1.40, 4 star 1.80, and 5 star 2.40 alongside the pre-existing, unchanged Fishing Luck coefficients
- `TraitProbabilityService` is the reusable canonical V2 trait-event probability path
- callers provide a base trait event probability plus the selected canonical `SpeciesProfile`; rarity is read only from `SpeciesProfile.rarity()` so a second independent rarity value cannot disagree with the selected species
- the calculation order is fixed as base trait probability, rarity compensation, Trait Luck transform, then final defensive bounding to `[0, 1]`
- the existing `TraitLuckProbabilityService` remains the single implementation of the Trait Luck formula and its frozen input-safety behavior; `TraitProbabilityService` composes it rather than duplicating the formula
- no random selection is performed by either probability service
- `SpeciesSelectionService` is unchanged, so rarity compensation and Trait Luck do not participate in Fishing Luck species selection
- Body Type, Condition, Pigmentation, and later Specimen Quality generators are intentionally not wired to the new probability path in this stage; that remains a dedicated integration slice

Exact unit coverage verifies all five canonical rarity multipliers, all five compensated probabilities at zero Trait Luck, combined rarity plus Trait Luck numerical cases, the required rarity-before-Trait-Luck order using the 5-star 5% plus T=10 result of 22.56%, final probability bounds, and inherited invalid-input behavior.

Implementation commit `abf75156f906a0ef2e40b813e5b20f6870372430` contains the code and tests. Full CI validation is recorded below once the documentation commit is validated.

## Body Type trait probability integration is complete

Stage 12 moves only the Body Type event probability onto the shared canonical trait probability pipeline.

Frozen integration behavior:

- Body Type keeps its exact 5% base event probability.
- `BodyTypeGenerator` calls `TraitProbabilityService` with the selected canonical `SpeciesProfile`, so rarity compensation is applied first and Trait Luck is applied second before the final event bound.
- canonical species rarity comes only from `SpeciesProfile.rarity()`; Body Type does not accept or derive a second independent rarity value.
- `TideSpeciesSelectionBridge` passes the server-owned `FishingContext.traitLuck()` into `SpecimenGenerator`, which forwards it to the one canonical Body Type selection.
- the Body Type event comparison still uses `TraitRandom.Salts.BODY_TYPE_EVENT`, and the conditional Giant/Dwarf decision still uses `TraitRandom.Salts.BODY_TYPE_VARIANT`; no deterministic salt changed.
- Giant/Dwarf subtype selection remains entirely separate from event probability and still uses `giantProbability = 0.25 + 0.50 * (naturalPercentile / 100.0)` after an event triggers.
- there are still no hard percentile thresholds, so Giant remains possible at low percentile and Dwarf remains possible at high percentile.
- `SpecimenGenerator` and `BodyTypeGenerator` now accept an explicit Body Type event probability multiplier. Runtime supplies `1.0` in this stage. This reserves the API position needed for the later Perfect Catch 1.25x Body Type rule without implementing Perfect Catch behavior early.
- the reserved Body Type event multiplier is applied after the canonical base, rarity, Trait Luck pipeline and is bounded to `[0, 1]`; it does not alter the Giant/Dwarf conditional split.
- the old two-argument Body Type selector was not restored after stale tests exposed callers, preventing a rarity-free bypass path from remaining beside the canonical API.
- Condition, Pigmentation, Specimen Quality, Momentum, and Perfect Catch reward behavior are unchanged by this stage.

Statistical coverage samples Body Type events across multiple rarity and Trait Luck combinations, including 1-star T=-5, 1-star T=10, 3-star T=0, 4-star T=20, and 5-star T=10. Exact tests additionally prove all rarity/Trait Luck combinations route through `TraitProbabilityService`, the future multiplier is post-pipeline and bounded, deterministic Body Type salts remain stable, subtype bias remains separate, and natural specimen size is not rerolled.

Implementation commit `149aa5b9d67ec59334dae4fb3810c27db586029a` introduced the canonical pipeline integration. Commit `ee5bc701feed9aa7fff2c9c4694ed45bfcba38cb` updated the remaining Body Type test callers to the species-aware canonical API after CI exposed the stale two-argument calls. GitHub Actions run `33168959022` is green: exact-dependency `./gradlew clean build --stacktrace`, unit and statistical tests included by the Gradle build, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Condition and Pigmentation trait probability integration is complete

Stage 13 moves Condition and Pigmentation event probability onto the same canonical `TraitProbabilityService` path already used by Body Type.

Frozen integration behavior:

- Condition keeps its exact 5% base event probability, then selected-species rarity compensation is applied, then Trait Luck, then the final bounded probability is compared against `TraitRandom.Salts.CONDITION_EVENT`.
- Pigmentation keeps its exact 1.5% base event probability, then selected-species rarity compensation is applied, then Trait Luck, then the final bounded probability is compared against `TraitRandom.Salts.PIGMENTATION_EVENT`.
- `ConditionGenerator` and `PigmentationGenerator` now require the selected canonical `SpeciesProfile` and Trait Luck for event selection. Neither accepts a second independent rarity value.
- `SpecimenGenerator` forwards the same server-owned Trait Luck value it already receives from `TideSpeciesSelectionBridge` to Body Type, Condition, and Pigmentation in the existing single-pass generation order.
- Condition subtype selection still uses `TraitRandom.Salts.CONDITION_VARIANT` with the frozen 65% Scarred and 35% Parasite-Ridden conditional split.
- Pigmentation subtype selection still uses `TraitRandom.Salts.PIGMENTATION_VARIANT` with the frozen 70% Albino and 30% Iridescent conditional split.
- rarity compensation and Trait Luck alter only whether each axis event occurs. They do not alter subtype ratios and do not consume the variant RNG streams.
- Body Type, Condition, and Pigmentation continue to use independent event and variant salts, so evaluating one axis does not shift any other axis and notable values can still stack.
- all adjusted trait probability decisions remain in the server-authoritative canonical specimen generator. Client/UI and legacy compatibility code continue to consume persisted canonical axis values rather than recomputing them.
- no Specimen Quality, Momentum, Perfect Catch reward, Perfect Specimen, or FishScore behavior is implemented by this stage.

Exact tests prove one-star zero-Trait-Luck base probabilities remain 5% and 1.5%, five-star rarity compensation raises them to 12% and 3.6%, and five-star T=10 raises them to 22.56% and 7.0704% respectively. Statistical tests apply the compensated T=10 event probabilities while checking that triggered Condition remains approximately 65/35 and triggered Pigmentation remains approximately 70/30. The existing deterministic stacking seed remains valid at the one-star zero-Trait-Luck baseline, and `SpecimenGeneratorTest` now proves the same Trait Luck value is forwarded to all three implemented axes without changing the single natural specimen sample.

Implementation commit `7e4a9f6804b07428c57c752e56d40e5075b7b6a2` contains the generator, runtime forwarding, and test changes. GitHub Actions run `33169902017` performs the exact-dependency clean build, unit/statistical tests, Fabric GameTests, and artifact upload for that implementation commit.

## Per-species Trait Momentum storage is complete

Stage 14 implements the storage and access layer for player Trait Momentum without applying Momentum to catch probabilities yet.

Frozen storage behavior:

- `TraitMomentumStorage` is the server-authoritative public facade and accepts `ServerPlayerEntity`, so client-only state cannot author Momentum values.
- each player has an independent map keyed by canonical namespaced species ID; there is no global Momentum scalar and one species cannot overwrite another species' value.
- stored Momentum is bounded to the inclusive range 0 through 15. Zero is the default and zero-valued entries are omitted from the serialized species map.
- `get`, `set`, `add`, `clear`, and immutable `snapshot` access APIs are available for progression without exposing mutable backing data.
- the storage uses Tide's existing per-player `TidePlayer` persistent NBT root, matching the persistence pattern already used by Tideborne discovery data. This provides logout/restart persistence without putting Momentum into the team-shared Tide journal.
- Momentum is serialized under the dedicated `FishingV2TraitMomentum` compound with `DataVersion = 1` and a `Species` compound containing integer values by species ID.
- malformed non-compound roots, wrong value types, invalid species IDs, and negative values do not create Momentum. Oversized stored values are clamped to 15.
- the decoder also accepts an unversioned direct species-to-int map as a legacy/early-data shape, allowing safe forward migration instead of failing or creating a global fallback value.
- no fish item, entity, specimen, `SpecimenData`, or client packet owns the player's Momentum.

Unit coverage proves independent species values, cap/floor enforcement including overflow-safe additions, serialization/deserialization through the player-root shape, default zero for absent data, sanitization of malformed entries, and loading of the tolerated old unversioned format.

Implementation commit `2727ad0ae3d6a35fcab047f93412220554ceb867` is green in GitHub Actions run `33170551133`. The exact-dependency build, all unit tests, Fabric GameTests, and built-JAR artifact upload completed successfully.

## Trait Momentum progression is complete

Stage 15 wires the stored per-player, per-species Momentum into canonical catch generation and completed-catch progression.

Frozen progression behavior:

- species selection remains unchanged and is still driven only by Fishing Luck and the canonical species selector.
- after a species is selected, `TideSpeciesSelectionBridge` reads Momentum only for that selected species and captures the integer value before specimen generation.
- the captured Momentum value is added to the server-owned `FishingContext.traitLuck()` as temporary Trait Luck for Body Type, Condition, and Pigmentation event probabilities. It does not participate in species selection.
- the frozen captured value is stored only in transient `CanonicalCatchStateManager.CatchState`; it is not written into the fish item, entity, `SpecimenData`, provenance, or any client-owned state.
- later changes to the player's stored Momentum cannot alter the deterministic specimen already generated for that catch because generation uses the captured value exactly once.
- a fully normal catch is defined only by canonical trait axes: Body Type, Condition, Pigmentation, and Specimen Quality must all be `NORMAL`. Natural percentile, base/final size, and the Perfect Catch skill flag do not determine whether the specimen is fully normal.
- a fully normal completed catch adds exactly +1 Momentum for that species.
- any notable value on one of those canonical trait axes resets that species' Momentum to 0. This is the stage-15 interpretation of the specification's required substantial reduction.
- the existing storage cap keeps Momentum bounded at 15.
- progression occurs only from the server-side completed `TideFishingHook#retrieve(ItemStack, ServerWorld, PlayerEntity)` path. `CanonicalCatchStateManager` owns a synchronized one-shot guard, so duplicate completion callbacks for the same canonical catch cannot apply Momentum twice.
- if the catch is invalidated or lost before completion, the canonical catch state is cleared first and there is no Momentum update.
- item/entity conversion, serialization, bucket transfer, UI reads, legacy trait callbacks, and other representation callbacks do not update Momentum.
- species isolation remains exact because both capture and progression use `SpecimenData.speciesId()` / selected `SpeciesProfile.speciesId()` as the only Momentum key.

Focused tests prove repeated fully normal catches increase Momentum one point at a time, the 15 cap remains enforced, each notable canonical axis resets Momentum, other species remain unchanged, captured Momentum adds to temporary Trait Luck, fully-normal classification ignores non-axis size/Perfect Catch metadata, captured Momentum stays frozen in catch state, and the completion update callback can execute exactly once.

Implementation commit `c2b7422d5a29325f25f1bab692f6526dc3780ebf` is green in GitHub Actions run `33171860987`. The exact-dependency `./gradlew clean build --stacktrace`, unit tests, Fabric GameTests, and built-JAR artifact upload all completed successfully.

## Perfect Catch pre-delivery lifecycle is complete

Stage 16 fixes the runtime ordering so Tide's existing center-zone Perfect Catch result is known before canonical post-fight specimen finalization and fish delivery. It does not implement the later Perfect Catch reward math.

Current canonical runtime order:

```text
species selected
-> catch/specimen seed fixed
-> natural percentile and base length generated once
-> Body Type generated
-> final physical size produced
-> canonical FightProfile generated
-> Tide minigame runs
-> Tide resolves the existing center-zone Perfect Catch skill check
-> retrieve(perfectCatch) enters on the server
-> perfectCatch captured into canonical catch/specimen state
-> Condition generated
-> Pigmentation generated
-> finalized specimen persisted to the selected fish item
-> Tide delivery continues
-> completed-catch Momentum progression runs once
```

Frozen lifecycle behavior:

- `SpecimenGenerator.generatePreFight` now owns only the pre-fight specimen portion required to build the fight: the single natural specimen sample, Body Type, and Body Type physical-size finalization.
- `SpecimenGenerator.finalizeAfterFight` receives that already-created pre-fight specimen and the authoritative `perfectCatch` boolean, writes the skill flag into canonical `SpecimenData`, then generates Condition and Pigmentation.
- Perfect Catch is therefore available at the canonical post-fight generation boundary before final persistence, which is the integration point required by later +10 Trait Luck and Perfect Specimen reward work.
- this stage does not yet apply +10 Trait Luck, the 1.25x Body Type event multiplier, or any Perfect Specimen bonus.
- post-fight finalization preserves species ID, deterministic specimen seed, `basePercentile`, `baseLength`, Body Type, `finalLength`, and `finalPercentile` exactly.
- species selection is not repeated.
- natural percentile and base size are not repeated.
- Perfect Catch alone does not change length or percentile.
- `CanonicalCatchStateManager.CatchState` now keeps the pre-fight specimen mutable only inside transient server-owned catch state until one-shot post-fight finalization completes.
- the same frozen captured Momentum value from Stage 15 is reused for post-fight Condition and Pigmentation probability generation; persistent Momentum is not reread during finalization.
- `CanonicalCatchStateManager.capturePerfectCatch` finalizes canonical state first, then writes that finalized specimen onto matching canonical hooked items through `CanonicalSpecimenStorage` before Tide's delivery/retrieval work proceeds.
- `SPECIMEN_PERFECT_CATCH` is therefore persisted together with the final Condition and Pigmentation before delivery.
- repeated finalization calls return the already-finalized specimen, preventing a later callback from changing the captured Perfect Catch result.
- `TideFishingHookMixin` captures the existing `retrieve(boolean perfectCatch)` parameter at method entry. Tide's center-zone detection itself is unchanged.
- canonical V2 catches bypass the reconstructed late `PerfectCatchTraitBoost` mutation at `retrieve(boolean)` return. Legacy/noncanonical catches retain that old compatibility path.
- the defensive canonical guard inside `PerfectCatchTraitBoost` remains in place as a second protection against old percentile/length mutation from other compatibility callers.

Integration coverage in `CanonicalCatchStateManagerTest#perfectCatchReachesCanonicalGenerationBeforePersistence` proves the persistence seam receives `perfectCatch = true` only after canonical finalization, while species, seed, natural percentile, base length, Body Type, final length, and final percentile remain unchanged. A repeated finalization attempt with the opposite flag returns the same already-finalized specimen and cannot overwrite the captured result.

Implementation commit `8a7f3bdc8bc4e16de926a50aff7b7e4efc7fffa0` is green in GitHub Actions run `33172897527`. The exact-dependency `./gradlew clean build --stacktrace`, unit/integration tests, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Perfect Catch +10 temporary Trait Luck is complete

Stage 17 implements only the Fishing System 2.0 Perfect Catch Trait Luck reward on the server-authoritative post-fight finalization path established in Stage 16.

Frozen reward behavior:

- `SpecimenGenerator.PERFECT_CATCH_TRAIT_LUCK_BONUS` is exactly `10.0`.
- `CanonicalCatchStateManager.CatchState` continues to compose the server-owned catch Trait Luck from `FishingContext.traitLuck()` plus the selected species' frozen `capturedTraitMomentum` value before post-fight finalization.
- when `perfectCatch` is true, `SpecimenGenerator.finalizeAfterFight` adds exactly +10 to that already-composed temporary Trait Luck value before generating Condition and Pigmentation.
- when `perfectCatch` is false, post-fight Condition and Pigmentation continue to receive the ordinary gear/context Trait Luck plus captured Momentum value unchanged.
- the +10 value is a local post-fight probability input only. It is not written back to `FishingContext`, `TraitMomentumStorage`, `CatchState.capturedTraitMomentum`, `SpecimenData`, item components, or any client-owned state.
- species selection has already completed before Perfect Catch is known, so the +10 cannot affect Fishing Luck weighting or the selected species.
- Body Type has already been generated and physical size finalized before the minigame, so Stage 17 does not reroll Body Type and does not retroactively apply +10 Trait Luck to Body Type probability.
- natural percentile, base length, final length, and final percentile remain unchanged by the Perfect Catch Trait Luck bonus.
- the existing deterministic trait salts remain unchanged; Perfect Catch alters only the probability threshold supplied to the independent post-fight Condition and Pigmentation event comparisons.
- the existing one-shot canonical finalization guard remains authoritative, so a later repeated callback cannot remove or reapply the bonus to generate a different specimen.
- the separate Perfect Catch Body Type x1.25 reward and Perfect Specimen bonus remain intentionally unimplemented for later dedicated stages.

`CanonicalCatchStateManagerTest#identicalCatchGetsExactlyTenTemporaryTraitLuckWhenPerfect` compares two otherwise identical canonical catches using the same species, specimen seed, pre-fight specimen, fight profile, gear/context Trait Luck, and captured Momentum. The fixture uses gear/context Trait Luck 7 plus Momentum 5, giving ordinary T=12 and Perfect Catch T=22. Deterministic seed `78` stays `NORMAL` for Condition at T=12 but becomes `SCARRED` at T=22, proving the +10 reward changes the actual canonical trait probability decision rather than merely setting the Perfect Catch flag. The same test proves species, seed, natural/final size state, Body Type, context Trait Luck, and captured Momentum are unchanged.

Implementation commit `a4bc250c614e53ef03694721fc044172165a52f5` is green in GitHub Actions run `33173824349`. The exact-dependency `./gradlew clean build --stacktrace`, unit/integration tests, `./gradlew runGametest --stacktrace`, and built-JAR artifact upload all completed successfully.

## Current execution gate

Stage 17 is complete. Perfect Catch now grants exactly +10 temporary Trait Luck at the server-authoritative post-fight canonical trait-generation boundary, stacking with the catch's gear/context Trait Luck and frozen per-species Momentum without modifying either source and without affecting species selection or already-finalized Body Type/size state.

Do not extend this completed stage into the Perfect Catch Body Type x1.25 reward, Perfect Specimen probability/bonus, FishScore, migration, gear progression, or later slices. Continue only with the next explicitly queued stage.

## Later frozen slices

Execute in this order unless a later explicit queued stage narrows the work further:

1. apply the remaining Perfect Catch rewards: Body Type event chance x1.25 and the later Perfect Specimen bonus
2. remaining independent Specimen Quality / Perfect Specimen implementation
3. remaining Trait Luck and rarity-compensation integration for later notable axes
4. FishScore V2 with the canonical linear 1 to 3000 mapping
5. deterministic migration and canonical `SpecimenData` adoption across persistence/UI/network systems
6. gear and Leviathan Bait progression
7. compatibility SpeciesProfiles
8. legacy cleanup
9. final build, migration, optional-mod, and runtime validation

See `docs/TODO.md` for checkbox-level execution state and `docs/FISHING_SYSTEM_2_SPEC.md` for frozen formulas and compatibility contracts.

## Stage 32 runtime legacy migration coverage is complete

This section supersedes the older execution-gate text above for the current queued repository state.

Stage 32 finishes runtime migration coverage for old saved fish data across entities, buckets, displays, the Angler's Satchel, personal Journal roots, team Journal record snapshots, and relevant record-holder/network projection boundaries.

Frozen migration contracts:

- `LegacyFishMigrationService` remains the one canonical interpreter of legacy fish state. No persistence subsystem owns a separate trait mapping, percentile calculation, size inference, or seed derivation.
- `LegacyPersistenceMigration` only bridges registered length-only Tide fish that lack enough legacy markers for the Stage 31 ItemStack classifier. Successful conversion still delegates to `LegacyFishMigrationService` and writes through `CanonicalSpecimenStorage`.
- invalid current or partially canonical payloads fail closed and never fall back to length-only reinterpretation.
- entity and bucket transfer paths migrate legacy transfer NBT before exporting or applying it, while preserving the existing transfer container and generic source-stack snapshot.
- legacy bucketable entities identify species through Tide's normal bucket mapping. Entity reload migration is idempotent because a successful transfer payload becomes canonical immediately.
- displays migrate their stored fish at the existing `setDisplayStack` boundary and use canonical `finalLength` without changing Tide's block-entity save format, model selection, orientation, placement, or removal behavior.
- Angler's Satchel reads migrate nested stored fish and commit the upgraded copies back to the existing `SatchelContents` only when migration succeeds. Failed write-back leaves the original contents intact.
- personal Journal loading performs one-time reconstructable record backfill on the persisted player root.
- team Journal canonical reads, record update paths, and display/network projections use the same journal backfill before consuming canonical record snapshots.
- old Tide journals contain aggregate largest/smallest lengths rather than complete historical specimen identity. Only those reconstructable record snapshots are backfilled. Historical latest specimen identity, missing trait axes, Perfect Catch state, and FishScore are not fabricated.
- record-holder ownership, leaderboard, and history structures that do not contain recoverable specimen identity remain unchanged. They are not reinterpreted into a second inferred specimen.
- all outer old-world save shapes remain loadable. Successful migration is write-once canonical state on the next read, so repeated reads do not reroll or regenerate specimen data.

Stage 32 integration coverage includes legacy entity/bucket round trips, length-only display migration, persisted Satchel migration and extraction, deterministic personal/team Journal record backfill, preservation of unrelated record-holder metadata, and repeated-migration stability.

The implementation/test head `0fd0301b13864b130d373b0e8e89ae4ab0e12383` is green in GitHub Actions run `33240339974`. The exact-dependency `./gradlew clean build --stacktrace`, unit tests included by the build, Fabric GameTests, and built-JAR artifact upload all completed successfully.

Detailed Stage 32 behavior is documented in `docs/STAGE_32_RUNTIME_LEGACY_MIGRATION.md`.

## Current execution gate after Stage 32

Stage 32 is complete. Do not begin later queued work from this state unless a new numbered stage explicitly authorizes it. Remaining unchecked migration and progression work is tracked in `docs/TODO.md`.

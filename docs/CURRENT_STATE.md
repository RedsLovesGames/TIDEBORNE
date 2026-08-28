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

The exact Tideborne 1.3.57 release artifact was recovered, verified, reconstructed into maintained source/resources, and built successfully before Fishing System 2.0 work began.

Frozen reconstruction anchors:

- release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- canonical files: 442
- canonical `.class` files: 272
- canonical non-class resources: 170
- reconstructed Java source files: 187

The standard validation workflow uses Java 21, fetches exact Tide 2.1.1 and Apex Waters 1.1.1 compile dependencies, then runs:

```text
./gradlew clean build --stacktrace
./gradlew runGametest --stacktrace
```

The reconstructed baseline validation was GitHub Actions run `33152569860`.

## Fishing System 2.0 completed foundation

Pure-domain and runtime work completed before the current Perfect Catch lifecycle slice includes:

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. rarity-aware Fishing Luck species selection
3. immutable canonical `SpecimenData`, direct percentile/quantile size math, and one natural specimen sample
4. normalized Strength/Tempo, catch-zone math, percentile fight scaling, and canonical `FightProfile`
5. Body Type selection, deterministic physical-size modifiers, and fight modifiers
6. independent Condition and Pigmentation axes
7. Trait Luck probability math, rarity compensation, and per-species Trait Momentum storage/progression

Implementation root:

```text
src/main/java/com/redslovesgames/tideborne/fishing/v2/
```

## Runtime selection and size authority

The canonical V2 fish selector replaces only Tide's fish-species selector inside the existing Tide fish catch category. Tide's top-level fish versus junk/crate/treasure category probability remains unchanged.

Runtime invariants that remain frozen:

- one server-owned `FishingContext` is captured for the catch attempt
- one canonical catch seed is generated
- one species is selected through the V2 selector
- Tide `FishData#getResult` is not used for canonical fish because it would perform Tide's independent hidden length roll
- one natural percentile and matching base length are generated from the canonical specimen seed
- `basePercentile` and `baseLength` are never rerolled by later trait work
- Body Type physical size starts from `baseLength`; Normal is 1.0x, Giant is deterministic 1.10x to 1.30x, and Dwarf is deterministic 0.60x to 0.82x
- `finalLength` is the physical length after Body Type size modification
- `finalPercentile` is deterministically derived from `finalLength` through the species CDF where physical size exists; it is not a second random percentile
- the canonical `FightProfile` drives Tide's minigame baseline before existing Tide line behavior and Tideborne compatibility modifiers are applied
- canonical item/entity/bucket transfer paths preserve specimen state without rerolling identity

The original runtime integration is green in run `33159465388`. Body Type fight behavior is green in run `33163133061` on commit `72c98ea3d3f79161d97720a5833d47eb4e2f4a33`.

## Canonical trait axes

### Body Type

Canonical values are `NORMAL`, `GIANT`, and `DWARF`.

- base event probability: 5%
- event probability path: base probability -> selected-species rarity compensation -> Trait Luck -> bounded probability
- Giant/Dwarf conditional bias remains separate from event probability
- `giantProbability = 0.25 + 0.50 * (naturalPercentile / 100.0)`
- event, subtype, and physical-size decisions use separate deterministic specimen-seed salts
- Giant fight modifiers: Strength x1.08, Tempo x0.95
- Dwarf fight modifiers: Strength x0.92, Tempo x1.08
- legacy P97/P3 Giant/Dwarf gates and legacy Body Type size rerolls are bypassed for canonical V2 catches

### Condition

Canonical values are `NORMAL`, `SCARRED`, and `PARASITE_RIDDEN`.

- base event probability: 5%
- event probability uses the shared rarity compensation then Trait Luck path
- triggered subtype split remains exactly 65% Scarred and 35% Parasite-Ridden
- event and subtype use independent deterministic salts
- Condition is independent from Body Type and may stack with it
- canonical Condition is mirrored to legacy `MUTATION` only for compatibility

Condition implementation is green in run `33165044512` on commit `023c9a01918f525c8a726862d7cd800bc587d9f3`.

### Pigmentation

Canonical values are `NORMAL`, `ALBINO`, and `IRIDESCENT`.

- base event probability: 1.5%
- event probability uses the shared rarity compensation then Trait Luck path
- triggered subtype split remains exactly 70% Albino and 30% Iridescent
- event and subtype use independent deterministic salts
- Pigmentation is independent from Body Type and Condition and may stack with both
- Pigmentation remains canonical-only rather than sharing the legacy Condition mutation component

Pigmentation implementation is green in run `33165703632` on commit `7fd6181c09ca0e49dd598adf5fbb39d992eb29e3`. Runtime three-axis transfer integration is green in run `33166720586` on commit `774752af3b22f7a4dcb814602f48c21cd1895779`.

## Trait Luck and rarity compensation

`TraitLuckProbabilityService` implements exactly:

```text
P' = 1 - (1 - P)^(1 + T / 10)
```

Probability safety is frozen:

- numeric probability clamps to `[0,1]`
- `NaN` probability is rejected
- exact probability endpoints 0 and 1 remain fixed
- Trait Luck below `-10` is floored at `-10`
- `NaN` Trait Luck behaves as zero
- positive infinity safely saturates nonzero/nonunit probabilities to 1

Canonical rarity trait multipliers are:

- 1 star: 1.00
- 2 star: 1.15
- 3 star: 1.40
- 4 star: 1.80
- 5 star: 2.40

`TraitProbabilityService` is the single canonical event path and fixes the order as base probability, rarity compensation, Trait Luck transform, then final bound. Fishing Luck remains mechanically separate and still affects only species selection.

Pure Trait Luck implementation commit: `7606bc21d4d5489692e210227d8c7fdeac15f339`, green run `33167369448`.

Rarity compensation implementation commit: `abf75156f906a0ef2e40b813e5b20f6870372430`.

Body Type shared-pipeline integration commits: `149aa5b9d67ec59334dae4fb3810c27db586029a` and `ee5bc701feed9aa7fff2c9c4694ed45bfcba38cb`, green run `33168959022`.

Condition/Pigmentation shared-pipeline implementation commit: `7e4a9f6804b07428c57c752e56d40e5075b7b6a2`.

## Per-species Trait Momentum

Trait Momentum is server-authoritative, persistent per player and per canonical species ID, and bounded from 0 through 15.

Frozen behavior:

- storage uses Tide's existing player-persistent NBT root under `FishingV2TraitMomentum`
- absent data defaults to zero
- malformed entries are ignored/sanitized and oversized values clamp to 15
- the selected species' Momentum is captured after species selection and before specimen trait generation
- captured Momentum is added to the server-owned Trait Luck for canonical trait-event probabilities
- later changes to persistent Momentum cannot change the already-captured value for the in-flight catch
- a fully normal specimen means Body Type, Condition, Pigmentation, and Specimen Quality are all `NORMAL`
- natural percentile/size and the Perfect Catch skill flag do not define fully normal
- a fully normal completed catch adds +1 Momentum for that species
- any notable canonical trait axis currently resets that species' Momentum to 0
- progression is applied exactly once from the completed server catch path through a one-shot catch-state guard
- invalidated/lost catches clear transient state before completion
- item/entity/serialization/UI callbacks never update Momentum

Storage implementation commit `2727ad0ae3d6a35fcab047f93412220554ceb867` is green in run `33170551133`. Progression implementation commit `c2b7422d5a29325f25f1bab692f6526dc3780ebf` is green in run `33171860987`.

## Perfect Catch pre-delivery lifecycle is complete

Stage 16 fixes the runtime ordering needed for Step 8 without implementing the later Perfect Catch reward multipliers yet.

The authoritative runtime lifecycle is now split around Tide's existing minigame:

```text
species selected
-> canonical catch/specimen seed fixed
-> natural percentile and base length generated once
-> Body Type generated
-> physical final length/final percentile produced
-> canonical FightProfile generated
-> Tide minigame runs with the existing center-zone Perfect Catch skill check
-> Tide supplies server-side retrieve(perfectCatch)
-> perfectCatch captured into canonical catch/specimen state
-> Condition generated
-> Pigmentation generated
-> finalized canonical specimen written onto the selected item
-> Tide delivery/retrieval continues
-> completed-catch Momentum progression runs once
```

Implementation details and invariants:

- `SpecimenGenerator.generatePreFight` owns the pre-fight portion needed to build the current fight: one natural specimen sample, Body Type, and Body Type physical size
- `SpecimenGenerator.finalizeAfterFight` receives the already-created pre-fight specimen and the authoritative `perfectCatch` boolean
- `perfectCatch` is copied into `SpecimenData` before Condition and Pigmentation are generated, which gives later Perfect Catch reward math the correct pre-persistence integration point
- this stage intentionally does not yet grant +10 Trait Luck, the 1.25x Body Type chance, or the Perfect Specimen bonus
- post-fight finalization preserves the selected species ID, deterministic specimen seed, `basePercentile`, `baseLength`, Body Type, `finalLength`, and `finalPercentile` exactly
- no species selection is repeated
- no natural percentile or base size is repeated
- Perfect Catch alone does not change physical length or percentile
- `CanonicalCatchStateManager.CatchState` now owns one-shot post-fight specimen finalization and retains the frozen Momentum captured at selection
- `CanonicalCatchStateManager.capturePerfectCatch` finalizes the canonical post-fight specimen first and then persists that final value onto matching canonical hooked items
- canonical persistence writes `SPECIMEN_PERFECT_CATCH` along with the final Condition and Pigmentation before Tide's delivery path continues
- repeated post-fight finalization calls return the already-finalized specimen, so a later callback cannot change a captured `true` Perfect Catch into `false` or vice versa
- `TideFishingHookMixin` captures Tide's existing `retrieve(boolean perfectCatch)` parameter at method entry instead of changing or duplicating Tide's center-zone skill check
- the reconstructed late `PerfectCatchTraitBoost` path remains available for legacy/noncanonical catches, but canonical V2 catches bypass it after successful canonical capture/finalization
- the existing defensive guard inside `PerfectCatchTraitBoost` still prevents canonical percentile/length mutation if that legacy service is reached through another compatibility path

Integration coverage in `CanonicalCatchStateManagerTest#perfectCatchReachesCanonicalGenerationBeforePersistence` proves that the persistence seam receives a specimen with `perfectCatch = true` only after canonical post-fight generation, while species, seed, natural percentile, base length, Body Type, final length, and final percentile are unchanged. It also proves a repeated finalization attempt cannot overwrite the captured Perfect Catch result.

Implementation commit `8a7f3bdc8bc4e16de926a50aff7b7e4efc7fffa0` is green in GitHub Actions run `33172897527`. The exact-dependency `./gradlew clean build --stacktrace`, unit/integration tests, Fabric GameTests, and built-JAR artifact upload all completed successfully.

## Current execution gate

The Perfect Catch lifecycle capture slice is complete. Canonical V2 catches now know and persist Tide's server-side center-zone Perfect Catch result before post-fight Condition/Pigmentation finalization and before item delivery, without rerolling species or natural size and without applying the old late canonical mutation path.

Do not extend this completed stage into the actual Perfect Catch reward math, Perfect Specimen probability, FishScore, migration, gear progression, or later slices unless the next queued stage explicitly requests that work.

The immediate Step 8 work still pending in `docs/TODO.md` is:

1. apply the frozen +10 temporary Trait Luck Perfect Catch reward at the new post-fight finalization boundary
2. implement the specified 1.25x Body Type event-chance interaction without introducing a second natural specimen sample or a late direct size mutation
3. implement the substantial non-forcing Perfect Specimen bonus
4. implement the percentile-based Perfect Specimen curve and deterministic interaction tests

Later frozen work remains FishScore V2, deterministic migration, canonical adoption across persistent/UI/network consumers, gear and Leviathan Bait progression, compatibility SpeciesProfiles, legacy cleanup, and final release validation.

See `docs/TODO.md` for checkbox-level execution state and `docs/FISHING_SYSTEM_2_SPEC.md` for frozen formulas and compatibility contracts.

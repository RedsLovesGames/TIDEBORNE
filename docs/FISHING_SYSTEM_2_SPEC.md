# Fishing System 2.0 authoritative specification

Status: authoritative implementation contract  
Baseline compatibility truth: reconstructed Tideborne 1.3.57 source, documentation, and tests  
Target branch: `dev`  
Scope rule: refactor only an affected legacy subsystem when its replacement is implemented

## Compatibility contract

Keep `main` untouched while Fishing System 2.0 is developed on `dev`.

Preserve or explicitly migrate worlds, fish, item, and recipe IDs, component and NBT IDs where practical, Satchel data, Journal and history data, team and Top Fish data, records and badges, commands, networking, external fish IDs, and Tide minigame UX. Do not invent fish. Preserve legacy specimen identity instead of rerolling it. Gameplay and specimen generation remain server-authoritative.

Do not broad-clean before implementation. For every subsystem, inspect only the relevant legacy code, implement its replacement, migrate callers and persisted data, test it, run the full build, remove only superseded code, and update the current-state documentation.

## Target domain models

### `FishingContext`

- Bite Speed
- Fishing Luck
- Trait Luck
- equipment modifiers
- bait modifiers
- environment modifiers

### `SpeciesProfile`

- species ID
- canonical rarity
- encounter weight
- eligibility and habitat
- Strength
- Tempo
- Behavior
- size distribution
- trait eligibility and affinity

### `SpecimenData`

- species ID
- schema and generation version
- deterministic seed
- base percentile
- base length
- final length
- final percentile
- Body Type
- Condition
- Pigmentation
- Specimen Quality
- Perfect Catch
- raw FishScore
- final FishScore
- provenance

### Services

- `SpeciesSelectionService`
- `SpecimenGenerator`
- `BodyTypeGenerator`
- `FightProfileService`
- `TraitGenerator`
- `SpecimenQualityService`
- `FishScoreService`

Stable consumers such as the Satchel, Journal, records, history, networking, tooltips, and teams consume canonical `SpecimenData`. They must not independently reconstruct or reroll specimen state.

## Authoritative catch pipeline

1. Build the eligible species pool and `FishingContext`.
2. Apply rarity-aware Fishing Luck.
3. Select the species.
4. Generate base percentile and length.
5. Roll Body Type.
6. Calculate physical size.
7. Build a normalized fight profile.
8. Apply size, body, gear, and bait fight modifiers.
9. Run the existing Tide minigame.
10. Determine success and Perfect Catch.
11. Roll Condition and Pigmentation using rarity compensation, Trait Luck, Momentum, and Perfect Catch.
12. Evaluate Perfect Specimen.
13. Finalize `SpecimenData` and final percentile.
14. Calculate FishScore.
15. Persist and update the Satchel, Journal, records, history, badges, and team data.

No downstream system may reroll specimen identity.

## Fishing Luck

Replace Tideborne's effective use of `selection_quality`.

For encounter weight `W`, Fishing Luck `L`, and canonical rarity coefficient `C_R`:

```text
W' = W * (1 + C_R * sqrt(max(L, 0)))
```

| Rarity | Coefficient |
|---|---:|
| 1 star | 0.00 |
| 2 star | 0.08 |
| 3 star | 0.16 |
| 4 star | 0.24 |
| 5 star | 0.32 |

At +15 Fishing Luck, the target multipliers are:

| Rarity | Multiplier |
|---|---:|
| 1 star | 1.00x |
| 2 star | 1.31x |
| 3 star | 1.62x |
| 4 star | 1.93x |
| 5 star | 2.24x |

Fishing Luck changes relative fish rarity, not overall fish-catch probability. It is separate from Trait Luck and applies consistently to Tide and compatibility fish.

## Leviathan Bait

- fish-only catches
- +15 Fishing Luck
- substantial Trait Luck
- Strength x1.15
- Tempo x1.15
- better exceptional-specimen hunting

Remove the old `selection_quality +15` behavior.

## Minigame and fight profile

Keep Tide's marker, catch region, behavior, and Perfect Catch center-zone interaction. Do not replace it with stamina, QTE, or button-mashing mechanics.

### Tempo normalization

For normalized external or legacy speed `s`:

```text
V = 0.04 + 0.085 * ln(1 + s) / ln(3.2)
```

Target values:

| Input `s` | Tempo `V` |
|---:|---:|
| 0.05 | 0.044 |
| 0.25 | 0.056 |
| 0.50 | 0.070 |
| 1.00 | 0.091 |
| 1.50 | 0.106 |
| 2.00 | 0.119 |
| 2.20 | 0.125 |

Clamp external outliers safely.

### Strength and initial catch-zone model

```text
A = 0.78 - 0.58 * S^1.25
```

Clamp safely. Strength values 0.7, 0.8, 0.9, and 1.0 must remain meaningfully different. Normalize external values before using this model.

## Size

Keep useful Tide species and lognormal distributions.

Replace the old 4096-sample percentile approximation with direct cumulative distribution function and quantile math.

Persist:

- `basePercentile`
- `baseLength`
- `finalLength`
- `finalPercentile`

`basePercentile` is the single natural specimen percentile generated with the base-size sample and is never rerolled by Body Type or later trait axes. `baseLength` is the matching natural physical length.

`finalLength` is the physical length after deterministic physical-size modifiers such as Giant or Dwarf. `finalPercentile` is the percentile implied by that `finalLength` in the same species size distribution, calculated with the direct CDF. It is a deterministic size-adjusted percentile, not a second random specimen percentile. For species with no physical-size distribution, `finalPercentile` remains equal to `basePercentile` because no meaningful size-adjusted percentile exists.

### Size fight effect

```text
X = (P - 50) / 50
strengthMult = 1 + 0.12X
tempoMult = 1 - 0.06X
```

Species remains dominant.

## Trait axes

### Body Type

- Normal
- Giant
- Dwarf

### Condition

- Normal
- Scarred
- Parasite-Ridden

### Pigmentation

- Normal
- Albino
- Iridescent

### Specimen Quality

- Normal
- Perfect Specimen

Axes may coexist.

### Rare-species trait compensation

| Rarity | Multiplier |
|---|---:|
| 1 star | 1.00 |
| 2 star | 1.15 |
| 3 star | 1.40 |
| 4 star | 1.80 |
| 5 star | 2.40 |

### Condition event

Base event probability: 5%.

- Scarred: 65% of Condition events
- Parasite-Ridden: 35% of Condition events

### Pigmentation event

Base event probability: 1.5%.

- Albino: 70% of Pigmentation events
- Iridescent: 30% of Pigmentation events

### Trait Luck

For base event probability `P` and Trait Luck `T`:

```text
P' = 1 - (1 - P)^(1 + T / 10)
```

For `P = 1%`:

| Trait Luck | Adjusted probability |
|---:|---:|
| 0 | 1.00% |
| 5 | 1.49% |
| 10 | 1.99% |
| 20 | 2.97% |
| 30 | 3.94% |

Centralize all probability calculations.

Probability safety is frozen as follows:

- clamp numeric `P` inputs to `[0, 1]`; reject `NaN` because it cannot represent an event probability
- preserve exact endpoint behavior, so `P = 0` always remains 0 and `P = 1` always remains 1
- clamp Trait Luck below `-10` to `-10`, which prevents a negative exponent and therefore prevents invalid negative adjusted probabilities
- treat `NaN` Trait Luck as 0
- negative infinity Trait Luck reaches the `-10` floor; positive infinity saturates every nonzero, nonunit probability to 1
- keep the probability calculation pure and separate from deterministic trait RNG selection
- Trait Luck must never participate in species selection; `SpeciesSelectionService` remains driven by Fishing Luck only

## Trait Momentum

Use bounded, hidden, per-species bad-luck protection:

- a mutationless or fully normal specimen adds roughly +1 temporary Trait Luck
- cap at about +15
- a notable trait substantially reduces Momentum
- first-catch traits remain possible
- server-authoritative
- bounded persistence

## Body Type generation

Initial event probability: 5%.

Upper-half natural percentile biases Giant. Lower-half natural percentile biases Dwarf.

Physical multipliers:

- Giant: 1.10x to 1.30x
- Dwarf: 0.60x to 0.82x

Use a dedicated deterministic specimen-seeded Body Type size stream. The multiplier must not consume the natural percentile/base-size RNG stream or any Condition, Pigmentation, or Specimen Quality stream.

Fight modifiers:

| Body Type | Strength | Tempo |
|---|---:|---:|
| Giant | 1.08x | 0.95x |
| Dwarf | 0.92x | 1.08x |

Condition and Pigmentation remain primarily cosmetic and trophy traits.

## Perfect Catch

Keep the center-zone skill check.

Do not alter natural size or percentile after the fight.

Initial reward:

- +10 temporary Trait Luck
- Body Type chance x1.25
- substantial Perfect Specimen bonus

## Perfect Specimen

Replace the independent lottery.

Normally require percentile at least 95.

Base curve:

| Percentile | Base probability |
|---:|---:|
| below 95 | 0% |
| 95 | 2% |
| 97.5 | 8% |
| 99 | 25% |
| 99.9 or higher | 60% |

Interpolate smoothly, then apply Trait Luck and Perfect Catch.

Legacy Perfect Specimens remain Perfect.

## FishScore V2

All legacy and new fish use one deterministic scoring system.

Preserve legacy specimen identity, but recalculate every legacy FishScore.

### Raw score

```text
RawScore = SpeciesValue + SpecimenValue + TraitValue
```

Species values:

| Rarity | SpeciesValue |
|---|---:|
| 1 star | 50 |
| 2 star | 100 |
| 3 star | 175 |
| 4 star | 250 |
| 5 star | 350 |

Specimen value:

```text
SpecimenValue = 3 * finalPercentile
```

Trait values:

| Trait | Value |
|---|---:|
| Scarred | +20 |
| Parasite-Ridden | +35 |
| Giant | +40 |
| Dwarf | +40 |
| Albino | +70 |
| Iridescent | +100 |
| Perfect Specimen | +100 |

Only compatible traits contribute together.

### Linear normalization to 1 through 3000

Canonical anchors:

- worst valid Incandescent Larva equals exactly 1
- best valid Dragon Fish at percentile 100 with the best compatible trait combination equals exactly 3000

Define and freeze:

```text
RAW_MIN = canonical minimum raw score
RAW_MAX = canonical maximum raw score

normalized = (RawScore - RAW_MIN) / (RAW_MAX - RAW_MIN)
FishScore = round(1 + 2999 * normalized)
```

Defensively clamp the result to 1 through 3000, but valid generated fish should naturally fit the anchors.

Do not use logarithmic, exponential, or sigmoid scaling.

Freeze and document `RAW_MIN` and `RAW_MAX` once established so later species additions do not rescale old scores.

Persist raw score, final score, and score breakdown.

Records remain separate from FishScore.

## Legacy migration

Do not preserve old score numbers.

Preserve:

- species
- physical size
- percentile when known or recoverable
- Giant or Dwarf
- Condition
- Pigmentation
- Perfect Specimen
- historical and catch metadata

Deterministically derive missing V2 fields and recalculate score.

Equivalent legacy and new specimens must score identically.

Update every discovered score-bearing representation:

- loose fish `ItemStack`s
- Satchels
- bucket and entity round trips
- personal records
- Top Fish and Top 12
- team records
- Journal and history
- leaderboards
- badge and history score snapshots
- persistent caches and network representations

Rebuild all score-dependent rankings afterward.

Use eager migration for centralized Tideborne save, team, Journal, Satchel, and record structures. Use lazy migration for legacy fish encountered later in inventories, chunks, entities, and buckets. Do not scan every unloaded chunk at startup.

Migration must be deterministic, idempotent, resumable, automatic, server-authoritative, and safe across reload.

Add an admin migration and rebuild command as backup tooling.

## Records

Records are separate from scoring:

- Largest
- Smallest
- Highest FishScore
- Best Percentile
- notable or rarest trait combinations where practical

## Build diversity

Existing gear should support:

- Bite Speed for fast fishing
- Fishing Luck for rare species
- Trait Luck for unusual specimens
- trophy builds combining Fishing Luck and Trait Luck with harder fights
- record builds using specimen or size effects where appropriate
- safe builds using a larger zone, lower Tempo, or defensive gear

Prefer reworking existing items over adding unnecessary new items.

## Compatibility fish

All supported fish use `SpeciesProfile`.

Normalize external:

- rarity
- encounter weight
- Strength
- Tempo
- size distribution
- trait eligibility

Do not trust external `selection_quality`, raw Speed, or raw Strength to share Tideborne semantics.

Preserve registry IDs and avoid eager optional-mod loading.

## Test requirements

Use deterministic seeded random number generation.

Eventually cover:

- rarity and Fishing Luck
- compatibility normalization
- percentile and length
- specimen generation
- Body Type
- Condition and Pigmentation
- rarity compensation
- Trait Luck
- Momentum
- Tempo and Strength normalization
- size fight modifiers
- Perfect Catch
- Perfect Specimen
- raw FishScore
- linear 1 through 3000 normalization
- minimum specimen equals 1
- maximum Dragon Fish equals 3000
- intermediate linearity
- score breakdown
- serialization
- legacy migration and recalculation
- equivalent old and new score equality
- idempotent migration
- Satchel and item migration
- Journal, team, and history migration
- Top Fish, Top 12, and leaderboard rebuild
- reload stability

## Execution order

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. species selection and Fishing Luck
3. `SpecimenData`, percentile, and size
4. fight normalization
5. Body Type
6. independent traits
7. Trait Luck, rarity compensation, and Momentum
8. Perfect Catch and Perfect Specimen
9. FishScore V2
10. legacy specimen and score migration
11. persistence, UI, team, and network integration
12. gear and bait progression
13. remove dead legacy code
14. finish documentation and tests

For every slice:

1. Inspect only relevant legacy code.
2. Implement the replacement.
3. Migrate callers and data.
4. Test.
5. Run `./gradlew build`.
6. Fix failures.
7. Remove superseded code.
8. Update `docs/CURRENT_STATE.md`.
9. Commit the logical slice.

Do not pause for approval between phases.

## Current run boundary

The run that introduced this specification is limited to execution steps 1 through 4:

- persist this authoritative specification
- update current-state and TODO documentation
- implement `FishingContext`, `SpeciesProfile`, and canonical rarity
- implement species selection and Fishing Luck
- implement `SpecimenData` and exact percentile and size math
- implement fight normalization and size fight scaling
- add deterministic tests for these systems
- run and repair the full build
- commit logical slices

Stop after step 4 is green. Leave `docs/CURRENT_STATE.md` with the exact next action for the following session.

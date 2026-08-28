# Stage 20: Perfect Specimen modifier integration

Stage 20 finishes the canonical Fishing System 2.0 Specimen Quality probability path without changing specimen percentile or physical size.

## Frozen probability order

Perfect Specimen uses the canonical final specimen percentile curve from Stage 19:

- below P95: 0%
- P95: 2%
- P97.5: 8%
- P99: 25%
- P99.9 and above: 60% base
- linear interpolation between anchors

The Quality-specific modifier order is:

1. canonical final-percentile base curve
2. total Trait Luck using `P' = 1 - (1 - P)^(1 + T / 10)`
3. direct Perfect Catch Quality bonus
4. final probability bound

The authoritative Perfect Specimen section says to interpolate the percentile curve, then apply Trait Luck and Perfect Catch. It does not name rarity compensation for Quality, so rarity compensation is intentionally not applied to this axis. This also prevents a 5-star P99.9 specimen from becoming guaranteed purely through `0.60 * 2.40` rarity scaling.

## Perfect Catch Quality bonus

The frozen specification describes the direct Perfect Catch reward as substantial but does not provide a numeric constant. Stage 20 freezes the direct Quality reward as a second opportunity against the already Trait-Luck-adjusted chance:

`Pperfect = 1 - (1 - Pluck)^2`

This squares the miss probability. It gives a large improvement while preserving the important invariants:

- a zero base chance stays zero, so specimens below P95 remain impossible
- every ordinary nonzero chance improves
- the direct Perfect Catch reward does not force certainty
- the existing Perfect Catch `+10` temporary Trait Luck is included in total Trait Luck before this direct bonus

At P99.9, a Perfect Catch with total Trait Luck 10 produces `0.9744`, not 100%.

## Canonical integration

`SpecimenGenerator.finalizeAfterFight` now passes the post-fight total Trait Luck and captured Perfect Catch flag into `SpecimenQualityService` after Body Type physical size, Condition, and Pigmentation are finalized.

Quality continues to use the dedicated `PERFECT_SPECIMEN` deterministic salt. Applying Quality copies the existing canonical specimen state and changes only `specimenQuality`. It does not rewrite:

- base percentile
- final percentile
- base length
- final length
- Body Type
- Condition
- Pigmentation
- Perfect Catch state

`CanonicalSpecimenStorage` already persists Quality independently in `SPECIMEN_QUALITY`, alongside separate canonical components for Body Type, Condition, and Pigmentation, so no persistence schema change is required in this stage.

## Coverage

Stage 20 tests lock:

- exact P95, P97.5, P99, and P99.9 anchors
- zero probability below P95 before and after modifiers
- canonical Trait Luck math at every anchor
- direct Perfect Catch probability improvement
- Perfect Catch remaining below certainty
- unchanged deterministic Quality RNG salt
- no percentile or size rewrite
- Perfect Specimen stacking with Giant, Scarred, and Iridescent on one specimen

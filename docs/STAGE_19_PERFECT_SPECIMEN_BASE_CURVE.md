# Stage 19: Perfect Specimen base curve

Stage 19 implements the canonical Fishing System 2.0 Specimen Quality axis using only the frozen percentile-based base curve. Trait Luck and any direct Perfect Catch bonus remain outside this slice.

## Frozen behavior

- Canonical Specimen Quality values remain `NORMAL` and `PERFECT_SPECIMEN`.
- Quality is evaluated from `SpecimenData.finalPercentile`, not `basePercentile`.
- Under the frozen V2 size definition, `finalPercentile` is the deterministic percentile implied by `finalLength` after physical-size modifiers. It is not a second random specimen percentile.
- Below P95, base Perfect Specimen probability is exactly 0%.
- At P95, base probability is exactly 2%.
- At P97.5, base probability is exactly 8%.
- At P99, base probability is exactly 25%.
- At P99.9 and above, base probability is exactly 60%.
- Between adjacent anchors, probability uses continuous piecewise-linear interpolation.
- The curve is capped at 60% for all valid percentiles from P99.9 through P100.
- `SpecimenQualityService` is pure and deterministic. It uses the already-reserved `TraitRandom.Salts.PERFECT_SPECIMEN` stream.
- The service never rerolls species, natural percentile, base length, final length, or final percentile.
- Applying Quality preserves every other canonical trait axis and the Perfect Catch flag.
- Post-fight canonical finalization now evaluates Specimen Quality after Body Type physical-size finalization, Condition, and Pigmentation, so the Quality curve sees the canonical final specimen percentile.
- Stage 19 intentionally does not apply Trait Luck, rarity compensation, or a direct Perfect Catch Perfect Specimen bonus to this curve.

## Interpolation

For each segment, Stage 19 uses standard linear interpolation:

`P(x) = y0 + ((x - x0) / (x1 - x0)) * (y1 - y0)`

Representative frozen midpoint values are:

- P96.25 = 5%
- P98.25 = 16.5%
- P99.45 = 42.5%

These values make the curve continuous at every anchor while preserving every specified endpoint exactly.

## Tests

`SpecimenQualityServiceTest` covers:

- every frozen anchor;
- zero probability at representative percentiles below P95, including the immediately preceding floating-point value;
- multiple interior interpolation points in all three non-flat segments;
- the 60% cap at and above P99.9;
- deterministic selection from the dedicated Perfect Specimen salt;
- invalid percentile rejection;
- proof that `finalPercentile`, rather than `basePercentile`, controls Quality;
- proof that Quality application does not rewrite base/final percentile or base/final length;
- proof that a below-P95 specimen can never become Perfect Specimen from the base curve regardless of deterministic roll.

## Execution gate

Stage 19 implements only the base Perfect Specimen curve and deterministic canonical axis selection. Do not extend this stage into Trait Luck, rarity compensation, direct Perfect Catch Quality bonus, FishScore, migration, or later slices.

# Stage 18: Perfect Catch Body Type bonus

Stage 18 applies the Fishing System 2.0 Perfect Catch Body Type event reward on the existing server-authoritative post-fight finalization path.

## Frozen behavior

- The base Body Type event chance remains exactly `0.05`.
- A Perfect Catch multiplies only that Body Type event chance by exactly `1.25`.
- The canonical probability order is: base Body Type chance, Perfect Catch event multiplier, canonical species rarity compensation, total temporary Trait Luck, final probability bound.
- The frozen Fishing System 2.0 specification requires the `1.25` Perfect Catch Body Type reward but does not freeze a conflicting modifier order, so Stage 18 follows the queued conceptual order above.
- Total temporary Trait Luck includes the already-implemented catch Trait Luck, captured per-species Momentum, and the Stage 17 Perfect Catch `+10` temporary Trait Luck when the catch is perfect.
- The Body Type event roll still uses the existing deterministic `BODY_TYPE_EVENT` salt.
- The Giant versus Dwarf conditional roll still uses the independent `BODY_TYPE_VARIANT` salt and the same smooth percentile bias function. The `1.25` multiplier never modifies that subtype distribution.
- Perfect Catch is known only after Tide resolves the minigame. Finalization therefore reevaluates Body Type from the same specimen seed and named deterministic salts using the final Perfect Catch probability inputs.
- A non-perfect finalization reproduces the pre-fight Body Type exactly because its seed, event threshold inputs, and salts are unchanged.
- A Perfect Catch can promote an otherwise Normal specimen into a Giant or Dwarf when the original deterministic Body Type event roll lies between the ordinary and Perfect Catch thresholds.
- Species selection, deterministic specimen seed, natural percentile, and base length are never rerolled.
- If the final Body Type changes, physical size is recomputed from the original base length with the existing deterministic `BODY_TYPE_SIZE` salt. This is an indirect consequence of the Body Type reward, not a separate Perfect Catch size bonus.
- The later direct Perfect Catch size reward and Perfect Specimen reward remain outside this stage.

## Probability service change

`TraitProbabilityService` now supports an optional axis-specific event multiplier inside the shared canonical path. The three-argument API delegates with multiplier `1.0`, so Condition and Pigmentation behavior is unchanged. Body Type supplies `1.25` only for a Perfect Catch.

For a five-star species at total Trait Luck `T=10`, the exact Stage 18 calculation is:

`0.05 * 1.25 * 2.40 = 0.15`

`1 - (1 - 0.15)^2 = 0.2775`

The old post-pipeline multiplication would have produced `0.282`, so the exact regression test distinguishes the required ordering.

## Tests and validation

`PerfectCatchBodyTypeTest` proves:

- the multiplier constant is exactly `1.25`;
- the exact five-star, `T=10` probability is `0.2775`;
- the required order differs from the old post-pipeline multiplication;
- deterministic seed `21` is Normal at the ordinary three-star zero-Trait-Luck threshold but becomes notable after the Perfect Catch multiplier plus the Stage 17 `+10` temporary Trait Luck;
- the same specimen seed, natural percentile, and base length survive post-fight finalization;
- the Giant versus Dwarf percentile bias function remains unchanged.

`BodyTypeGeneratorTest` was updated so its multiplier-order regression now freezes the pre-rarity, pre-Trait-Luck behavior.

Implementation commit `4c1e78047d46de711ddaa4254ad744766f26c562` passed GitHub Actions run `33174842668`. The exact-dependency Gradle build, unit and integration tests, Fabric GameTests, and built-JAR artifact upload all completed successfully.

## Execution gate

Stage 18 is complete. Do not extend this stage into the later direct Perfect Catch size reward, Perfect Specimen probability or bonus, FishScore, migration, or other later slices.
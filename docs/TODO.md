# Fishing System 2.0 implementation backlog

The authoritative behavior and formulas are in `docs/FISHING_SYSTEM_2_SPEC.md`. This file tracks execution state only.

## Current run

- [x] Persist the complete Fishing System 2.0 specification.
- [x] Record the current repository and reconstruction state.
- [ ] Restore and verify the complete reconstructed 1.3.57 source, resources, tests, and Gradle wrapper.
- [ ] Run the untouched 1.3.57 baseline tests and `./gradlew build`.
- [ ] Implement step 1: `FishingContext`, `SpeciesProfile`, and canonical rarity.
- [ ] Add deterministic step 1 unit tests.
- [ ] Commit step 1 as a logical slice.
- [ ] Implement step 2: `SpeciesSelectionService` and rarity-aware Fishing Luck.
- [ ] Remove affected `selection_quality` selection behavior only after callers are migrated.
- [ ] Add deterministic selection distribution and compatibility normalization tests.
- [ ] Commit step 2 as a logical slice.
- [ ] Implement step 3: canonical `SpecimenData`, seeded generation metadata, and direct lognormal CDF and quantile math.
- [ ] Persist base percentile, base length, final length, and final percentile without rerolls.
- [ ] Add deterministic percentile, length, round-trip, and serialization tests.
- [ ] Commit step 3 as a logical slice.
- [ ] Implement step 4: normalized Strength and Tempo, safe external clamps, initial catch-zone model, and size fight scaling.
- [ ] Preserve Tide's existing marker, catch region, behavior, and center-zone Perfect Catch interaction.
- [ ] Add normalization, target-value, boundary, and size-scaling tests.
- [ ] Run the full test suite and `./gradlew build`, then repair every failure.
- [ ] Update `docs/CURRENT_STATE.md` with verified results and the exact next action.
- [ ] Commit the green step 4 slice and stop this run.

## Next run

- [ ] Step 5: Body Type.
- [ ] Step 6: independent Condition and Pigmentation axes.
- [ ] Step 7: Trait Luck, rarity compensation, and per-species Momentum.
- [ ] Step 8: Perfect Catch rewards and percentile-based Perfect Specimen.
- [ ] Step 9: FishScore V2 and frozen linear anchors.
- [ ] Step 10: deterministic, idempotent legacy migration and score recalculation.
- [ ] Step 11: persistence, Satchel, Journal, records, history, teams, UI, and network integration.
- [ ] Step 12: gear and Leviathan Bait progression.
- [ ] Step 13: remove dead legacy code after all callers and data are migrated.
- [ ] Step 14: complete documentation, tests, runtime smoke checks, and release validation.

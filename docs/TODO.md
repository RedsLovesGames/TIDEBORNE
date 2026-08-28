# Fishing System 2.0 implementation backlog

The authoritative behavior and formulas are in `docs/FISHING_SYSTEM_2_SPEC.md`. This file tracks execution state only.

## Current run

- [x] Persist the complete Fishing System 2.0 specification.
- [x] Record the current repository and reconstruction state.
- [ ] Restore and verify the complete reconstructed 1.3.57 source, resources, baseline tests, and fixtures. The Gradle wrapper is now restored.
- [ ] Run the untouched 1.3.57 baseline tests and `./gradlew build`.
- [x] Implement the step 1 pure domain layer: `FishingContext`, `SpeciesProfile`, and canonical rarity.
- [x] Add deterministic step 1 unit tests.
- [x] Commit step 1 as part of the bounded steps 1 through 4 domain slice.
- [x] Implement the step 2 pure domain layer: `SpeciesSelectionService` and rarity-aware Fishing Luck.
- [ ] Remove affected `selection_quality` selection behavior only after callers are migrated.
- [x] Add deterministic selection distribution, eligibility, and fight normalization tests.
- [x] Commit step 2 as part of the bounded steps 1 through 4 domain slice.
- [x] Implement the step 3 pure domain layer: canonical `SpecimenData`, seeded generation metadata, and direct lognormal CDF and quantile math.
- [x] Preserve base percentile, base length, final length, and final percentile in immutable canonical specimen data without rerolls.
- [ ] Add serialization and reconstructed 1.3.57 adapter round-trip tests after legacy formats are restored.
- [x] Commit step 3 as part of the bounded steps 1 through 4 domain slice.
- [x] Implement the step 4 pure domain layer: normalized Strength and Tempo, safe external clamps, initial catch-zone model, and size fight scaling.
- [ ] Preserve Tide's existing marker, catch region, behavior, and center-zone Perfect Catch interaction.
- [x] Add normalization, target-value, boundary, and size-scaling tests.
- [x] Run all currently available tests and the full repository `./gradlew clean build` successfully.
- [x] Update `docs/CURRENT_STATE.md` with verified results and the exact next action.
- [x] Commit the green steps 1 through 4 domain slice and stop this run.

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

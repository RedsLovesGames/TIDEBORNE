# Fishing System 2.0 implementation backlog

The authoritative behavior and formulas are in `docs/FISHING_SYSTEM_2_SPEC.md`. This file tracks execution state only.

## Current run

- [x] Persist the complete Fishing System 2.0 specification.
- [x] Record the current repository and reconstruction state.
- [ ] Restore and verify the complete reconstructed 1.3.57 source, resources, baseline tests, and fixtures. The Gradle wrapper is restored, but the current staged class archive is corrupt and only yields 91 of 272 required classes.
- [ ] Supply an authoritative reconstruction input: the exact 1.3.57 JAR, a content-identical 442-file tree, or corrected complete class and resource payloads.
- [ ] Verify release JAR SHA-256 `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`, 442 actual files, 272 `.class` files, and canonical content-tree SHA-256 `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`.
- [ ] Run the untouched reconstructed 1.3.57 baseline tests and `./gradlew clean build --stacktrace`.
- [x] Implement the step 1 pure domain layer: `FishingContext`, `SpeciesProfile`, and canonical rarity.
- [x] Add deterministic step 1 unit tests.
- [x] Commit step 1 as part of the bounded steps 1 through 4 domain slice.
- [x] Implement the step 2 pure domain layer: `SpeciesSelectionService` and rarity-aware Fishing Luck.
- [ ] Integrate V2 species selection into the actual legacy catch flow after the reconstructed runtime exists.
- [ ] Remove affected `selection_quality` selection behavior only after callers are migrated.
- [x] Add deterministic selection distribution, eligibility, and fight normalization tests.
- [x] Commit step 2 as part of the bounded steps 1 through 4 domain slice.
- [x] Implement the step 3 pure domain layer: canonical `SpecimenData`, seeded generation metadata, and direct lognormal CDF and quantile math.
- [x] Preserve base percentile, base length, final length, and final percentile in immutable canonical specimen data without rerolls.
- [ ] Integrate V2 specimen generation into the actual catch flow and remove the old sampled percentile calculation after callers migrate.
- [ ] Add serialization and reconstructed 1.3.57 adapter round-trip tests after legacy formats are restored.
- [x] Commit step 3 as part of the bounded steps 1 through 4 domain slice.
- [x] Implement the step 4 pure domain layer: normalized Strength and Tempo, safe external clamps, initial catch-zone model, and size fight scaling.
- [ ] Integrate normalized Strength and Tempo into actual Tide minigame input construction.
- [ ] Preserve Tide's existing marker, catch region, behavior, and center-zone Perfect Catch interaction.
- [x] Add normalization, target-value, boundary, and size-scaling tests.
- [x] Run all currently available tests and the maintained-source repository `./gradlew clean build --stacktrace` successfully.
- [x] Recheck the reconstruction blocker against current Git history and prove the staged archive is corrupt, hash-mismatched, and historically unrecoverable from the truncated payload commit.
- [x] Exhaust retained authoritative-input recovery across File Library, connected Drive, TIDEBORNE releases/history/Actions artifacts, and available local workspace files.
- [x] Trace the Fish Wiki runtime provenance that loaded the exact 1.3.57 SHA and inspect its historical export ZIP. The export contains 1,056 entries but 0 JARs and 0 `.class` files, so it cannot recover the authoritative baseline.
- [x] Update `docs/CURRENT_STATE.md` with the verified reconstruction evidence and exact external input required to proceed.

## Blocked until Phase 0 and runtime integration are green

- [ ] Step 5: Body Type.
- [ ] Step 6: independent Condition and Pigmentation axes.
- [ ] Step 7: Trait Luck, rarity compensation, and per-species Momentum.
- [ ] Step 8: Perfect Catch rewards and percentile-based Perfect Specimen.
- [ ] Step 9: FishScore V2 and frozen linear anchors.
- [ ] Step 10: deterministic, idempotent legacy migration and score recalculation.
- [ ] Step 11: persistence, Satchel, Journal, records, history, teams, UI, and network integration.
- [ ] Step 12: gear and Leviathan Bait progression.
- [ ] Step 13: compatibility SpeciesProfiles for every actually supported Tide/compatibility fish.
- [ ] Step 14: remove dead legacy code after all callers and data are migrated.
- [ ] Step 15: complete documentation, tests, runtime smoke checks, migration rebuild validation, optional-mod absence safety, and release validation.

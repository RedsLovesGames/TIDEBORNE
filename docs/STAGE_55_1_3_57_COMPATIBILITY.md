# Stage 55 - Tideborne 1.3.57 old-world compatibility

Stage 55 validates the Fishing System 2.0 migration boundary specifically against Tideborne 1.3.57 saved data. The goal is to keep old worlds loadable while converting reconstructable fish specimens exactly once into canonical V2 state.

## Compatibility rule

There are two intentionally different legacy-data cases.

### Reconstructable fish specimens

Old ItemStacks, fish entities, bucket payloads, displays, and Angler's Satchel entries that identify a real fish specimen are migrated through the single `LegacyFishMigrationService` path.

For these fish:

- the legacy species is preserved;
- a valid legacy deterministic seed is preserved, otherwise the seed is derived deterministically from legacy identity data;
- a valid natural percentile is preserved;
- a valid physical length is preserved;
- when percentile is missing, it is inferred deterministically from the species size distribution where possible;
- Giant/Dwarf, Scarred/Parasite, Albino/Iridescent, and Perfect Specimen legacy state maps onto the matching canonical axes;
- no trait generator is called during migration;
- no natural-percentile or base-size sample is rerolled;
- canonical FishScore is calculated from the final migrated specimen through the existing `FishScoreV2Service` with no RNG;
- a successful migration writes current canonical schema state so a second read is an exact no-op.

Older partial canonical specimens are handled similarly, with one additional rule: usable saved canonical fields are restored before any missing canonical FishScore is calculated. An already-saved canonical score remains authoritative and is not replaced.

### Aggregate-only historical data

Old personal/team Journal aggregates, record-holder metadata, leaderboard/history structures, and other historical containers do not always contain enough information to recover an actual historical specimen identity.

Stage 55 does not invent a second specimen for those records. Reconstructable Journal largest/smallest size snapshots may deterministically recover species/seed/percentile/length through the shared migration service, but synthetic FishScore is stripped from those aggregate-only snapshots because the original historical traits are unknown. Existing event, contributor, record, top-fish, and ordering metadata remains preserved through the established one-time compatibility storage paths.

This distinction prevents migration from manufacturing historical traits or scores while still giving every real recoverable 1.3.57 fish a deterministic canonical score.

## Defects found and fixed

Stage 55 found three compatibility issues.

1. After legacy cached-score fallback removal, a real pre-V2 fish could migrate to canonical specimen identity without receiving canonical FishScore. `LegacyFishMigrationService` now scores the completed migrated specimen through `FishScoreV2Service`.
2. An older partial canonical ItemStack could restore saved canonical fields after the core migration score had already been calculated. `LegacyItemStackMigration` now calculates a missing score only after final preserved fields are known, while leaving an existing saved score untouched.
3. The first scoring fix also caused aggregate-only Journal reconstructed snapshots to gain a synthetic score. `JournalSpecimenStore` now explicitly keeps these historical aggregate snapshots scoreless while retaining their deterministic recovered size/percentile/seed state.

No new scoring formula, RNG stream, trait selector, or size sampler was introduced.

## Regression matrix

Stage 55 validates the requested persistence surfaces through the existing canonical integration paths:

- old fish ItemStacks: `LegacyFishMigrationServiceTest` and `CanonicalSpecimenStorageGameTests`;
- old fish entities: `LegacyEntityBucketMigrationGameTests`;
- buckets: `LegacyEntityBucketMigrationGameTests`;
- displays: `FishDisplayPersistenceGameTests`;
- Angler's Satchel: `AnglersSatchelPersistenceGameTests`;
- personal Journal: `LegacyJournalPersistenceGameTests` and `LegacyJournalMigrationTest`;
- team Journal: `LegacyJournalPersistenceGameTests` and `LegacyJournalMigrationTest`;
- event history: `StoredFishScoreStorageTest` plus the previously established canonical persisted-score consumer tests;
- leaderboards/contributor scores: `StoredFishScoreStorageTest` plus canonical leaderboard payload/consumer coverage;
- record fish/top-fish data: `StoredFishScoreStorageTest`, Journal record snapshots, and canonical record consumer coverage.

The strengthened specimen tests explicitly require a migrated real fish to contain both raw and normalized canonical FishScore and require repeated reads/transfers to remain exactly equal. Existing tests also verify preserved custom ItemStack data, Satchel extraction without duplication, preserved record ordering, malformed-data fail-closed behavior, and one-time migration semantics.

## Validation result

GitHub Actions run `33268651496` is green on the Stage 55 implementation head `6fbddf5ea0cfa6b786ef9191212e1a101046bae6`.

The Java 21 workflow successfully completed:

- exact external dependency fetch and reconstruction-identifier validation;
- `./gradlew clean build --stacktrace`, including all unit tests;
- Fabric GameTests without Apex Waters;
- Fabric GameTests with exact Apex Waters 1.1.1;
- built-JAR artifact upload.

The initial Stage 55 run exposed the aggregate-Journal score regression described above. After the boundary fix, the replacement run passed the complete gate.

## Stage 55 guarantees

For reconstructable Tideborne 1.3.57 fish, the validated migration contract is now:

- no lost fish;
- no repeated migration;
- no trait rerolls;
- no size rerolls;
- deterministic canonical migrated FishScore based on the final preserved specimen;
- existing saved canonical score is not overwritten;
- old outer persistence shapes remain loadable;
- aggregate-only historical records remain preserved rather than being converted into invented specimens.

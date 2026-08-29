# Stage 32: Runtime legacy migration coverage

Updated: 2026-08-29

Stage 32 finishes runtime migration coverage for old saved fish data across the persistence surfaces that can still contain pre-Fishing-System-2.0 specimen state.

## Canonical migration authority

All specimen interpretation remains owned by `LegacyFishMigrationService`. Runtime surfaces do not define their own trait mappings, percentile rules, size inference, or seed derivation.

`CanonicalSpecimenStorage.read` remains the normal migration boundary for legacy-marked and older-schema registered fish. `LegacyPersistenceMigration.migrateStack` adds only the missing persistence bridge for registered fish whose old save contains a valid Tide physical length but not enough Tideborne markers for `CanonicalSpecimenStorage.detectMigration` to classify it as legacy. It constructs the same `LegacyFishMigrationService.LegacyFish` input and writes the successful result through `CanonicalSpecimenStorage.write`.

Invalid current or partially canonical payloads never fall back to length-only legacy interpretation. Non-fish items never migrate.

## Entities and buckets

`SpecimenTransfer` now upgrades legacy transfer NBT through the same canonical stack migration path.

- Stack-to-entity and stack-to-bucket transfer first migrate the source fish when possible and then write the canonical transfer payload.
- Entity reload calls `SpecimenTransfer.migrateEntitySpecimen` after restoring the saved TideTraits NBT.
- A legacy bucketable fish entity resolves its species through the normal Tide bucket mapping, restores only its existing legacy transfer fields onto a temporary species ItemStack, and delegates the actual conversion to `LegacyPersistenceMigration` and `LegacyFishMigrationService`.
- Entity-to-stack and entity-to-bucket paths migrate before exporting data.
- Bucket-to-entity restoration migrates the transferred legacy specimen before applying physical length.
- Existing source-stack snapshots and unrelated transfer metadata are retained.
- Invalid canonical transfer payloads are not reinterpreted as legacy data.

Non-bucketable legacy mobs that cannot identify a fish species from their entity representation remain loadable and can still migrate later when exported through a species-specific fish ItemStack.

## Fish displays

`FishDisplayPersistenceMixin` migrates the displayed fish ItemStack at the existing `setDisplayStack` boundary. A successfully migrated display uses canonical `finalLength` for Tide's cached display length while keeping Tide's existing block-entity format, model data, orientation behavior, placement/removal flow, and full stored ItemStack.

This also covers old length-only displayed fish. No display-specific percentile or trait reconstruction exists.

## Angler's Satchel

`AnglersSatchelStorage` now treats persisted nested fish as a migration-on-read collection.

- `contents` and `size` inspect stored entries through `LegacyPersistenceMigration`.
- When at least one nested fish becomes current canonical state, the migrated copies are committed back to the existing `SatchelContents` component once.
- Inserted fish are migrated before storage when possible.
- A failed migration commit leaves the original Satchel contents intact.
- Existing specimen metadata, custom names, protection state, entry order, and capacity behavior remain intact.
- Repeated reads of already-current canonical entries do not rerun migration.

No Satchel-specific migration mapping or random generation path was added.

## Personal and team Journals

Old Tide journals retain aggregate per-species statistics rather than complete historical specimen payloads. Stage 32 therefore migrates only what can be reconstructed without inventing history.

`JournalSpecimenStore.migrateLegacyJournal` reads the existing personal `TidePlayerData` or team `journal` aggregate data and backfills canonical `largest` and `smallest` specimen sidecars when a valid physical record length exists. Each reconstructed snapshot is created through `LegacyFishMigrationService`, which deterministically derives missing seed and percentile from the preserved species and physical length.

The migration intentionally does not fabricate:

- a historical `latest` specimen;
- historical Body Type, Condition, Pigmentation, or Quality that the aggregate journal never stored;
- historical Perfect Catch state;
- raw FishScore or normalized FishScore.

Personal journal loading invokes this backfill on the persisted player root. Team journal record updates and canonical display/network projection invoke the same journal backfill before consuming the canonical sidecar. `JournalSpecimenStore.read` also performs the idempotent backfill before reading a requested snapshot.

## Record-holder, leaderboard, and history audit

Team record-holder ownership and progress/history storage were audited as part of this stage. Those structures store ownership, aggregate record values, event/history metadata, or already-projected score values rather than a recoverable legacy specimen identity.

Stage 32 preserves those structures and does not reinterpret them as fish specimens. Canonical journal snapshots are added beside them only when a source species plus record length actually exists. Existing record-holder metadata is left unchanged.

This avoids creating a second inferred specimen identity merely to populate leaderboard or history rows.

## Idempotence and old-world compatibility

Every migrated surface retains its existing outer save shape. Canonical data is written into the already-established Fishing System 2.0 ItemStack or transfer-sidecar representation, and successful migration is current-schema data on the next read.

Therefore:

- migration is write-once per stored specimen representation;
- repeated reads do not reroll seed, percentile, size, traits, or score;
- already-current canonical specimens stay authoritative;
- malformed data fails safely instead of being regenerated;
- old world, entity, bucket, display, Satchel, personal Journal, and team Journal containers remain loadable.

## Tests and validation

Stage 32 adds or updates integration coverage for:

- legacy entity NBT migration through stack and bucket transfer;
- legacy bucket data migration and canonical round-trip stability;
- length-only legacy fish display migration;
- persisted Angler's Satchel migration and write-back;
- migrated Satchel extraction preserving canonical identity and unrelated ItemStack metadata;
- deterministic legacy journal record migration;
- personal journal migration on normal load;
- team journal largest/smallest record backfill;
- no fabricated latest specimen or FishScore for aggregate-only old journal data;
- preservation of unrelated record-holder metadata;
- repeated journal migration producing no additional changes;
- updated pre-Stage-32 persistence tests asserting canonical values rather than stale compatibility mirrors.

GitHub Actions run `33240339974` is green for implementation/test head `0fd0301b13864b130d373b0e8e89ae4ab0e12383`. The exact-dependency `./gradlew clean build --stacktrace`, unit tests included by the build, Fabric GameTests, and built-JAR artifact upload all completed successfully.

Primary Stage 32 implementation/test commits are `c05b696f` (`feat(stage-32): migrate legacy persistence surfaces`), `2ef880d2` (`test(stage-32): assert canonical legacy outcomes`), and `0fd0301b` (`fix(stage-32): migrate legacy personal journal on load`), with the intermediate Stage 32 test expansion retained in history.

Stage 32 is complete. Later work may decide where migrated historical records should receive recalculated FishScore, but this stage deliberately does not invent score or trait history that the old persistence format did not contain.

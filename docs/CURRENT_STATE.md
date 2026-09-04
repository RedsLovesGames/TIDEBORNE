# Current development state

Updated: 2026-09-04

## Baseline and branch

- compatibility baseline: Tideborne 1.3.57
- current published release: Tideborne 2.0.0
- current development version: Tideborne 2.0.1
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- reconstruction branch: `reconstruct-1.3.57`
- `main` remains untouched unless explicitly authorized
- authoritative Fishing System 2.0 contract: `docs/FISHING_SYSTEM_2_SPEC.md`

Frozen reconstruction anchors:

- Tideborne 1.3.57 release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- reconstructed canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- Tide 2.1.1 Fabric 1.21.1 SHA-256: `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Apex Waters 1.1.1 Fabric 1.21.1 SHA-256: `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`

Historical stage-by-stage implementation details remain available in the dedicated stage documents and Git history. This file records the current authoritative state.

## Exact published 2.0.0 build

The public 2.0.0 release is frozen legacy release state.

- GitHub release/tag: `TIDEBORN-2.0.0`
- exact release commit: `6f1d2d0c67f38c5d5924f3c3572babe0fe6d2feb`
- artifact: `tideborne-2.0.0.jar`
- artifact size: `1,087,782` bytes
- artifact SHA-256: `1f69f32fbb1bb85675dffddb5b585f33ab8f31a0495d31c3177a3f0d26190637`

Future `dev` commits must not retarget that release or replace its JAR.

## Release versioning policy

`.github/workflows/build.yml` keeps normal pushes and pull requests CI-only. Public release publication is reachable only from an explicit numeric semantic-version tag that exactly matches `gradle.properties`. Tagged publication validates the tagged commit itself, refuses an existing release/tag target, does not use `--clobber`, and records the exact commit and JAR digest.

The old `TIDEBORN-2.0.0` name remains only as the legacy 2.0.0 release identifier. New releases use exact semantic versions such as `2.0.1`.

The normal streamlined validation path was revalidated at `3308ab5610ff121f13d12a03532c60c41c1f831e`, GitHub Actions run `33831374450`. That run passed dependency/checksum validation, repository/version validation, clean Gradle build/unit tests, core GameTests, production JAR validation, final validation-count reporting, and CI artifact upload. Optional-mod matrices and dedicated-server smoke are intentionally not part of ordinary `dev` pushes.

## Fishing System 2.0

Fishing System 2.0 through Stage 64 is complete on `dev`, including later Fishing Journal and Team Top 15 presentation fixes.

Canonical runtime authority includes:

- server-owned Tide species selection using canonical Fishing Luck weighting and Tide eligibility restrictions;
- one canonical natural specimen percentile/base-size sample per catch;
- independent deterministic Body Type, Condition, Pigmentation, and Specimen Quality axes;
- canonical final physical size and size-adjusted final percentile without a second specimen sample;
- canonical Perfect Catch and Perfect Specimen behavior;
- server-owned per-player, per-species Trait Momentum;
- canonical FishScore V2 as the production score source;
- canonical Strength, Tempo, line, Steel Leader, rod, hook, bait, and Leviathan Bait behavior;
- canonical ItemStack, entity, bucket, display, Satchel, Journal, record, leaderboard, and network persistence/projection paths;
- deterministic one-way migration for recoverable Tideborne 1.3.57 fish and saved-data representations;
- guarded legacy compatibility paths that cannot reroll or overwrite current canonical V2 state;
- canonical Best Specimen per species and derived Team Top 15 records;
- replay-safe record recovery that does not replay live-catch side effects;
- exact namespaced canonical fishing-gear identity;
- dedicated Tideborne creative tab and optional-mod visibility matrix.

## Post-2.0 architecture

### Canonical internal fishing API

`com.redslovesgames.tideborne.api.TideborneFishingApi` is the stable internal read/query boundary for canonical specimen reads, stored FishScore reads, fishing-gear modifier queries, Tide species profiles, and canonical record/Top Fish reads.

Initial implementation commit: `7631fb1f3493b709a2bc2e96d01d1c2dd7b3e910`.
Initial GitHub Actions run: `33861656348`.
Boundary documentation: `docs/TIDEBORNE_INTERNAL_API.md`.

The facade returns existing canonical domain records rather than creating a second representation. It is intentionally read/query-only. FishScore calculation remains owned by `FishScoreV2Service`, and fishing-gear registry authoring remains owned by `FishingGearRegistry`.

### Canonical specimen presentation

`com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation` is the shared read-only presentation contract for canonical trait order/names/colors, length, percentile, FishScore, and rarity stars.

Covered consumers include the Fishing Journal, Team Top 15/Top Fish, team records, Satchel detail/record views, normal fish tooltips, Tide Fish Profile overlays, and operator fishing inspection.

Final presentation validation head: `7e2498542cd21215c6c931cca208ff8b4337963b`.
GitHub Actions run: `33892874198`.

No meaningful independent specimen-presentation implementation is intentionally retained. Layout, clipping, localized screen text, and timestamps remain UI responsibilities while specimen semantics come from the canonical presentation layer.

### Tide mixin inventory and Stage 3 reduction

The mixin architecture is documented in `docs/TIDE_MIXIN_INVENTORY.md`.

A Stage 2 classification error was corrected during Stage 3: `LegacyFishScoreCalculatorMixin` targets Tideborne-owned `TraitAxesRuntime`, not Tide. The corrected pre-Stage-3 baseline was 41 configured mixins: 20 Tide targets, 11 vanilla targets, 9 Tideborne-owned targets, and 1 optional Apex Waters target.

Stage 3 removed one actual Tide dependency and consolidated overlapping Team Journal lifecycle logic:

- `tideteamjournal.mixin.client.SyncPlayerDataMsgMixin` was deleted and removed from the active config;
- journal record-holder and canonical display-specimen metadata now arrive through Tideborne-owned `RecordHoldersPayload` instead of intercepting Tide's `SyncPlayerDataMsg.handle`;
- `TeamJournalCatchBridge` owns crate canonicalization, catch snapshots, post-save capture, record capture, and TeamProgressStore catch-context cleanup;
- `tideteamjournal.mixin.TidePlayerDataMixin` and `tideteamjournal.mixin.TideUtilsMixin` remain only as Tide lifecycle adapters into that shared bridge.

After Stage 3, the active configuration contained 40 mixins: 19 Tide targets, 11 vanilla Minecraft targets, 9 Tideborne-owned targets, and 1 optional Apex Waters target.

Stage 3 implementation/test head: `e55d19721bb8094c514df471d2d432540626b603`.
GitHub Actions run: `33898441943`.

That exact normal `dev` run completed successfully. It passed dependency/checksum and repository/version validation, the clean Gradle build and unit tests, the core no-optional-mod GameTests, production JAR validation, final validation-count reporting, and CI artifact upload. The optional Apex/Myths matrices and dedicated-server smoke were intentionally skipped by the streamlined normal-push policy, and the release-publication job was skipped.

### Stage 4 legacy and package cleanup

Stage 4 is complete. Detailed ownership and migration-preservation rules are documented in `docs/STAGE_4_LEGACY_PACKAGE_CLEANUP.md`.

High-confidence dead implementation removed in this pass:

- `LegacyFishScoreCalculatorMixin` was removed from `tide_traits.mixins.json` and deleted from source;
- `TraitAxesRuntime.score(...)` and `scoreFromParts(...)` remain only as compatibility signatures and now return `-1.0` directly, so the reconstructed pre-V2 FishScore formula is no longer hidden behind a Tideborne-owned self-mixin;
- `FishSatchelConversionMixin.java` was deleted because it was already absent from every active mixin config and therefore had no runtime role.

Production FishScore remains exclusively owned by `FishScoreV2Service`. Stage 4 does not remove legacy score storage keys, one-way score migration reads, legacy trait/body/condition components, legacy size recovery helpers, species/fight compatibility inputs, or stable registry/component/NBT/network/item/recipe identifiers that old saves or compatibility paths still require.

The package review keeps the four historical roots because they still represent meaningful boundaries rather than cosmetic namespaces:

- `com.redslovesgames.tideborne` is the canonical home for Fishing System 2.0 domain logic, services, internal APIs, presentation, and new shared architecture;
- `com.redslovesgames.tideboundcompatibility` remains the external Tide and optional-mod integration boundary;
- `com.redslovesgames.tideteamjournal` remains the Team Journal/FTB Teams persistence, records, UI, and networking subsystem;
- `com.redslovesgames.tidetraits` remains the historical traits, Satchel, discovery, transfer, rendering, and persisted compatibility boundary.

A broad package rewrite is intentionally rejected. New canonical Fishing System 2.0 behavior should live under `tideborne`, while the historical roots increasingly act as subsystem owners or adapters. Persisted identifiers must not move merely because Java packages do.

Behavior-rich Tideborne-owned self-mixins remain where they still carry meaningful Team Journal indexing/projection/storage or cross-package compatibility behavior. They are internal architecture debt, not Tide version coupling, and should only be replaced when their owner classes can absorb the behavior with focused regression coverage. `TideTeamJournalServiceMixin` remains an explicit later focused bridge-cleanup candidate.

After Stage 4, the active configuration contains 39 mixins:

- 19 Tide targets;
- 11 vanilla Minecraft targets;
- 8 Tideborne-owned targets;
- 1 optional Apex Waters target.

Stage 4 validated code/test head: `f0819b9fc384a256a0d525e7df5e9578a6926652`.
GitHub Actions run: `33900568570`.

That exact normal `dev` run passed dependency/checksum validation, repository/version validation, clean Gradle build and unit tests, core no-optional-mod GameTests, production JAR validation, validation-count reporting, and CI artifact upload. The Apex/Myths optional matrices and dedicated-server smoke were intentionally skipped by normal-push policy, and release publication was skipped.

The first Stage 4 validation attempt correctly exposed an outdated `LegacyFishScoreRemovalTest` that still asserted the removed self-mixin mechanism. The test was updated to assert the new compatibility contract instead: no registered legacy score self-mixin, both old score signatures disabled at `-1.0`, and Team Journal canonical score projection still registered. The successful run above validates the repaired contract.

### Real Tide balance simulator

The authoritative tuning simulator uses the live Tide 2.1.1 species catalog rather than the synthetic equal-rarity pool.

Implementation head: `918b5fce07036b888cebb505ffec4faebeb1f0af`.
GitHub Actions run: `33832555397`.
Authoritative report: `docs/FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`.

The runtime-backed harness loads all 106 Tide fish, preserves actual eligibility/rarity/weight/size/strength/speed metadata, samples representative environments, and measures rarity, FishScore, notable traits, Perfect Specimen, Fishing Luck, Trait Luck, and progression behavior. The validation run passed 282 unit tests and all 59 required core GameTests.

The historical `docs/FISHING_SYSTEM_2_BALANCE_REPORT.md` remains a deterministic synthetic regression record, not the authoritative real-content tuning report.

## Detailed references

- `docs/FISHING_SYSTEM_2_SPEC.md`
- `docs/STAGE_57_58_FINAL_RELEASE_VALIDATION.md`
- `docs/STAGE_59_FISHING_UI_POLISH.md`
- `docs/FISHING_RECOVERY.md`
- `docs/STAGE_60_61_RECOVERY_AND_FINAL_POLISH.md`
- `docs/STAGE_63_TIDEBORNE_CREATIVE_TAB.md`
- `docs/FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`
- `docs/FISHING_SYSTEM_2_BALANCE_REPORT.md`
- `docs/TIDEBORNE_INTERNAL_API.md`
- `docs/TIDE_MIXIN_INVENTORY.md`
- `docs/STAGE_4_LEGACY_PACKAGE_CLEANUP.md`

## Current execution gate

- `dev` is the active development branch at version 2.0.1.
- 2.0.0 remains frozen to the exact published commit and artifact digest above.
- normal `dev` pushes are CI-only and do not publish releases.
- the next public release must come from an explicit semantic-version tag matching `gradle.properties` exactly.
- real Fishing System 2.0 tuning must use `docs/FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`.
- new covered fishing read/query features should prefer `TideborneFishingApi`.
- canonical specimen presentation migration is complete.
- Tide-targeting mixin inventory, Stage 3 focused Tide reduction, and Stage 4 legacy/package cleanup are complete and validated.
- the planned post-2.0 architecture sequence is complete. Future cleanup should follow the documented ownership boundaries rather than begin a broad namespace rewrite.
- remaining non-architecture backlog is the repository-level Immutable Releases setting, long-term source-distribution license decision, and future Minecraft/Fabric/Tide/optional-mod compatibility work as needed.
- `main` must not be merged, rebased, or modified unless explicitly authorized.

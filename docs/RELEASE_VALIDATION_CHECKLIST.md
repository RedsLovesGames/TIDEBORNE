# Tideborne Release Validation Checklist

Use this checklist before starting a release-candidate phase and before publishing a Tideborne JAR.

## 1. Baseline and integration

- [x] P10 runtime hardening is present on `dev`.
- [x] P10 UI/presentation is integrated into `dev`.
- [x] `dev` passed the normal Java 21 build after P10 integration.
- [x] `dev` passed the Java/AI quality gate after P10 integration.
- [ ] Record the final candidate commit SHA and JAR SHA-256.

## 2. Automated runtime matrix

The `P10.5 Runtime Matrix` workflow must pass all four runtime combinations:

- [ ] Required dependencies only
- [ ] Apex Waters only
- [ ] Myths of the Sea only
- [ ] Apex Waters + Myths of the Sea

Each matrix leg must pass:

- repository validation;
- unit tests;
- all registered Fabric GameTests;
- dedicated-server startup;
- real client initialization under Xvfb;
- a real Minecraft multiplayer login using `--quickPlayMultiplayer`;
- clean client/server classloading and Mixin checks.

The matrix intentionally sets `includeApexRuntime` and `includeMythsRuntime` independently. GameTest bootstrap assertions must agree with the runtime that is actually present.

## 3. Persistence and old-world compatibility

These are release blockers and are covered by the registered GameTests. Do not replace them with hand-written approximations.

- [ ] Current canonical specimen data survives item/entity transfers unchanged.
- [ ] Legacy mutation/body-type specimen data migrates once and is then idempotent.
- [ ] Legacy living-fish entity specimen data migrates in place without changing seed, percentile, physical size, Body Type, or Condition.
- [ ] Legacy bucket specimen data migrates when released.
- [ ] Legacy personal Journal roots backfill reconstructable canonical records.
- [ ] Legacy team Journal roots backfill reconstructable canonical records without changing unrelated record-holder metadata.
- [ ] Owned length-only legacy fish backfill the missing Journal specimen without replaying a catch.
- [ ] Uncaught species are not unlocked by migration/backfill.
- [ ] Mixed Satchel contents migrate only entries that are safely migratable and do not mutate original stacks.
- [ ] Satchel physical presets survive encode/decode, preserve unknown future keys, and reject malformed data without erasing it.
- [ ] Legacy Satchel preset rod references migrate once, preserve missing references, and do not duplicate/loss items.

Relevant automated coverage includes:

- `LegacyFishMigrationServiceTest`
- `LegacyEntityBucketMigrationGameTests`
- `LegacyFishRecoveryGameTests`
- `LegacyJournalPersistenceGameTests`
- `AnglersSatchelPersistenceGameTests`
- `CanonicalEntityTransferGameTests`
- `CanonicalSpecimenStorageGameTests`
- `FishDisplayPersistenceGameTests`
- `FreshWorldRegressionGameTests`

## 4. Gameplay smoke coverage

Automated GameTests/unit tests must remain green for:

- [ ] normal specimen generation;
- [ ] deterministic RNG and specimen identity;
- [ ] Normal / Giant / Dwarf Body Types;
- [ ] Condition traits;
- [ ] Pigmentation traits;
- [ ] Perfect Specimen quality;
- [ ] Perfect Catch behavior;
- [ ] Trait Momentum;
- [ ] FishScore V2;
- [ ] fishing gear registry and Tide integration;
- [ ] lines, leaders, hooks, bobbers, bait and related modifiers;
- [ ] Satchel storage, protection, sorting, presets, upgrades and records;
- [ ] Journal discovery/history/records;
- [ ] optional compatibility absence-safety and presence behavior;
- [ ] crate/progression bridge behavior;
- [ ] dedicated-server safety.

Do not manually re-test every mathematical combination that is already deterministic unit-test coverage. Manual testing is reserved for presentation and human interaction.

## 5. Manual client visual sign-off

These checks require a real interactive Minecraft client and cannot be truthfully replaced by source inspection or a headless CI boot.

Test at minimum:

- [ ] 16:9, normal GUI scale
- [ ] 16:9, small GUI scale
- [ ] 16:9, large GUI scale
- [ ] ultrawide
- [ ] compact/windowed layout

Inspect:

- [ ] Tideborne Settings: six root categories fit, nested groups are readable, search works, no overlap/clipping.
- [ ] Angler's Satchel Contents: title, grid, selected fish panel, footer and tooltips align.
- [ ] Satchel Sorting: locked/disabled state is obvious and cannot mutate draft rules.
- [ ] Satchel Upgrades: enabled state, costs, max state and unavailable prerequisites are readable.
- [ ] Satchel Records: long names, hover text, scrollbar, range text and empty state are correct.
- [ ] Satchel keyboard navigation: `1`-`4`, Tab/Shift+Tab, arrows, Page Up/Page Down, Home/End.
- [ ] Bobber and leader advanced tooltips are useful and do not duplicate native Tide text.
- [ ] Advanced Tooltips still show normal Minecraft raw IDs/component counts.
- [ ] No mouse/hover/click offset appears when the Satchel is scaled down.

Record screenshots for any failure before changing code.

## 6. Manual multiplayer gameplay sign-off

CI now proves that a real 1.21.1 client can initialize and complete a dedicated-server login. Before release, also perform one human gameplay session with two clients when practical:

- [ ] Client A catches a fish and server owns the generated specimen identity.
- [ ] Client A stores/protects/sorts the specimen in the Satchel.
- [ ] Client A sees Journal/history/record updates.
- [ ] Client B joins the same team and receives the expected team/Top Fish state.
- [ ] Disconnect/reconnect preserves specimen, Satchel and Journal state.
- [ ] Server restart preserves the same state.
- [ ] Config/network sync behaves correctly after join and reconnect.

This human session is for interaction/synchronization confidence. It does not replace automated persistence, migration, networking or deterministic-mechanics tests.

## 7. Release artifact

- [ ] `./gradlew clean build --stacktrace` succeeds on Java 21.
- [ ] Expected unit-test count is reported and all tests pass.
- [ ] `scripts/validate_repository.sh` passes.
- [ ] Java/AI quality gate passes.
- [ ] Runtime matrix passes all four combinations.
- [ ] Production `tideborne-<version>.jar` passes release-artifact validation.
- [ ] Dedicated-server + client-login smoke passes.
- [ ] Final JAR SHA-256 recorded in release notes.
- [ ] No temporary worker-branch workflow triggers remain.

## P10.5 notes

P10 integration was fast-forwarded into `dev` from `agent/p10-ui-presentation` because the worker branch was ahead of `dev` with no divergence. The normal post-integration Build Tideborne and Java/AI Quality Gate workflows both passed on `4de98e4ef581d0e27685ed5a8141bfc218400f15`.

P10.5 adds the runtime matrix and changes the dedicated client connection smoke from the removed `--server` / `--port` arguments to Minecraft Quick Play (`--quickPlayMultiplayer`).

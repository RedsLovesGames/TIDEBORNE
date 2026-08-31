# Tideborne post-2.0 backlog

Updated: 2026-08-31

Fishing System 2.0 implementation, legacy recovery tooling, final integration polish, owned legacy-fish Journal backfill, canonical gear-registry hardening, the dedicated Tideborne creative tab, and the Stage 64 record/balance audit are complete on `dev`.

The authoritative behavior and validation record are documented in:

- `docs/FISHING_SYSTEM_2_SPEC.md`
- `docs/CURRENT_STATE.md`
- `docs/FISHING_SYSTEM_2_BALANCE_REPORT.md`
- `docs/STAGE_57_58_FINAL_RELEASE_VALIDATION.md`
- `docs/STAGE_59_FISHING_UI_POLISH.md`
- `docs/FISHING_RECOVERY.md`
- `docs/STAGE_60_61_RECOVERY_AND_FINAL_POLISH.md`
- `docs/STAGE_63_TIDEBORNE_CREATIVE_TAB.md`

## Fishing System 2.0

- [x] Complete the canonical species, specimen, traits, Momentum, score, persistence,
  migration, gear, compatibility, Journal, records, and networking paths.
- [x] Remove or guard duplicate legacy runtime calculations so canonical V2 state is
  authoritative for V2 catches and legacy code is used only for migration or explicitly
  supported noncanonical compatibility fallback.
- [x] Validate normal server fishing, server-owned specimen generation, server-owned
  Momentum, client request boundaries, team Journal sync, record/leaderboard projections,
  optional-mod matrices, and dedicated-server classloading.
- [x] Produce and validate the `tideborne-2.0.0.jar` production artifact.
- [x] Publish the validated artifact as `TIDEBORN-2.0.0` from the validated `dev` commit.
- [x] Complete Stage 59 Fishing System 2.0 UI correctness and polish, including canonical
  score projections, shared formatting, structured History rows, leaderboard and Top Fish
  layouts, species display cleanup, and Angler's Satchel clarity.
- [x] Complete Stage 60 legacy fish recovery tooling with identity-preserving repair and an
  explicitly confirmed destructive reroll path.
- [x] Complete Stage 61 final integration polish, including Top Fish specimen-detail layout
  cleanup and canonical progression for legitimate fish entering Tide's normal catch-accounting
  path from crate rewards.
- [x] Register and execute the Stage 60 and Stage 61 GameTests in every required compatibility
  matrix.
- [x] Backfill missing canonical Journal specimen display data from actual old fish a player
  still owns, including length-only Tide fish that predate Tideborne specimen components, without
  replaying catches, incrementing progression, unlocking uncaught species, or overwriting an
  existing canonical latest specimen.
- [x] Consolidate continuous validation on the current 2.0.0 build workflow, retire
  reconstruction-era and one-off release workflows, and pin every CI runtime dependency by
  SHA-256.
- [x] Complete Stage 62 canonical fishing-gear registry hardening: exact namespaced registered-item
  IDs now drive native Tide line resolution, Tideborne line/hook/rod recognition, advanced gear
  tooltips, and operator diagnostics; regression tests cover complete registration, unregistered
  lookalikes, metadata/reverse lookup, and composed gear stacking.
- [x] Complete Stage 63 dedicated Tideborne creative tab: keep all existing item IDs stable, use
  the Angler's Satchel as the tab icon, preserve optional-mod visibility rules, curate Tideborne
  gear by gameplay role, remove duplicate Tideborne entries from vanilla Tools/Ingredients, and
  lock the exact four-state Myths/Apex item matrix with registered GameTests.
- [x] Complete Stage 64 canonical record and balance audit: persist a deterministic Best Specimen
  per species, derive Team Top 15 from canonical Best Specimens, suppress duplicate canonical
  record identity, make recovery rebuild record projections without replaying live-catch side
  effects, drive records preview and Journal details from canonical specimen state, and cover the
  built-in rods, lines, bobbers, hooks, bait, Steel Leader, Leviathan Bait, and representative
  stacking paths with deterministic tests.

Stages 60 and 61 are validated on implementation head
`9619f756c9ecd61139acd5ffc687d68be61a4e04` by GitHub Actions run `33319707597`.
The clean build, unit tests, all four Fabric GameTest matrices, dedicated-server/client smoke,
production JAR validation, artifact upload, and release publication passed.

The owned legacy-fish Journal backfill is implemented at
`44c4803f8f6bee16eb76b162b82883398a8bd3ca` and validated by GitHub Actions run
`33323297138`. The clean build, unit tests, all four Fabric GameTest matrices,
dedicated-server/client-connect smoke test, production JAR validation, artifact upload, and
release refresh passed.

Stage 63 is validated on implementation head
`24cc3a42e30f9dc8a51bf9abef469585e0d2b48b` by GitHub Actions run `33360256194`. The build/unit
suite, all four Fabric GameTest compatibility matrices, dedicated-server/client-connect smoke,
production JAR validation, artifact upload, and release refresh passed.

Stage 64 implementation is validated on head
`f6aac3de1277500229428d37a69faf1c5eaf7d9a` by GitHub Actions run `33367391365`. Clean build,
unit tests, all four Fabric GameTest compatibility matrices, dedicated-server smoke,
client-connect smoke, production JAR validation, artifact upload, and release publishing passed.

No known Fishing System 2.0 blocker or unfinished Fishing System 2.0 implementation item remains.

## Remaining non-Fishing-System-2.0 work

- [ ] Decide and document the long-term source-distribution license. The repository root
  currently uses MIT while historical packaged metadata contains an All Rights Reserved
  declaration; this policy decision does not affect the built mod's runtime validation.
- [ ] Handle future Minecraft, Fabric, Tide, or optional-mod version upgrades as separate
  compatibility work. The 2.0.0 release remains frozen to the versions documented in
  `docs/VALIDATION.md`.

# Tideborne post-2.0 backlog

Fishing System 2.0 implementation is complete. The authoritative behavior remains in
`docs/FISHING_SYSTEM_2_SPEC.md`, and the completed release gate is documented in
`docs/STAGE_57_58_FINAL_RELEASE_VALIDATION.md`.

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
- [x] Publish the validated artifact as `TIDEBORN-2.0.0` from the final `dev` commit.

No known Fishing System 2.0 blocker or unfinished Fishing System 2.0 implementation item
remains.

## Remaining non-Fishing-System-2.0 work

- [ ] Decide and document the long-term source-distribution license. The repository root
  currently uses MIT while historical packaged metadata contains an All Rights Reserved
  declaration; this policy decision does not affect the built mod's runtime validation.
- [ ] Handle future Minecraft, Fabric, Tide, or optional-mod version upgrades as separate
  compatibility work. The 2.0.0 release remains frozen to the versions documented in
  `docs/VALIDATION.md`.

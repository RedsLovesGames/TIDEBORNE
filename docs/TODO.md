# Tideborne post-2.0 backlog

Updated: 2026-09-03

Fishing System 2.0 implementation, recovery tooling, integration polish, canonical gear identity, the dedicated Tideborne creative tab, Stage 64 record/balance work, and the release-versioning correction are complete on `dev`.

The authoritative current state is documented in `docs/CURRENT_STATE.md`.

## Fishing System 2.0

- [x] Complete canonical species, specimen, traits, Momentum, score, persistence, migration, gear, compatibility, Journal, records, and networking paths.
- [x] Guard duplicate legacy runtime calculations so canonical V2 state remains authoritative.
- [x] Validate server authority, multiplayer synchronization, optional-mod matrices, and dedicated-server behavior.
- [x] Complete Stages 59 through 64 and the later Fishing Journal and Team Top 15 presentation fixes.
- [x] Freeze the exact public 2.0.0 build identity in `docs/CURRENT_STATE.md`.

## Release versioning

- [x] Stop publishing or refreshing a GitHub Release on normal `dev` pushes.
- [x] Advance `dev` to semantic version `2.0.1` so the frozen 2.0.0 artifact is no longer reused by development builds.
- [x] Require exact `MAJOR.MINOR.PATCH` versions in `gradle.properties`.
- [x] Publish only from an explicit numeric semantic-version tag matching `mod_version` exactly.
- [x] Build and validate the release artifact from the tagged commit itself.
- [x] Refuse to modify, retarget, upload over, or `--clobber` an existing GitHub Release.
- [x] Record the release commit SHA and artifact SHA-256 in release metadata.
- [x] Keep normal `dev`, `main`, reconstruction, pull-request, and manual-dispatch runs CI-only.
- [ ] Enable GitHub repository-level Immutable Releases for server-enforced protection against manual asset replacement or tag movement. The workflow already refuses replacement; this GitHub repository setting adds platform-level enforcement.

## Remaining non-Fishing-System-2.0 work

- [ ] Decide and document the long-term source-distribution license. The repository root currently uses MIT while historical packaged metadata contains an All Rights Reserved declaration.
- [ ] Handle future Minecraft, Fabric, Tide, or optional-mod version upgrades as separate compatibility work.

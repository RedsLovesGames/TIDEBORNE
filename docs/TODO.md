# Tideborne post-2.0 backlog

Updated: 2026-09-04

Fishing System 2.0 implementation, recovery tooling, integration polish, canonical gear identity, the dedicated Tideborne creative tab, Stage 64 record/balance work, release-versioning correction, canonical internal fishing API, canonical specimen presentation migration, Tide-targeting mixin inventory, focused Tide mixin reduction, the legacy/package cleanup pass, and the existing Fishing System 2.0 gear-path audit are complete on `dev`.

The authoritative current state is documented in `docs/CURRENT_STATE.md`.

## Fishing System 2.0

- [x] Complete canonical species, specimen, traits, Momentum, score, persistence, migration, gear, compatibility, Journal, records, and networking paths.
- [x] Guard duplicate legacy runtime calculations so canonical V2 state remains authoritative.
- [x] Validate server authority, multiplayer synchronization, optional-mod matrices, and dedicated-server behavior.
- [x] Complete Stages 59 through 64 and the later Fishing Journal and Team Top 15 presentation fixes.
- [x] Freeze the exact public 2.0.0 build identity in `docs/CURRENT_STATE.md`.

## Post-2.0 gear progression

- [x] Audit all existing Fishing System 2.0 gear paths, IDs, modifiers, effect stages, canonical specimen boundaries, duplicated logic, hard-coded checks, Tide mixins, Satchel behavior, and APIs to preserve. See `docs/GEAR_PROGRESSION_AUDIT.md`.
- [ ] Define the intended post-2.0 gear progression in a separate design step. Do not change gameplay until the coordinated design is frozen.
- [ ] Implement the coordinated gear rework only after the design step, preserving server-authoritative specimen generation and the audited compatibility IDs/API boundaries.

## Post-2.0 architecture goals

- [x] Create a small canonical Tideborne fishing API layer for specimen reads, stored FishScore reads, gear modifier queries, species profiles, and record reads. Boundary documented in `docs/TIDEBORNE_INTERNAL_API.md`.
- [x] Create one canonical specimen presentation layer for trait names/order, colors, length/percentile formatting, FishScore formatting, and rarity stars, then migrate covered UI consumers without rebuilding trait state per screen.
- [x] Inventory all active Tide-targeting mixins, separate Tide coupling from vanilla/Tideborne-owned/optional compatibility hooks, classify fragility, and document the genuinely version-sensitive remainder in `docs/TIDE_MIXIN_INVENTORY.md`.
- [x] Replace or consolidate avoidable Tide mixins using stable Tideborne-owned/shared integration paths where behavior can be preserved. Journal specimen sync now uses a Tideborne-owned payload, Team Journal catch bookkeeping is centralized in `TeamJournalCatchBridge`, and the remaining Tide hooks are explicitly version-sensitive adapters.
- [x] Remove high-confidence dead legacy implementation while preserving required migration reads, remove the obsolete FishScore self-mixin and inactive Satchel mixin source, and document ownership of the historical package split without a broad rewrite. See `docs/STAGE_4_LEGACY_PACKAGE_CLEANUP.md`.

The architecture sequence is now complete. Future cleanup should follow the ownership boundaries established by the internal API, canonical presentation layer, mixin inventory, shared integration services, and Stage 4 package-ownership rules rather than starting a namespace-wide rewrite.

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

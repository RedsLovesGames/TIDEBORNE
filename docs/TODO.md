# Tideborne post-2.0 backlog

Updated: 2026-09-04

Fishing System 2.0 implementation, recovery tooling, integration polish, canonical gear identity, the dedicated Tideborne creative tab, Stage 64 record/balance work, release-versioning correction, canonical internal fishing API, canonical specimen presentation migration, Tide-targeting mixin inventory, and the focused Tide mixin reduction pass are complete on `dev`.

The authoritative current state is documented in `docs/CURRENT_STATE.md`.

## Fishing System 2.0

- [x] Complete canonical species, specimen, traits, Momentum, score, persistence, migration, gear, compatibility, Journal, records, and networking paths.
- [x] Guard duplicate legacy runtime calculations so canonical V2 state remains authoritative.
- [x] Validate server authority, multiplayer synchronization, optional-mod matrices, and dedicated-server behavior.
- [x] Complete Stages 59 through 64 and the later Fishing Journal and Team Top 15 presentation fixes.
- [x] Freeze the exact public 2.0.0 build identity in `docs/CURRENT_STATE.md`.

## Post-2.0 architecture goals

- [x] Create a small canonical Tideborne fishing API layer for specimen reads, stored FishScore reads, gear modifier queries, species profiles, and record reads. Boundary documented in `docs/TIDEBORNE_INTERNAL_API.md`.
- [x] Create one canonical specimen presentation layer for trait names/order, colors, length/percentile formatting, FishScore formatting, and rarity stars, then migrate covered UI consumers without rebuilding trait state per screen.
- [x] Inventory all active Tide-targeting mixins, separate Tide coupling from vanilla/Tideborne-owned/optional compatibility hooks, classify fragility, and document the genuinely version-sensitive remainder in `docs/TIDE_MIXIN_INVENTORY.md`.
- [x] Replace or consolidate avoidable Tide mixins using stable Tideborne-owned/shared integration paths where behavior can be preserved. Journal specimen sync now uses a Tideborne-owned payload, Team Journal catch bookkeeping is centralized in `TeamJournalCatchBridge`, and the remaining Tide hooks are explicitly version-sensitive adapters.
- [ ] Remove truly dead legacy calculations while preserving required migration reads, then review Tideborne-owned self-mixins and the historical `tideborne`, `tideboundcompatibility`, `tideteamjournal`, and `tidetraits` package split without a broad rewrite.

Implementation order is intentional: canonical API first, presentation second, mixin inventory third, mixin reduction fourth, legacy/package cleanup last. The remaining cleanup should use the established API, presentation, and integration boundaries rather than creating replacements for them.

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

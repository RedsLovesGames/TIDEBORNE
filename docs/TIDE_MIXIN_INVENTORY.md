# Tide mixin inventory and reduction status

Updated: 2026-09-04

This document records the active mixin architecture after the Stage 2 inventory, Stage 3 Tide reduction, and Stage 4 internal cleanup. Version-sensitive Tide lifecycle hooks remain thin adapters. Tideborne-owned self-mixins are removed only when the owning class can preserve the behavior more directly and safely.

## Corrected baseline classification

The original Stage 2 inventory incorrectly counted `LegacyFishScoreCalculatorMixin` as a Tide target. It targeted Tideborne-owned `TraitAxesRuntime`.

The corrected pre-Stage-3 baseline was:

- 41 active configured mixins total;
- 20 Tide targets;
- 11 vanilla Minecraft targets;
- 9 Tideborne-owned targets;
- 1 optional Apex Waters target.

## Stage 3 result

Stage 3 removed one actual Tide dependency and consolidated Team Journal catch accounting:

1. `tideteamjournal.mixin.client.SyncPlayerDataMsgMixin` was removed completely.
   - Canonical journal specimen display data no longer piggybacks Tide's `SyncPlayerDataMsg.handle` lifecycle.
   - Tideborne-owned `RecordHoldersPayload` now updates both `ClientRecordHolders` and `ClientJournalSpecimens`.

2. Team Journal catch-accounting state moved into `TeamJournalCatchBridge`.
   - `tideteamjournal.mixin.TidePlayerDataMixin` and `tideteamjournal.mixin.TideUtilsMixin` remain as Tide lifecycle adapters.
   - Crate canonicalization, before/after snapshots, record capture, post-save capture, and catch-context cleanup are owned by the shared bridge.

After Stage 3 the active configuration contained 40 mixins:

- 19 Tide targets;
- 11 vanilla Minecraft targets;
- 9 Tideborne-owned targets;
- 1 optional Apex Waters target.

## Stage 4 result

Stage 4 removes one high-confidence Tideborne-owned self-mixin and one inactive source file:

1. `LegacyFishScoreCalculatorMixin` was removed from `tide_traits.mixins.json` and deleted.
   - `TraitAxesRuntime.score(...)` and `scoreFromParts(...)` remain compatibility signatures but now return `-1.0` directly.
   - The reconstructed pre-V2 FishScore formula is no longer present behind a self-mixin.
   - Production scoring remains exclusively owned by `FishScoreV2Service`.
   - Legacy stored-score migration reads and compatibility mirrors remain intact.

2. `FishSatchelConversionMixin.java` was deleted.
   - It was already absent from every active mixin config, so it was dead source rather than an active runtime boundary.
   - Active Satchel conversion and storage paths are unchanged.

The active configuration after Stage 4 contains 39 mixins:

- 19 Tide targets;
- 11 vanilla Minecraft targets;
- 8 Tideborne-owned targets;
- 1 optional Apex Waters target.

Stage 4 package ownership and migration-preservation rules are documented in `docs/STAGE_4_LEGACY_PACKAGE_CLEANUP.md`.

## Active Tide-targeting mixins

| Area | Mixin | Coupling | Disposition |
| --- | --- | --- | --- |
| Specimen authoring | `tidetraits.ApplyFishEntityLengthFunctionMixin` | High | Retained. Tide's loot/entity hook remains required for nonstandard specimen-authoring paths. |
| Fish Display persistence | `tidetraits.FishDisplayPersistenceMixin` | High | Retained thin. It remains a Tide save/load compatibility boundary. |
| Catch delivery / Satchel | `tidetraits.TideFishingHookMixin` | High | Retained. Exact catch replacement and retrieval timing preserves canonical assignment and post-log auto-stow. |
| Personal catch accounting | `tidetraits.TidePlayerDataMixin` | Medium | Retained. Tide `logCatch` remains the required finalization/discovery boundary. |
| Fish Display rendering | `tidetraits.client.FishDisplayBlockEntityMixin` | Medium | Retained thin. It applies stored preview state only. |
| Tide fish rendering | `tidetraits.client.TideFishRendererMixin` | Medium | Retained thin. Tide's renderer texture return remains the scoped visual integration point. |
| Fish Profile badges | `tidetraits.client.TideFishProfileMixin` | Medium | Retained. Profile component insertion still depends on Tide's Fish Profile lifecycle. |
| Fish Profile canonical overlay | `tidetraits.client.FishProfileSizeRangeMixin` | High | Retained. It depends on Tide profile fields and journal render lifecycle/layout. |
| Team Journal ownership | `tideteamjournal.TidePlayerDataMixin` | High | Retained thin. Team journal takeover still depends on `getOrCreate`, `syncTo`, and `logCatch`. |
| Team Journal saved-catch boundary | `tideteamjournal.TideUtilsMixin` | Medium | Retained thin. `tryLogCatch` success remains the exact post-save effects boundary. |
| Bobber luck/lure | `tideteamjournal.TideFishingHookMixin` | High | Retained. No behavior-equivalent stable replacement has been proven for Tide constructor-owned luck/lure state. |
| Team Fish Profile stats | `tideteamjournal.client.FishProfileMixin` | Medium | Retained. Tide's component build remains the scoped Team Stats replacement point. |
| Team Records button | `tideteamjournal.client.FishingJournalMixin` | Medium | Retained. Current placement/config/parent-screen behavior remains Tide screen lifecycle dependent. |
| Angling Table server UI | `tideboundcompatibility.AnglingTableLeaderMixin` | High | Retained. Tide exposes no proven equivalent slot-extension path. |
| Crate weight | `tideboundcompatibility.CrateDataMixin` | Low | Retained thin. Narrow return-value modifier. |
| Fishing minigame | `tideboundcompatibility.FishCatchMinigameMixin` | High | Retained. Constructor arguments and `onFinish` timing are exact Tide lifecycle dependencies. |
| Canonical species selection | `tideboundcompatibility.FishSelectorMixin` | High | Retained thin. Tide has no proven pluggable within-fish selector that preserves eligibility. |
| Fishing lifecycle / compatibility | `tideboundcompatibility.TideFishingHookMixin` | High | Retained. Perfect Catch, Momentum, Leviathan, scent, catch loss, and cleanup depend on Tide lifecycle ordering. |
| Angling Table client UI | `tideboundcompatibility.AnglingTableScreenLeaderMixin` | Medium | Retained thin. Visual integration remains tied to Tide's Angling Table layout. |

## Genuinely version-sensitive Tide remainder

The explicit Tide upgrade audit list remains:

- `TideFishingHook` constructor, selection, retrieval, replacement, invalidation, and exact internal call sites;
- Team Journal takeover of `TidePlayerData.getOrCreate` and `syncTo`;
- `TideUtils.tryLogCatch` post-save success boundary;
- Fish Catch Minigame constructor/message and `onFinish` interception;
- Fish Profile rendering and component construction;
- Fish Display persistence and preview construction;
- Angling Table menu/screen extension;
- `FishSelector.getResult` canonical within-fish selection interception.

Canonical fishing calculations, persistence formats, presentation rules, and Team Journal bookkeeping must remain outside these mixin bodies.

## Non-Tide mixins

The 11 vanilla Minecraft-targeting mixins cover Satchel recipes, bucket/entity specimen transfer, vanilla rendering, tooltips, record badges, and vanilla screen access. They are not Tide version dependencies.

The remaining 8 Tideborne-owned mixins are internal architecture debt, not Tide compatibility debt. Most are behavior-rich Team Journal projection/index/storage bridges. They should move into owner classes only when direct integration can preserve migration, record, network, and ordering semantics with focused coverage.

`TideTeamJournalServiceMixin` remains an explicit cross-package cleanup candidate. Its fallback behavior crosses the historical `tidetraits` and `tideteamjournal` boundary, so it should be reconciled directly rather than removed as part of a broad namespace rewrite.

The single Apex Waters `GreatWhiteSharkMixin` remains optional-mod compatibility and stays guarded by `OptionalCompatMixinPlugin`.

## Preserved invariants

Stages 3 and 4 preserve:

- server-authoritative specimen generation and identity;
- one natural percentile/base-size sample per catch;
- canonical FishScore V2 authority;
- server-owned Trait Momentum and Perfect Catch finalization;
- Tide native catch accounting semantics;
- Team Journal, record-holder, history, and Team Top Fish synchronization;
- Satchel auto-stow ordering after Tide's native log path;
- Tide minigame behavior and optional compatibility behavior;
- old-world migration reads and compatibility mirrors;
- no new client-authoring path for canonical specimen state.

Further cleanup should follow `docs/STAGE_4_LEGACY_PACKAGE_CLEANUP.md` rather than deleting historical readers or moving package trees solely for cosmetic consistency.

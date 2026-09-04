# Tide mixin inventory and reduction status

Updated: 2026-09-04

This document records the active mixin architecture after the Stage 2 inventory and Stage 3 reduction pass. Stage 3 intentionally removes or consolidates only hooks for which Tideborne can preserve the existing behavior through a more stable owned boundary. Version-sensitive Tide lifecycle hooks remain thin adapters rather than being removed merely to lower the mixin count.

## Corrected baseline classification

The Stage 2 inventory originally counted `LegacyFishScoreCalculatorMixin` as a Tide target. That classification was incorrect. The mixin targets Tideborne-owned `TraitAxesRuntime`, so the corrected pre-Stage-3 baseline was:

- 41 active configured mixins total;
- 20 Tide targets;
- 11 vanilla Minecraft targets;
- 9 Tideborne-owned targets;
- 1 optional Apex Waters target.

`FishSatchelConversionMixin.java` still exists in source but is not registered in an active mixin config. It remains deferred to the later dead-code cleanup stage.

## Stage 3 result

Stage 3 makes one full Tide-mixin removal and one major consolidation:

1. `tideteamjournal.mixin.client.SyncPlayerDataMsgMixin` was removed completely.
   - Canonical journal specimen display data no longer piggybacks Tide's `SyncPlayerDataMsg.handle` lifecycle.
   - `RecordHoldersPayload`, which is owned by Tideborne, now updates both `ClientRecordHolders` and `ClientJournalSpecimens`.
   - The mixin was removed from `tide_team_journal.mixins.json` and its source file was deleted.

2. Team Journal catch-accounting state was consolidated into `TeamJournalCatchBridge`.
   - `tideteamjournal.mixin.TidePlayerDataMixin` now delegates direct `logCatch` begin/end work to the bridge.
   - `tideteamjournal.mixin.TideUtilsMixin` now delegates `tryLogCatch` begin/end work to the same bridge.
   - Crate canonicalization, before/after snapshots, record capture, post-save capture, and `TeamProgressStore` catch-context cleanup are no longer independently implemented inside the Tide mixins.
   - The Tide hooks remain because their lifecycle boundaries are still required, but they are now thin adapters.

The active configuration after Stage 3 contains 40 mixins:

- 19 Tide targets;
- 11 vanilla Minecraft targets;
- 9 Tideborne-owned targets;
- 1 optional Apex Waters target.

## Active Tide-targeting mixins after Stage 3

| Area | Mixin | Coupling | Stage 3 result |
| --- | --- | --- | --- |
| Specimen authoring | `tidetraits.ApplyFishEntityLengthFunctionMixin` | High | Retained. Removing Tide's loot/entity hook is not safe until every nonstandard specimen-authoring path is proven to remain one-sample and deterministic. |
| Fish Display persistence | `tidetraits.FishDisplayPersistenceMixin` | High | Retained thin. It shadows Tide display inventory and remains a save/load compatibility boundary. |
| Catch delivery / Satchel | `tidetraits.TideFishingHookMixin` | High | Retained. Exact `selectCatch`, replacement, and `retrieve` call sites are required to preserve canonical assignment and post-log auto-stow without duplication or loss. |
| Personal catch accounting | `tidetraits.TidePlayerDataMixin` | Medium | Retained. Tide's `logCatch` remains the required finalization/discovery boundary. |
| Fish Display rendering | `tidetraits.client.FishDisplayBlockEntityMixin` | Medium | Retained thin. It applies stored preview state only and does not author canonical state. |
| Tide fish rendering | `tidetraits.client.TideFishRendererMixin` | Medium | Retained thin. Tide's renderer texture return remains the scoped visual integration point. |
| Fish Profile badges | `tidetraits.client.TideFishProfileMixin` | Medium | Retained. Profile component insertion still depends on Tide's Fish Profile lifecycle. |
| Fish Profile canonical overlay | `tidetraits.client.FishProfileSizeRangeMixin` | High | Retained. It shadows Tide profile fields and depends on Tide's journal render lifecycle/layout. |
| Team Journal ownership | `tideteamjournal.TidePlayerDataMixin` | High | Retained thin. `getOrCreate`, `syncTo`, and `logCatch` are still required boundaries, while catch bookkeeping is delegated to `TeamJournalCatchBridge`. |
| Team Journal saved-catch boundary | `tideteamjournal.TideUtilsMixin` | Medium | Retained thin. `tryLogCatch` success remains the point where post-save history/Top Fish effects can be applied exactly once. |
| Bobber luck/lure | `tideteamjournal.TideFishingHookMixin` | High | Retained. The current hook modifies Tide constructor-owned luck/lure state at the required timing; no behavior-equivalent stable replacement was proven in this pass. |
| Team Fish Profile stats | `tideteamjournal.client.FishProfileMixin` | Medium | Retained. Tide's component build remains the scoped replacement point for Team Stats. |
| Team Records button | `tideteamjournal.client.FishingJournalMixin` | Medium | Retained. No replacement was accepted without proving identical widget placement, config gating, parent-screen behavior, and no duplicate registration. |
| Angling Table server UI | `tideboundcompatibility.AnglingTableLeaderMixin` | High | Retained. Tide exposes no proven equivalent slot-extension path in the current integration. |
| Crate weight | `tideboundcompatibility.CrateDataMixin` | Low | Retained thin. It is already a narrow return-value modifier. |
| Fishing minigame | `tideboundcompatibility.FishCatchMinigameMixin` | High | Retained. Constructor message arguments and `onFinish` delay behavior are exact Tide lifecycle dependencies. |
| Canonical species selection | `tideboundcompatibility.FishSelectorMixin` | High | Retained thin. Tide has no proven pluggable within-fish selector boundary that preserves its eligibility rules. |
| Fishing lifecycle / compatibility | `tideboundcompatibility.TideFishingHookMixin` | High | Retained. Perfect Catch, Momentum, Leviathan selection, scent, catch loss, and cleanup all depend on Tide hook lifecycle ordering. |
| Angling Table client UI | `tideboundcompatibility.AnglingTableScreenLeaderMixin` | Medium | Retained thin. The visual slot remains tied to Tide's Angling Table screen layout. |

## Genuinely version-sensitive remainder

The remaining Tide mixins are not treated as ordinary application logic. The following are explicit version-sensitive compatibility boundaries:

- `TideFishingHook` constructor, `selectCatch`, `retrieve`, replacement, invalidation, and exact internal call sites;
- Team Journal takeover of `TidePlayerData.getOrCreate` and `syncTo`;
- `TideUtils.tryLogCatch` post-save success boundary;
- Fish Catch Minigame constructor/message and `onFinish` interception;
- Fish Profile rendering and component construction;
- Fish Display persistence and preview construction;
- Angling Table menu/screen extension;
- `FishSelector.getResult` canonical within-fish selection interception.

When Tide changes versions, these boundaries are the focused compatibility audit list. Canonical fishing calculations, persistence formats, presentation rules, and Team Journal catch bookkeeping should remain outside the mixin bodies.

## Non-Tide mixins

The 11 vanilla Minecraft-targeting mixins remain outside Tide version-coupling counts. They cover Satchel recipes, bucket/entity specimen transfer, vanilla item/entity rendering, tooltips, record badges, and vanilla screen access.

The 9 Tideborne-owned mixins are internal architecture debt rather than Tide compatibility debt. This set includes `LegacyFishScoreCalculatorMixin`, which targets `TraitAxesRuntime`, plus Team Journal projection/index/storage self-mixins. Their removal belongs with the following legacy/package cleanup stage, where Tideborne controls both sides and can replace self-mixins with direct calls without conflating that work with Tide compatibility.

The single Apex Waters `GreatWhiteSharkMixin` remains optional-mod compatibility and stays guarded by `OptionalCompatMixinPlugin`.

## Stage 3 invariants

The reduction pass preserves these requirements:

- server-authoritative specimen generation and identity;
- one natural percentile/base-size sample per catch;
- canonical FishScore V2 authority;
- server-owned Trait Momentum and Perfect Catch finalization;
- Tide native catch accounting semantics;
- Team Journal, record-holder, history, and Team Top Fish synchronization;
- Satchel auto-stow ordering after Tide's native log path;
- Tide minigame behavior and optional compatibility behavior;
- no new client-authoring path for canonical specimen state.

Stage 4 may now remove dead legacy calculations and Tideborne-owned self-mixins, while preserving required migration reads and avoiding a broad package rewrite.

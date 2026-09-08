# Tide mixin inventory and reduction status

Updated: 2026-09-08

Version-sensitive Tide lifecycle hooks remain thin adapters. The active configuration contains 31 mixins: 19 Tide targets, 11 vanilla targets, no Tideborne-owned targets and 1 optional Apex target. `TeamStatsComponent` directly owns Journal stats rendering and height; it requires no Tideborne-owned self-mixin. Tide's Fish Profile component-insertion hook remains necessary.

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

The historical configuration after Stage 4 contained 39 mixins:

- 19 Tide targets;
- 11 vanilla Minecraft targets;
- 8 Tideborne-owned targets;
- 1 optional Apex Waters target.

Stage 4 package ownership and migration-preservation rules are documented in `docs/ARCHITECTURE.md`.

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

## Post-2.0 gear ownership review

The gear rework retains these targets; no behavior-equivalent stable API replacement is proven:

| Gear-affected adapter | Classification and current owner |
| --- | --- |
| Team Journal TideFishingHook constructor | Version-sensitive thin adapter: captures one server BobberGearModifiers snapshot and exposes native luck/lure; synchronized client values are display-only. |
| FishSelectorMixin | Version-sensitive thin adapter to server TideSpeciesSelectionBridge/SpeciesSelectionService; native eligibility precedes capped weighting. |
| FishCatchMinigameMixin | Version-sensitive constructor/message and finish timing; FishingModifiers delegates projection to FightProfileService. |
| Compatibility TideFishingHookMixin | Version-sensitive lifecycle adapter for fish-only selection, catch finalization, Momentum and cleanup; Leviathan numbers remain canonical. |
| CrateDataMixin | Thin return-value adapter to FishingModifiers/FishingGearEffects; native bait crate aggregation remains native. |
| AnglingTableLeaderMixin / AnglingTableScreenLeaderMixin | Version-sensitive slot/UI adapters; persisted tier translation remains LeaderAttachment. Client layout does not author modifiers. |
| Traits TideFishingHookMixin / TidePlayerDataMixin | Version-sensitive delivery/finalization boundaries; canonical storage and Satchel services retain specimen authority. |

The optional Apex loss hook remains guarded by OptionalCompatMixinPlugin and consumes canonical protection. No new client-class reference or mixin target was introduced in the final pass. Unused bobber environment-based lookup methods were removed; config/payload migration and native fallback adapters remain.

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

B02 removed the final four Tideborne-owned self-mixins. TeamProgressStore directly owns canonical score/Top Fish behavior and contributor/history projection; TeamCanonicalJournalCapture retains the catch snapshot. Direct-owner tests and core GameTests cover migration, nested cleanup, canonical ordering and replay. No Tide target or injection timing changed; personal-record decisions now delegate to RecordHolderStore at the existing logCatch callback.

TeamJournalService directly tracks successful team loads and leaves native fallback data unmarked.

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

Further cleanup should follow `docs/ARCHITECTURE.md` rather than deleting historical readers or moving package trees solely for cosmetic consistency.

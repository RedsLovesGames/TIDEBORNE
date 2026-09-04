# Tide mixin inventory

Updated: 2026-09-04

This document is the Stage 2 inventory for Tideborne's post-2.0 mixin architecture pass. It records the mixins that are actually configured on `dev`, separates Tide targets from Minecraft and Tideborne-owned targets, classifies Tide coupling, and defines the replacement strategy for Stage 3.

Snapshot baseline before this document: `35cd23ff64f4d42a14fba646fbbf93e55cb137f8` on `dev`.

No runtime behavior is changed by this inventory stage.

## Active configuration summary

The five active mixin configs register 41 mixins total:

- 21 target Tide classes directly;
- 11 target vanilla Minecraft classes;
- 8 target Tideborne-owned classes;
- 1 targets Apex Waters and is guarded by the optional compatibility plugin.

The raw count of 41 therefore overstates Tide version coupling. Only the 21 Tide-targeting entries belong in the Tide dependency inventory.

`FishSatchelConversionMixin.java` still exists in source, but it is not present in the active `tide_traits.mixins.json` config and is not counted as an active mixin. Treat that as legacy/dead-code review material for the later cleanup stage rather than silently restoring it here.

### Coupling scale

- **High**: cancels or redirects Tide behavior, shadows mutable/private Tide state, depends on an exact constructor/call site, or owns a critical compatibility boundary.
- **Medium**: injects into a named Tide method or screen layout but does not replace the core method wholesale.
- **Low**: small return-value augmentation against a narrow public-shaped Tide surface.

### Stage 3 disposition labels

- **Retain thin**: a Tide method remains the only reliable trigger, but business logic should live behind a Tideborne-owned service or API rather than in the mixin.
- **Consolidate**: overlapping Tide hooks should share one integration service and, where safe, fewer mixin entry points.
- **Replace candidate**: a stable Tideborne/Fabric-owned path appears capable of replacing the mixin, but removal still requires focused validation.
- **Compatibility shim**: retain only while external or historical callers still require the Tide surface being intercepted.

## Tide-targeting mixins

| Config | Mixin | Tide target and hook | Coupling | Stage 3 disposition | Required invariant |
| --- | --- | --- | --- | --- | --- |
| `tide_traits.mixins.json` | `ApplyFishEntityLengthFunctionMixin` | `ApplyFishEntityLengthFunction.apply`, HEAD cancellation | High | Replace candidate | Canonical specimen generation must still occur exactly once for all Tide loot/entity paths without rerolling percentile or size. |
| `tide_traits.mixins.json` | `FishDisplayPersistenceMixin` | `FishDisplayBlockEntity.writeNbt/readNbt`, with shadowed inventory | High | Retain thin | Display specimen payload must survive save/load and must not create a second specimen representation. |
| `tide_traits.mixins.json` | `LegacyFishScoreCalculatorMixin` | `FishScoreCalculator.calculateFishScore`, HEAD cancellation | Medium | Compatibility shim / removal candidate | Any caller that still reaches Tide's calculator must receive canonical V2 FishScore, never the reconstructed pre-V2 formula. |
| `tide_traits.mixins.json` | `TideFishingHookMixin` | `TideFishingHook.selectCatch`, `replacePrimaryCatch`, and exact calls inside `retrieve` | High | Retain thin and consolidate | Selected catches remain canonical, auto-stow occurs only after Tide's native log path, no catch is duplicated/lost, and server authority is preserved. |
| `tide_traits.mixins.json` | `TidePlayerDataMixin` | `TidePlayerData.logCatch`, HEAD and TAIL | Medium | Consolidate | Specimen finalization precedes Tide accounting, personal discovery/momentum/record effects remain once-per-catch, and Tide progression stays intact. |
| `tide_traits.client.mixins.json` | `FishDisplayBlockEntityMixin` | `FishDisplayBlockEntity.setRenderedEntity` | Medium | Retain thin | Rendered display entities receive only the stored display specimen preview and never author canonical state. |
| `tide_traits.client.mixins.json` | `TideFishRendererMixin` | Tide `FishRenderer.getTextureLocation` return | Medium | Retain thin | Canonical pigmentation/mutation visuals remain deterministic and do not alter specimen data. |
| `tide_traits.client.mixins.json` | `TideFishProfileMixin` | `FishProfile.buildComponentsWithStats` return | Medium | Consolidate | Discovery badges remain on the Tide profile without rebuilding canonical specimen state. |
| `tide_traits.client.mixins.json` | `FishProfileSizeRangeMixin` | exact `FishProfile.render(DrawContext,IIF)` HEAD/TAIL plus shadowed `data` and `font` | High | Retain thin and consolidate | Canonical record/specimen presentation remains correct and Journal render context is always balanced. |
| `tide_team_journal.mixins.json` | `TidePlayerDataMixin` | `TidePlayerData.getOrCreate` HEAD cancel, `syncTo` HEAD conditional cancel, `logCatch` HEAD/TAIL | High | Retain thin and consolidate | Team journal state remains authoritative and synchronized while Tide's catch accounting semantics are preserved. |
| `tide_team_journal.mixins.json` | `TideUtilsMixin` | `TideUtils.tryLogCatch` HEAD/RETURN | Medium | Consolidate / replace candidate | Team before/after snapshots occur around the successful native save exactly once, including nonstandard Tide award paths. |
| `tide_team_journal.mixins.json` | `TideFishingHookMixin` | exact `TideFishingHook` constructor plus mutable shadows of final `luck` and `lureSpeed` | High | Replace candidate | Bobber luck and lure bonuses remain server-side and compose exactly once with the canonical gear modifier model. |
| `tide_team_journal.mixins.json` | `client.FishProfileMixin` | `FishProfile.buildComponentsWithStats` return | Medium | Consolidate | Team record-holder stats replace Tide's stock stats component without changing team data ownership. |
| `tide_team_journal.mixins.json` | `client.FishingJournalMixin` | `FishingJournal.init` and `render` | Medium | Replace candidate | Team Records remains reachable from the Tide journal with unchanged visibility/config behavior and no duplicate widget registration. |
| `tide_team_journal.mixins.json` | `client.SyncPlayerDataMsgMixin` | `SyncPlayerDataMsg.handle` HEAD | Medium | Replace candidate | Canonical display specimens and record-holder metadata reach the client from server-owned state before UI reads them. |
| `tidebound_compatibility.mixins.json` | `AnglingTableLeaderMixin` | string target `AnglingTableMenu`, `getForgingSlotsManager` cancel plus `updateResult` HEAD/TAIL | High | Retain thin unless a supported Tide menu extension is found | Leader slot semantics and output preservation remain identical and no invalid recipe path is introduced. |
| `tidebound_compatibility.mixins.json` | `CrateDataMixin` | `CrateData.weight` return | Low | Retain thin | Kujira/gear crate weighting composes with Tide eligibility and never bypasses canonical catch selection. |
| `tidebound_compatibility.mixins.json` | `FishCatchMinigameMixin` | `FishCatchMinigame` constructor message args, constructor return, and `onFinish` HashMap call redirect | High | Retain thin | Line/bait minigame modifiers and intentional post-catch delay removal remain server-authoritative and apply exactly once. |
| `tidebound_compatibility.mixins.json` | `FishSelectorMixin` | `FishSelector.getResult(FishingContext)`, HEAD cancellation | High | Retain thin | Only Tide's within-fish species roll is replaced; Tide eligibility restrictions still apply and canonical Fishing Luck remains authoritative. |
| `tidebound_compatibility.mixins.json` | `TideFishingHookMixin` | `selectCatch` redirects, `invalidateCatch`, both retrieve paths, `tick`, catch-loss integration | High | Retain thin and split logic into services | Canonical catch state, Perfect Catch finalization, Momentum, Leviathan behavior, scent, catch loss, and cleanup stay server-owned and cannot double-run. |
| `tidebound_compatibility.mixins.json` | `AnglingTableScreenLeaderMixin` | string target `AnglingTableScreen.drawInvalidRecipeArrow` | Medium | Replace candidate / retain thin | The leader slot remains visually aligned with the Tide Angling Table without changing menu state. |

## Highest-priority Stage 3 replacement candidates

The inventory does not authorize removing these immediately. It defines the order in which to prove a replacement.

1. **`client.SyncPlayerDataMsgMixin`**: strongest candidate for full removal. Move Tideborne display-specimen and record-holder metadata onto a Tideborne-owned S2C payload rather than piggybacking the Tide player-data message. This removes a Tide networking interception and makes the wire contract explicitly Tideborne-owned.
2. **`LegacyFishScoreCalculatorMixin`**: search production callers and external compatibility assumptions. If no supported integration still requires Tide's static calculator to expose V2 score, remove the shim. If external callers require it, keep it isolated and document it as compatibility-only.
3. **`tideteamjournal.mixin.TideFishingHookMixin`**: move bobber luck/lure behavior into the canonical fishing-gear modifier query path if the same timing and values can be preserved. This would remove an exact constructor descriptor and mutable shadow of Tide final fields.
4. **`client.FishingJournalMixin`**: evaluate Fabric screen lifecycle APIs for adding the Team Records widget. Remove the Tide screen mixin only if the existing button placement, config gating, and parent-screen behavior can be reproduced without private Tide access.
5. **Fish Profile hooks**: consolidate `tidetraits` profile badges, canonical size/score overlay, and Team Journal stats replacement behind one Tideborne client integration service. A thin FishProfile mixin may still be necessary, but three independent UI hooks should not each reconstruct Tide integration rules.
6. **Catch accounting hooks**: consolidate the two `TidePlayerDataMixin` classes plus `TideUtilsMixin` around one Tideborne catch-accounting bridge. Do not remove a trigger until crate awards, direct Tide catches, save/sync, records, Momentum, and team progression prove equivalent.
7. **`ApplyFishEntityLengthFunctionMixin`**: determine whether all live specimen-authoring paths can be covered by the canonical catch/specimen service without intercepting Tide's loot function. Removal is safe only if entity, bucket, display, migration, and nonstandard loot paths remain single-sample and deterministic.

## Genuinely version-sensitive remainder

Even after avoidable hooks are reduced, the following boundaries are expected to remain version-sensitive unless Tide exposes new extension APIs:

- Tide fishing-hook lifecycle integration, especially exact `retrieve`, `selectCatch`, and constructor/call-site hooks;
- Team Journal takeover of `TidePlayerData.getOrCreate` and `syncTo`;
- Fish Catch Minigame constructor/message and `onFinish` call-site interception;
- Fish Profile rendering that shadows Tide fields or depends on Tide's fixed 400x260 journal layout;
- Fish Display persistence that shadows Tide's display inventory;
- Tide Angling Table menu/screen extension if no supported slot/screen extension surface exists;
- canonical `FishSelector.getResult` interception while Tide provides no pluggable within-fish selector.

These mixins should be kept as small adapters. They should call Tideborne-owned services and avoid embedding canonical fishing calculations, persistence formats, or presentation logic directly in the mixin body.

## Non-Tide active mixins

These are active mixins but are not Tide version dependencies. Do not count them when measuring Tide coupling.

### Vanilla Minecraft targets: 11

| Mixin | Target | Purpose |
| --- | --- | --- |
| `tidetraits.AnglersSatchelRecipeMixin` | `ShapedRecipe` | Validate upgraded Satchel recipe and preserve contents. |
| `tidetraits.BucketableMixin` | `FishEntity` | Copy specimen state into bucket stacks. |
| `tidetraits.MobBucketItemMixin` | `EntityBucketItem` | Restore specimen state when bucket entities spawn. |
| `tidetraits.MobSpecimenMixin` | `MobEntity` | Track and persist specimen entity state. |
| `tidetraits.TideItemBucketMixin` | `Item` | Preserve specimen data across Tide bucket conversion click flows. |
| `tidetraits.client.ItemRendererMutationTintMixin` | `ItemRenderer` | Render canonical mutation overlays on item models. |
| `tidetraits.client.ItemStackMutationTooltipMixin` | `ItemStack` | Append canonical specimen tooltip presentation. |
| `tidetraits.client.LivingEntityRendererMixin` | `LivingEntityRenderer` | Apply foreign-fish texture and physical length rendering. |
| `tidetraits.client.MinecraftMutationRenderingMixin` | `MinecraftClient` | Initialize client mutation rendering. |
| `tideteamjournal.client.GuiGraphicsMixin` | `DrawContext` | Draw record badges on item decorations. |
| `tideteamjournal.client.ScreenAccessor` | `Screen` | Invoke vanilla child/widget registration methods. |

### Tideborne-owned targets: 8

| Mixin | Target | Note |
| --- | --- | --- |
| `tidetraits.TideTeamJournalServiceMixin` | `TeamJournalService` | Cross-package Personal Journal compatibility bridge. |
| `tidetraits.client.TeamStatsPercentileMixin` | `TeamStatsComponent` | Canonical Team Stats rendering. |
| `TeamProgressCanonicalJournalMixin` | `TeamProgressStore` | Canonical score/journal migration and capture. |
| `TopFishCanonicalIndexMixin` | `TeamProgressStore` | Canonical deterministic Team Top 15 indexing. |
| `ContributorCanonicalScorePayloadMixin` | `TeamProgressStore.Contributor` | Canonical contributor score payload. |
| `RecordEventCanonicalScorePayloadMixin` | `TeamProgressStore.RecordEvent` | Canonical history-event score payload. |
| `RecordHolderCanonicalJournalMixin` | `RecordHolderStore` | Canonical record specimen sidecars. |
| `RecordHolderNetworkProjectionMixin` | `RecordHolderStore` | Client display projection attachment. |

These are internal architecture debt rather than Tide compatibility debt. Because Tideborne owns both sides, later package cleanup should generally prefer direct calls/interfaces over self-mixins where doing so does not disrupt the reconstructed compatibility boundary.

### Optional external target: 1

`GreatWhiteSharkMixin` targets Apex Waters `GreatWhiteSharkEntity` and is guarded by `OptionalCompatMixinPlugin`. It is optional-mod compatibility debt, not Tide coupling. Keep it isolated and validate both Apex-present and Apex-absent classloading during compatibility/release passes.

## Stage 3 execution rules

- Preserve server authority, canonical specimen identity, one-sample specimen generation, FishScore V2, Momentum, Tide minigame behavior, Satchel behavior, Journal/team records, and optional-mod classloading.
- Prefer `TideborneFishingApi`, canonical fishing services, and Tideborne-owned network payloads for logic/data access.
- When a Tide mixin is still required as an event trigger, make it a thin adapter into a shared integration service rather than a second implementation of fishing behavior.
- Do not replace a Tide mixin with a vanilla mixin merely to reduce the Tide-target count. A replacement is only better if the target/API is actually more stable and the behavior remains scoped.
- Do not remove migration reads or compatibility projection solely because the current V2 path no longer writes the old field.
- Validate each meaningful replacement before proceeding to the next high-risk group.
- Keep package/dead-code cleanup as the following architecture stage. Do not combine broad historical package movement with Tide mixin replacement.

# Stage 4 legacy and package cleanup

Updated: 2026-09-04

## Scope

Stage 4 removes high-confidence dead legacy implementation while preserving migration and compatibility reads, then defines ownership for the historical `tideborne`, `tideboundcompatibility`, `tideteamjournal`, and `tidetraits` package roots.

This is intentionally not a repository-wide namespace rewrite. Package movement is only useful when it removes a real ownership ambiguity without changing persisted identifiers, loader entry points, mixin configuration, networking, or compatibility behavior.

## Cleanup completed

### Legacy FishScore implementation

Fishing System 2.0 production scoring remains owned exclusively by `FishScoreV2Service`.

The historical `TraitAxesRuntime.score(...)` and `TraitAxesRuntime.scoreFromParts(...)` signatures remain only as disabled compatibility surfaces. They now return `-1.0` directly instead of containing the reconstructed pre-V2 formula behind a Tideborne-owned self-mixin.

As a result:

- `LegacyFishScoreCalculatorMixin` was removed from `tide_traits.mixins.json`;
- `LegacyFishScoreCalculatorMixin.java` was deleted;
- no production FishScore path was redirected or recalculated;
- legacy stored-score migration keys and reads remain untouched.

The compatibility signatures are intentionally retained rather than deleted because historical or compatibility code can still link against them. They no longer contain a second scoring implementation.

### Inactive Satchel conversion mixin

`FishSatchelConversionMixin.java` was present in source but absent from the active mixin configuration. It was therefore dead source rather than an active compatibility boundary and has been deleted.

Active Satchel conversion, purchase, recipe, storage, UI, and specimen behavior remains in the currently configured/runtime paths.

## Migration surfaces deliberately retained

Stage 4 does not remove an old field merely because current V2 catches no longer author it. Persisted compatibility is a separate contract from production calculation.

The following categories remain intentionally supported where already audited:

- legacy FishScore storage and one-way migration reads;
- legacy mutation/body/condition components used by old stacks, migration, admin editing, or compatibility rendering;
- legacy size recovery helpers used to interpret pre-V2 persisted fish correctly;
- legacy species/fight compatibility inputs still required at Tide integration boundaries;
- existing registry IDs, component IDs, NBT/save keys, network payload IDs, recipe/item IDs, and external fish IDs.

These compatibility reads must not become new V2 authoring paths.

## Package ownership

### `com.redslovesgames.tideborne`

This is the canonical home for Fishing System 2.0 and new Tideborne-owned architecture.

It owns:

- canonical specimen and fishing domain behavior;
- canonical fishing services and server-authoritative runtime state;
- `TideborneFishingApi` and other stable internal read/query boundaries;
- canonical specimen presentation;
- V2 migration orchestration and recovery services;
- new shared architecture that should not depend on a historical subsystem name.

New canonical fishing calculations should be added here rather than to `tidetraits`, `tideteamjournal`, or a compatibility package.

### `com.redslovesgames.tideboundcompatibility`

This remains the external integration boundary for Tide and optional fishing-mod behavior that is genuinely compatibility-specific.

It currently owns Tide fishing lifecycle adapters, Fish Catch Minigame integration, Angling Table integration, external-mod compatibility, related gear/content registration, and integration networking/client code.

Code should remain here when its reason for existing is an external API or lifecycle boundary. Canonical fishing rules called by those adapters should live in `tideborne` services instead of being reimplemented here.

### `com.redslovesgames.tideteamjournal`

This remains the Team Journal and FTB Teams subsystem boundary.

It owns team journal persistence, record ownership/history, Team Top Fish projection, Team Records UI/networking, shared-team migration/backfill, and Team Journal integration services.

Behavior-rich self-mixins against Team Journal-owned classes are internal architecture debt, but they are not automatically dead code. They should only be replaced when the owner class can absorb the behavior directly with focused regression coverage.

### `com.redslovesgames.tidetraits`

This remains the historical traits, Satchel, discovery, entity transfer, rendering, and compatibility-preservation boundary.

It owns persisted legacy components, old-world interpretation helpers, Satchel infrastructure, discovery compatibility, entity/item transfer support, and rendering adapters that still depend on historical component contracts.

It is not the home for new Fishing System 2.0 scoring or specimen-authoring logic. New canonical state should flow from `tideborne` and be projected into legacy surfaces only when compatibility requires it.

## Self-mixin disposition

Stage 4 removes the high-confidence `LegacyFishScoreCalculatorMixin` self-mixin because the owner class can preserve its compatibility signatures directly with no behavior change.

Other Tideborne-owned self-mixins are retained when they still carry meaningful behavior, including TeamProgress/RecordHolder canonicalization and cross-package Team Journal compatibility. Removing those safely requires moving their behavior into the owning classes, not simply deleting the mixin declarations.

`TideTeamJournalServiceMixin` is specifically retained for a later focused bridge cleanup. Its current fallback integration crosses the historical `tidetraits` and `tideteamjournal` package boundary, and its old target surface should be reconciled directly rather than hidden inside a broad package rewrite.

## Package movement rules

Future cleanup should follow these rules:

1. Do not move a class solely to make package names look uniform.
2. Do not rename persisted identifiers as part of Java package cleanup.
3. Do not move version-sensitive Tide or optional-mod adapters into the canonical domain package.
4. Prefer direct owner-class calls over Tideborne-owned self-mixins when behavior can be moved without changing compatibility timing.
5. Keep migration reads until the supported migration window is intentionally retired and documented.
6. New Fishing System 2.0 domain logic belongs under `tideborne`; historical packages should increasingly act as adapters or subsystem owners rather than alternate canonical implementations.

## Result

Stage 4 reduces actual implementation duplication without creating migration risk. The repository keeps its historical package roots because they still correspond to meaningful subsystem or compatibility boundaries, but their ownership is now explicit and future canonical fishing work has one preferred home.

# Tideborne architecture map

This document describes the 1.3.57 package structure recovered from the authoritative JAR and the direction for cleanup.

## Composition root

### `com.redslovesgames.tideborne`

Responsibilities recovered from 1.3.57:

- main/common entrypoint
- client entrypoint
- unified configuration facade
- migration coordination
- top-level commands
- backend/facade integration
- render-service facade

Representative classes:

- `Tideborne`
- `TideborneBackend`
- `TideborneClient`
- `TideborneModMenu`
- `TideborneUnifiedConfigScreen`
- `TideborneCommands`
- `TideborneConfigBackend`
- `TideborneMigrationManager`

Desired role: composition and orchestration only. Domain algorithms should live in the module that owns them.

## Specimen and Satchel domain

### `com.redslovesgames.tidetraits`

Responsibilities:

- Body Type and Condition traits
- catch-time trait assignment
- physical specimen size
- percentile calculations
- fish descriptor data
- discovery state
- entity/item specimen transfer
- trait rendering
- Angler's Satchel storage, upgrades, XP, protection, sort behavior, networking, and UI
- personal/shared discovery compatibility
- Tide journal integration

Important subpackages:

- `catching`: catch trait application
- `fish`: size/percentile/descriptor calculations
- `discovery`: discovery persistence and sync
- `satchel`: inventory, capacity, XP, upgrades, requests, protection
- `client.gui.satchel`: Satchel screen and client networking
- `client.render`: trait rendering and generated texture cache
- `compat.multiplayer`: personal/shared discovery bridges
- `mixin`: Tide/Minecraft integration points
- `gametest`: behavioral regression tests

High-value pure-domain extraction targets:

- percentile math
- trait probability math
- size transforms
- FishScore inputs shared with team journal
- Satchel capacity/upgrade calculations

## Team journal domain

### `com.redslovesgames.tideteamjournal`

Responsibilities:

- shared team journal merge behavior
- team progress store
- record holders
- record events and event-history badges
- team/shared record state
- bobber bonuses
- scoreboard and Top Fish presentation
- client/server synchronization

Representative classes:

- `TeamJournalService`
- `TeamProgressStore`
- `RecordHolderStore`
- `JournalMerger`
- `FishScoreComparator`
- `RecordScoreboard`
- `TopFishScreen`
- `TeamRecordsScreen`

Refactor direction:

- move record comparison to pure domain logic
- keep persistence adapters separate from record decisions
- keep screens as presentation only
- centralize payload registration and versioning

## Tide equipment and optional compatibility

### `com.redslovesgames.tideboundcompatibility`

Responsibilities:

- Leviathan Bait
- Tide fishing minigame modifiers
- hook/line equipment behavior
- chum
- shark catch-loss mechanics
- Apex Waters integration
- optional compatibility mixin loading
- synchronized server/client settings

Important subpackages:

- `fishing`: core Tide fishing modifiers and Leviathan behavior
- `compat.apex`: shark AI/scent/food integration
- `mixin`: Tide integration points
- `mixin.apex`: Apex-specific integration
- `network`: settings and feedback payloads
- `registry`: items/entities/tags
- `client`: HUD/config/tooltips/guide

Refactor direction:

- separate pure fishing modifiers from hook/entity mixins
- isolate optional Apex classes in a compatibility boundary
- avoid static global state where a world/server-scoped service is appropriate

## Current dependency direction

The recovered JAR contains cross-package calls between these modules. During cleanup, work toward:

```text
tideborne composition root
    -> tidetraits
    -> tideteamjournal
    -> tideboundcompatibility

client screens/mixins/network adapters
    -> application/domain services
    -> pure models/calculations
    -> persistence/external adapters
```

`tidetraits`, `tideteamjournal`, and `tideboundcompatibility` should not become mutually recursive utility collections.

## Known high-risk boundaries

Changes in these areas require extra regression coverage:

- `TideFishingHookMixin` variants
- FishData/FishCatchMinigame mixins
- Tide player-data mixins
- journal UI mixins
- specimen NBT transfer
- Satchel storage NBT
- team progress persistence
- record migration
- optional Apex mixin plugin
- client-generated mutation texture cache

## Planned Fishing System 2.0 boundary

The future fishing-system redesign should eventually introduce explicit domain services for:

- species selection luck
- bite speed
- specimen percentile generation
- trait luck and trait axes
- fight profile transformation
- deterministic FishScore evaluation

Those classes do not belong in this reconstruction branch yet. The current goal is to create clean seams so they can replace legacy logic safely later.

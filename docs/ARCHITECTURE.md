# Tideborne architecture map

Status: current architecture reference for the maintained `dev` branch.

Fishing System 2.0 is implemented. This document describes the current ownership model and the direction for future work. Historical reconstruction details remain in `docs/RECONSTRUCTION.md` and stage-specific documents.

## Core design rule

Tideborne should have one canonical owner for each gameplay decision and one shared read/presentation boundary for downstream consumers.

Do not solve local integration problems by creating parallel specimen, score, gear, record, or presentation models.

## Canonical Fishing System 2.0 domain

### `com.redslovesgames.tideborne.fishing.v2`

This package owns the canonical fishing domain and pure calculations.

Important responsibilities include:

- fishing context and environment inputs
- Tide species adaptation and eligibility
- canonical species weighting and selection
- deterministic specimen generation
- natural percentile and physical size
- Body Type, Condition, Pigmentation, and Specimen Quality
- Trait Luck and Trait Momentum inputs
- canonical FishScore V2
- canonical fight transformation
- immutable composable fishing-gear modifiers
- gear identity and named gear effects
- canonical persistence and migration boundaries
- canonical catch lifecycle authority

Important current boundaries include:

- `FishingContext`
- `SpeciesProfile`
- `SpeciesSelectionService`
- `SpecimenData`
- `SpecimenGenerator`
- `FishScoreV2Service`
- `FightProfileService`
- `FishingGearRegistry`
- `FishingGearModifiers`
- `FishingGearEffects`
- `CanonicalCatchStateManager`

Gear may influence the canonical pipeline through defined inputs, but it must not become a second specimen author.

## Stable internal read/query API

### `com.redslovesgames.tideborne.api.TideborneFishingApi`

This is the preferred internal facade for covered canonical reads and queries.

It exposes existing canonical records rather than creating a second representation.

Covered areas include:

- specimen reads
- stored FishScore reads
- gear modifier queries
- Tide species profile reads
- record comparison and Team Top Fish reads

New feature code should prefer this boundary instead of reaching directly into storage, record indexes, or compatibility internals when the API already covers the need.

The API is not a second calculation authority. FishScore remains owned by `FishScoreV2Service`, gear registration remains owned by `FishingGearRegistry`, and canonical specimen creation remains owned by `SpecimenGenerator`.

## Canonical specimen presentation

### `com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation`

This package owns shared specimen presentation semantics such as:

- trait order
- trait names and colors
- length formatting
- percentile formatting
- FishScore formatting
- rarity stars

Covered UI surfaces should consume this layer rather than reconstructing trait or score presentation independently.

Screens still own layout, clipping, localized labels, timestamps, and interaction behavior.

## Composition and application root

### `com.redslovesgames.tideborne`

Current responsibilities include:

- common and client entrypoints
- backend/facade composition
- unified configuration
- migration coordination
- commands
- shared client integration
- cross-subsystem orchestration

Domain algorithms should stay in the domain that owns them rather than accumulating in the composition root.

## Tide and optional-mod compatibility boundary

### `com.redslovesgames.tideboundcompatibility`

This package remains the external Tide and optional-mod integration boundary.

Responsibilities include:

- Tide fishing lifecycle adapters
- Tide equipment integration
- leader attachment persistence and migration
- Leviathan Bait integration
- Tide minigame projection bridges
- optional Apex Waters and Myths of the Sea compatibility
- compatibility networking and settings where still owned here

Version-sensitive Tide mixins belong here when no stable API/event seam can replace them.

Use `docs/TIDE_MIXIN_INVENTORY.md` before modifying Tide-targeting mixins.

## Team Journal boundary

### `com.redslovesgames.tideteamjournal`

This subsystem owns:

- team progress persistence
- Journal merging and team state
- record holders
- event/history badges
- Team Top Fish and related team record state
- scoreboards and Team Journal UI
- team-oriented networking

Canonical specimen semantics and FishScore should be consumed from Fishing System 2.0 boundaries, not recalculated here.

Journal integration mixins should be thin lifecycle adapters into named services such as the shared catch bridge.

## TideTraits and Satchel boundary

### `com.redslovesgames.tidetraits`

This historical package remains a compatibility and subsystem boundary for:

- Angler's Satchel storage and upgrades
- discovery persistence
- specimen transfer compatibility
- legacy trait/size compatibility reads and guarded writes
- rendering and generated specimen textures
- post-catch Satchel behavior

The Satchel is not a canonical specimen generator. It may store, sort, protect, summarize, and present canonical catches after generation.

Legacy catch individualization paths must remain guarded so they cannot reroll or overwrite a current canonical V2 specimen.

## Dependency direction

Prefer this direction:

```text
entrypoints / screens / networking / mixins / compatibility adapters
                         |
                         v
application and integration services
                         |
                         v
canonical domain services and models
                         |
                         v
persistence and external adapters
```

Avoid mutually recursive utility dependencies between `tideborne`, `tideteamjournal`, `tidetraits`, and `tideboundcompatibility`.

## Mixins

The current active mixin set contains Tide targets, vanilla targets, Tideborne-owned targets, and one optional Apex target. The authoritative classification and fragility notes live in `docs/TIDE_MIXIN_INVENTORY.md`.

Policy:

- mixins should adapt lifecycle or inaccessible state
- gameplay calculations should live in named services
- Tide-targeting mixins are treated as version-sensitive unless a stable seam is documented
- Tideborne-owned self-mixins are internal architecture debt, not Tide coupling, and should be removed only when the owner can absorb the behavior with focused regression coverage

Do not perform a broad mixin purge simply to reduce the count.

## Post-2.0 gear architecture

The gear audit is complete in `docs/GEAR_PROGRESSION_AUDIT.md`.

Current gear behavior spans Tide-native behavior plus Tideborne-owned canonical projections and a few historical adapters. The redesign goal is consolidation, not replacement of the existing canonical layer.

Preserve and extend these seams first:

- `FishingGearRegistry` for identity
- `FishingGearModifiers` for composition
- `FishingGearEffects` for named effects
- `FightProfileService` for fight/minigame transformation
- `TideborneFishingApi` for covered read/query access

Known consolidation targets include:

- bobber bonus ownership split between Team Journal and V2 modifiers
- native and Tideborne line projections
- leader persistence/tier/effect layering
- Leviathan selection and modifier ownership
- remaining individual hard-coded gear checks

A new loadout or resolver abstraction should be introduced only if the existing registry/modifier/effect architecture cannot represent a proven requirement cleanly.

## Persistence and compatibility constraints

Do not casually rename or move persisted identities because Java ownership changes.

Compatibility-sensitive contracts include:

- item and entity IDs
- recipe IDs
- component IDs
- NBT keys
- saved-state names
- config keys
- payload IDs and wire fields
- command names
- external fish IDs

The historical `tidebound_compatibility:steel_leader` item ID remains stable even though its semantic tier is now Iron Leader.

## Historical material

The repository intentionally retains historical reconstruction, migration, balance, and stage documents as evidence and recovery material.

They should not override:

1. `AGENTS.md`
2. `docs/CURRENT_STATE.md`
3. `docs/TODO.md`
4. this current architecture document
5. the specific current subsystem document listed in `docs/INDEX.md`

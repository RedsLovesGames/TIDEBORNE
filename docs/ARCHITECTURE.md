# Tideborne architecture map

Status: current architecture reference for the maintained `dev` branch.

Fishing System 2.0 is implemented. This document describes the current ownership model and the direction for future work. Historical reconstruction details remain in `docs/RECONSTRUCTION.md` and stage-specific documents.

## Core design rule

Tideborne should have one canonical owner for each gameplay decision and one shared read/presentation boundary for downstream consumers.

Do not solve local integration problems by creating parallel specimen, score, gear, record, or presentation models.

## Canonical Fishing System 2.0 domain

### `com.redslovesgames.tideborne.fishing`

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

`tideteamjournal.client.TeamStatsComponent` directly owns the compact Journal stats layout and its required height. Both use the same size-record visibility rule; specimen fields come from `ClientJournalSpecimens` and `CanonicalSpecimenPresentation`. Tide's Fish Profile component-insertion hook remains the external integration boundary.

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

### `com.redslovesgames.tideborne.fishing.tide` and `com.redslovesgames.tideborne.compat`

Tide-specific fishing adapters live under `fishing.tide`; optional-mod integration lives under `compat` (for example `compat.apex`). Gear-owned Tide equipment behavior lives under `fishing.gear`, while ecosystem behavior such as chum/shark handling lives under `ecosystem`. Version-sensitive Tide mixins live under `tideborne.mixin.tide` when no stable API/event seam can replace them.

Use `docs/TIDE_MIXIN_INVENTORY.md` before modifying Tide-targeting mixins.

## Team Journal boundary

### `com.redslovesgames.tideborne.journal`

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

## Satchel, discovery, presentation, and historical specimen compatibility

### `com.redslovesgames.tideborne.satchel`, `com.redslovesgames.tideborne.discovery`, and `com.redslovesgames.tideborne.fishing.specimen.legacy`

Angler's Satchel ownership lives under `satchel`; discovery persistence lives under `discovery`; rendering/presentation helpers live under `presentation`; specimen transfer and canonical specimen logic live under `fishing.specimen`. Historical Tide Traits codec/model compatibility is isolated under `fishing.specimen.legacy` until later migration cleanup.

The Satchel is not a canonical specimen generator. It may store, sort, protect, summarize, and present canonical catches after generation. Legacy compatibility paths must remain guarded so they cannot reroll or overwrite a current canonical specimen.

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

Avoid mutually recursive dependencies between Tideborne feature packages. Prefer canonical API and presentation boundaries over cross-feature implementation reach-through.

## Mixins

The current active mixin set contains Tide targets, vanilla targets, Tideborne-owned targets, and one optional Apex target. The authoritative classification and fragility notes live in `docs/TIDE_MIXIN_INVENTORY.md`.

Policy:

- mixins should adapt lifecycle or inaccessible state
- gameplay calculations should live in named services
- Tide-targeting mixins are treated as version-sensitive unless a stable seam is documented
- RecordHolderStore directly owns canonical sidecar migration/capture and client metadata projection; TeamJournalService marks team-backed data on successful resolution and preserves unmarked native fallback. TeamProgressStore score/index overlays remain internal architecture debt.

Do not perform a broad mixin purge simply to reduce the count.

## Post-2.0 gear architecture

Post-2.0 gear progression and physical Satchel tackle management are implemented. Ownership is converged:

- FishingGearRegistry owns exact identity, slots and bobber defaults; FishingGearModifiers composes immutable inputs, and FishingGearEffects consumes complete contributions with shared caps.
- TideborneFishingGearModifiers adapts native equipped items to encounter/fight/preview inputs. Native bait aggregation, eligibility and Gold luck stay native; attributed bait/bobber luck enters the canonical selector once.
- SpeciesSelectionService owns capped metadata-based target/rarity weights. TideSpeciesSelectionBridge owns server generation; Trait Luck enters SpecimenGenerator, while FishScoreV2Service retains score authority.
- FightProfileService projects species/specimen difficulty, positive-surcharge Trophy relief and composed equipment. The compatibility minigame fallback is used only without canonical catch state.
- BobberGearModifiers captures server-configured values per cast. BobberBonuses retains synchronized display data. LeaderAttachment owns persisted-tier translation; SteelLeaderGearModifiers remains an Apex facade over canonical rod/bobber/leader protection.
- SatchelPreset stores actual items, names/UUIDs and legacy references. SatchelTackleExchange plans item-conserving swaps; SatchelTackleHandler commits server inventory changes. SatchelGearSummary infers labels through canonical modifiers; no copied totals or parallel equipped state are persisted.
- TideborneFishingApi remains the covered internal query boundary; CanonicalSpecimenPresentation owns specimen display semantics.

No gear or UI path directly authors specimen percentile, size, axes, seed or score. Existing save/network IDs and migration readers remain. The seven archetypes are equipment combinations, not classes or preset bonuses. Statistical measurements are complete in POST_2_0_GEAR_BALANCE_REPORT.md; default boss targeting and practical all-seven balance acceptance remain open. See CURRENT_STATE.md for validation and limitations.

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

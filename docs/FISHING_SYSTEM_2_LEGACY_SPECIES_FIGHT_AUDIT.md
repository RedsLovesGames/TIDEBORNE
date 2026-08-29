# Fishing System 2.0 Legacy Species/Fight Audit

Stage 52 removes the remaining duplicate species-selection and fight-calculation paths from active catches.

## Species selection

- `FishSelector#getResult` is intercepted at method entry by `FishSelectorMixin` and delegated to `TideSpeciesSelectionBridge`.
- Active V2 fish selection therefore does not execute Tide's legacy within-pool weighting path or its `selection_quality` calculation.
- `TideSpeciesProfileAdapter` continues to call Tide `FishData#shouldKeep(context)`, preserving authoritative biome, weather, dimension, bait, time, and other Tide environment restrictions.
- Tide per-species `FishingModifier` entries remain part of the adapted encounter weight.
- Shark Tooth and Seafarer's Hook compatibility weighting is resolved as canonical `FishingGearModifiers` directly at the adapter boundary.
- Canonical Fishing Luck remains applied once by `SpeciesSelectionService` through canonical rarity.
- The old global `FishData#weight(context)` mixin is no longer registered, so there is no second compatibility weighting pass and no route back through legacy `selection_quality`.

## Fight calculation

`FightProfileService` is the single owner of active V2 fight math:

1. normalize Tide species strength and speed/tempo,
2. apply canonical final-percentile scaling,
3. apply Body Type strength/tempo modifiers,
4. apply canonical bait/gear strength and tempo modifiers,
5. derive catch-zone area from final strength,
6. project the canonical fight into Tide minigame area/speed exactly once.

The minigame projection also applies the retained named catch-zone and speed multipliers plus Tide's configured minigame difficulty multiplier. `FishingModifiers` now resolves compatibility gear and behavior only; it does not independently calculate strength, tempo, or catch-zone formulas.

Tide's legacy constructor constant-rewrite mixin for Copper/Iron/Golden/Diamond line effects is no longer registered. Those line effects are resolved through `TideFishingLineModifiers` into canonical `FishingGearModifiers` and consumed by `FightProfileService` during the one V2 minigame projection.

## Compatibility fallback

If a Tide minigame exists without canonical V2 catch state, compatibility behavior is retained without reconstructing a second fight. Tide's already-computed area/speed are passed through `FightProfileService#projectCompatibilityMinigame`, which applies only retained named addon gear multipliers and safety bounds.

This fallback does not perform species selection, percentile scaling, Body Type scaling, strength normalization, tempo normalization, or catch-zone derivation.

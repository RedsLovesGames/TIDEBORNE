# Stage 34: legacy Tide fishing-line modifier migration

## Scope

Stage 34 moves the four Tide 2.1.1 fishing-line fight multipliers under the canonical Fishing System 2.0 gear modifier model introduced in Stage 33. It does not migrate other rod, hook, bobber, bait, compatibility, or catch-pool behavior.

## Canonical mapping

| Tide line | Canonical axis | Multiplier |
| --- | --- | ---: |
| Copper Line | Tempo | 0.90 |
| Iron Line | Strength | 0.86 |
| Golden Line | Tempo | 0.95 |
| Diamond Line | Strength | 0.75 |

`TideFishingLineModifiers` owns these values as immutable `FishingGearModifiers`. The opposite fight dimension remains exactly `1.0`, and unrelated modifier dimensions remain neutral.

## Runtime integration

`FishCatchMinigameLineModifierMixin` replaces Tide 2.1.1's four hard-coded constructor constants with the corresponding values from `TideFishingLineModifiers`. Tide's existing line identity checks remain only as the compatibility selector, while the numerical source of truth is the canonical Stage 33 modifier model.

This deliberately does not add a second post-processing multiplier. Each selected line therefore affects the fight exactly once at the same location and ordering as Tide 2.1.1. Existing hook, bobber, fishing-power, fish behavior/value, and minigame conversion logic is not intercepted.

`FightProfileService` is unchanged and remains the sole canonical specimen/base fight-profile calculation. Stage 34 only migrates the gear layer that is applied after that profile.

## Regression coverage

`TideFishingLineModifiersTest` verifies:

- exact 0.90 / 0.86 / 0.95 / 0.75 legacy equivalence;
- the unaffected Strength or Tempo dimension remains 1.0;
- neutral fallback behavior;
- composition with an existing Stage 33 modifier set;
- one application equals the legacy result and a duplicate application would differ.

The mixin configuration explicitly registers the runtime bridge so normal build/GameTest startup also validates that the target is loadable against the exact Tide 2.1.1 dependency.

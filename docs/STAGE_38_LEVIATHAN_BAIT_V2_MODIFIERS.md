# Stage 38 - Leviathan Bait V2 modifiers

Stage 38 finishes Leviathan Bait's Fishing System 2.0 modifier migration on top of the fish-only catch-pool behavior from Stage 37.

## Canonical modifier set

When Leviathan Bait is active, `TideborneFishingGearModifiers.leviathanBait(...)` now produces one canonical `FishingGearModifiers` value containing all bait behavior:

- catch category restricted to fish only
- Fishing Luck +15
- Trait Luck +8
- Strength x1.15
- Tempo x1.15

When the bait is inactive, the modifier set is fully neutral.

The authoritative V2 specification froze the Leviathan Fishing Luck and fight multipliers but described Trait Luck only as a substantial bonus. The only numeric V2 design example elsewhere in the project uses Trait Luck +8. Stage 38 therefore freezes +8 as the implemented default rather than silently inventing a different value. A later explicit balance-spec revision may change that number.

## Fishing Luck and Trait Luck separation

The Tide-to-V2 selection bridge now resolves Leviathan Bait before canonical species selection and applies its first-class luck fields to the server-owned `FishingContext`.

The two luck axes stay independent:

- Fishing Luck is added only to `FishingContext.fishingLuck()` and is consumed by `SpeciesSelectionService` while weighting eligible species.
- Trait Luck is added only to `FishingContext.traitLuck()` and is consumed after species selection by canonical specimen trait probability generation, together with the selected species' captured Trait Momentum and later Perfect Catch rewards where applicable.
- Trait Luck does not participate in species weights.
- Fishing Luck is not passed into trait probability calculations.

Leviathan Bait no longer mutates Tide's legacy fishing context luck before invoking the fish selector. The Stage 37 fish-only branch now calls the ordinary Tide fish selector with the original Tide context, and the V2 bridge applies +15 Fishing Luck through the canonical context instead.

## Canonical FightProfile integration

`FightProfileService.applyGearModifiers(...)` is the one first-class fight modifier application point for canonical Strength and Tempo gear/bait fields.

The runtime order for Leviathan catches is now:

1. adapt the Tide context and add Leviathan Fishing Luck/Trait Luck
2. select an eligible species through the normal V2 species selector
3. generate the pre-fight specimen and Body Type
4. create the canonical species/size/Body-Type `FightProfile`
5. apply Leviathan Strength x1.15 and Tempo x1.15 to that canonical profile
6. store that final canonical profile in catch state
7. let the existing minigame consume it before built-in Tide line and active Tideborne line/leader layers

After Strength changes, catch-zone area is recomputed from the canonical Strength formula. Tempo continues to use the existing final canonical Tempo bounds.

## Removed duplicate Leviathan calculations

The old bait-specific runtime calculations are no longer active:

- no `FishingContext.luck() + leviathanBaitFishSelectionLuckBonus` clone
- no Leviathan catch-zone multiplier after canonical profile consumption
- no Leviathan minigame-speed multiplier after canonical profile consumption
- no legacy `selection_quality` path

The old reconstructed configuration fields may remain for save/config compatibility, but Stage 38 runtime behavior no longer reads the legacy fish-selection-luck, catch-zone, or minigame-speed fields. The canonical V2 modifier set is the gameplay authority.

## Regression coverage

`LeviathanBaitModifiersTest` freezes:

- exact active +15 Fishing Luck
- exact active +8 Trait Luck
- exact Strength x1.15
- exact Tempo x1.15
- preserved fish-only category restriction
- fully neutral inactive bait
- additive composition of Fishing Luck and Trait Luck onto the canonical context as separate fields
- Fishing Luck changing species weight while Trait Luck does not
- Trait Luck increasing trait-event probability without participating in species weighting
- fight multipliers applying to a canonical `FightProfile`
- catch-zone recomputation from modified Strength
- behavior preservation

The Stage 37 `LeviathanBaitCatchPoolTest` continues to freeze fish-only selector routing and inactive normal-pool behavior.

## Stage boundary

Stage 38 changes only Leviathan Bait V2 modifiers and their direct runtime consumers. It does not begin the Stage 39 native Tide SpeciesProfile audit or later compatibility-profile work.

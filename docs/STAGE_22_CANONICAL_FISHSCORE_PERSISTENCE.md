# Stage 22: canonical FishScore persistence

Stage 22 integrates the Stage 21 pure `FishScoreV2Service` into finalized Fishing System 2.0 specimen state.

## Canonical ordering

FishScore is not generated during base or pre-fight specimen generation. `SpecimenGenerator.finalizeAfterFight` finalizes, in order, the Perfect Catch state, Body Type and physical size, Condition, Pigmentation, and Specimen Quality. Only after those axes are final does it call `FishScoreV2Service` and copy both the raw score and normalized 1 through 3000 FishScore into the immutable `SpecimenData`.

This keeps the score derived from one finalized canonical specimen and prevents a score from being frozen before a later axis changes.

## Persistence

Two persistent data components are canonical for finalized V2 catches:

- `tide_traits:specimen_raw_fish_score`
- `tide_traits:specimen_fish_score`

`CanonicalSpecimenStorage` writes them from the existing `SpecimenData.rawFishScore` and `SpecimenData.fishScore` fields. Existing mutation, Body Type, percentile, and Tide length components remain compatibility mirrors only.

## Transfer compatibility

`SpecimenTransfer` data version is now 5. The shared specimen transfer payload explicitly carries `CanonicalRawFishScore` and `CanonicalFishScore` in addition to the generic component snapshot. The same payload is used by item to entity, entity to item, entity to bucket, bucket to entity, and direct stack to bucket paths, so FishScore does not depend on one representation retaining a particular component snapshot implementation.

Older specimens without the new fields remain readable. No legacy score formula is promoted to canonical state.

## UI boundary

This stage does not migrate every journal, tooltip, or UI consumer. New V2 code must treat the persisted normalized component as authoritative and must not recalculate FishScore from legacy mutation, weight, or rarity helpers. Legacy fallback behavior is retained only for records that do not yet carry canonical V2 FishScore.

## Tests

Coverage added in this stage proves that pre-fight specimens have no score, finalized specimens store the exact `FishScoreV2Service` result deterministically, and the shared transfer payload round-trips both canonical score values together with the independent specimen axes.

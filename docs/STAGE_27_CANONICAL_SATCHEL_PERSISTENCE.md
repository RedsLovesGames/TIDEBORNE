# Stage 27: Canonical Angler's Satchel persistence

## Decision

The Angler's Satchel continues to persist fish through Tide 2's existing `SATCHEL_CONTENTS` component, whose entries are complete `ItemStack` values. Fishing System 2.0 canonical specimen state already lives on the fish stack through `CanonicalSpecimenStorage`, so the Satchel must treat the nested stack as opaque persistence data.

No Satchel-specific specimen NBT or second canonical serialization format is introduced.

## Runtime contract

- Insertion stores copies of the offered fish stack, preserving all stack data components.
- `contents` returns copies of the stored stacks without generating, migrating, normalizing, or recalculating specimen state.
- Extraction returns a copy of the exact stored fish stack and removes only that Satchel entry.
- Canonical V2 fields therefore survive species, schema/generation versions, deterministic seed, natural/base percentile, base/final length, final percentile, Body Type, Condition, Pigmentation, Specimen Quality, Perfect Catch, raw FishScore, and mapped FishScore.
- Existing Tide/Tideborne legacy specimen components remain intact and remain readable from old `SATCHEL_CONTENTS` data.
- Reading or removing a legacy-only fish does not synthesize a canonical specimen.
- Unrelated stack metadata, including custom name and addon `CUSTOM_DATA`, is preserved by the same opaque-stack contract.
- Satchel UI behavior and layout are unchanged in this stage.

## Recalculation boundary

There are no calls from `AnglersSatchelStorage` to V2 generation, trait selection, or FishScore calculation services. The Satchel does not repair or regenerate specimen identity. Canonical migration, when explicitly required elsewhere, remains outside Satchel persistence.

## Coverage

`AnglersSatchelPersistenceGameTests` adds two end-to-end persistence checks:

1. A fully populated canonical V2 specimen is inserted, read while nested, and extracted. Every canonical specimen field, both score fields, custom name, and unrelated custom component data are asserted unchanged, and extraction is asserted not to duplicate the entry.
2. A legacy-only fish is loaded directly from the pre-existing Tide `SATCHEL_CONTENTS` representation, read, and extracted. Legacy mutation, seed, percentile, Tide length, and custom name survive unchanged, while canonical reads remain empty before and after removal.

These tests intentionally exercise the existing stack representation rather than a new serializer so future changes cannot silently fork Satchel persistence away from canonical stack persistence.

# Stage 24: canonical fish entity transfer

Updated: 2026-08-28

Status: complete on `dev`.

## Goal

Make the living-fish representation a lossless carrier of canonical Fishing System 2.0 specimen identity. ItemStack to entity to ItemStack and ItemStack to entity to bucket to entity to ItemStack must return exactly the same canonical specimen.

## Canonical authority

`CanonicalSpecimenStorage` remains the only canonical serializer/deserializer. `SpecimenTransfer` copies its `CanonicalSpecimen` transfer payload through living entities and buckets without regenerating or normalizing any field.

For a canonical transfer payload, live `FishLengthHolder` state is not allowed to overwrite transfer `LengthCm`. The live Tide length is a runtime/display behavior mirror only. On stack or bucket export, canonical `finalLength` remains authoritative. Legacy-only specimens retain the previous behavior where the current live Tide length may update the legacy transfer mirror.

This preserves exactly:

- species ID
- schema version
- generation version
- deterministic seed
- base percentile
- base length
- final length
- final percentile
- Body Type
- Condition
- Pigmentation
- Specimen Quality
- Perfect Catch
- raw FishScore
- normalized FishScore
- provenance

No transfer path samples RNG or invokes specimen generation, trait selection, size generation, percentile generation, or FishScore calculation.

## Tide behavior and compatibility

The transfer code continues to operate on the already-created living entity. It does not replace or recreate the entity type, so the actual fish species/entity behavior remains owned by Tide/Minecraft.

When a canonical stack enters a living fish, canonical `finalLength` is applied to Tide's existing `FishLengthHolder` so render/display and entity behavior continue to see the expected physical length. Generic source-stack component snapshots are still carried where registry access is available, preserving names, addon/custom components, protection state, and other compatibility data. Existing legacy mirrors remain available to current display and compatibility consumers, but they are not canonical authority.

## GameTests

`CanonicalEntityTransferGameTests` covers three deliberately different trait combinations:

1. Giant + Scarred + Iridescent + Perfect Specimen + Perfect Catch
2. Dwarf + Parasite-Ridden + Albino + normal quality
3. all-normal axes with Perfect Catch

Each fixture carries distinct seed, natural/final percentile, base/final length, raw FishScore, and normalized FishScore. Tests deliberately mutate the living entity's Tide length after stack-to-entity transfer and prove that both direct entity-to-stack export and the entity/bucket/entity path still restore every canonical field exactly. They also assert that the living fish remains the same `EntityType.COD` and that canonical final length is reapplied to Tide's `FishLengthHolder` on entity restoration.

## Validation

GitHub Actions run `33186940240` is green for implementation commit `100c243e6a1c7c3484ad17c2c07647e6a7e6904a`:

- exact external dependency fetch passed
- reconstruction identifier validation passed
- `./gradlew clean build --stacktrace` passed, including unit tests
- `./gradlew runGametest --stacktrace` passed, including `CanonicalEntityTransferGameTests`
- built JAR artifact upload passed

The validated transfer path therefore preserves canonical seed, natural and final percentile/size state, every canonical trait axis, Perfect Catch, and both FishScore fields without rerolling or recomputing them, while retaining the existing living fish entity type and Tide length behavior.

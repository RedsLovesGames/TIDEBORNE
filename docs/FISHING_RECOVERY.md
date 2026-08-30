# Fishing System 2.0 Legacy Fish Recovery

Fishing System 2.0 includes operator-only server commands for recovering fish created before the canonical V2 specimen schema.

## Repair

Use repair when the goal is to preserve recoverable legacy specimen identity and normalize it into current canonical data.

- `/tideborne fishing repair held`
- `/tideborne fishing repair inventory`

Repair delegates to the existing one-way `CanonicalSpecimenStorage` legacy migration boundary. It does not call the specimen generator and does not invent a second percentile or trait roll. Recoverable legacy species, deterministic seed, percentile/physical length, Giant/Dwarf state, legacy Condition/Pigmentation/Perfect Specimen mappings, and other stack metadata are retained where the legacy payload contains them. Missing canonical FishScore is calculated from the final preserved specimen using the single V2 FishScore service.

Repair is idempotent. A fish already written as the current canonical schema is reported as current and is not migrated again. Unsupported, malformed, incomplete, newer-schema, and non-fish stacks fail closed.

## Destructive reroll

Use reroll only when the old specimen identity should intentionally be replaced with a newly generated canonical specimen.

- `/tideborne fishing reroll held --confirm`
- `/tideborne fishing reroll inventory --confirm`

The `--confirm` literal is mandatory. Running the reroll target without it performs no migration and no write. A confirmed reroll keeps the Tide fish species/item but replaces specimen seed, percentile/size, Body Type, Condition, Pigmentation, Quality, Perfect Catch state, and FishScore with a newly generated canonical specimen. Non-specimen stack metadata is not cleared by the recovery service.

Both repair and reroll execute on the server and require operator permission level 2. The client does not author canonical specimen values.

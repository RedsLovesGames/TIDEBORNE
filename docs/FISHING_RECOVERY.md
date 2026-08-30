# Fishing System 2.0 Legacy Fish Recovery

Fishing System 2.0 includes operator-only server commands for recovering fish created before the canonical V2 specimen schema, plus a non-catch Journal backfill for old fish the player still owns.

## Repair

Use repair when the goal is to preserve recoverable legacy specimen identity and normalize it into current canonical data.

- `/tideborne fishing repair held`
- `/tideborne fishing repair inventory`

Repair delegates to the existing one-way `CanonicalSpecimenStorage` legacy migration boundary. It does not call the specimen generator and does not invent a second percentile or trait roll. Recoverable legacy species, deterministic seed, percentile/physical length, Giant/Dwarf state, legacy Condition/Pigmentation/Perfect Specimen mappings, and other stack metadata are retained where the legacy payload contains them. Missing canonical FishScore is calculated from the final preserved specimen using the single V2 FishScore service.

Repair is idempotent. A fish already written as the current canonical schema is reported as current and is not migrated again. Unsupported, malformed, incomplete, newer-schema, and non-fish stacks fail closed.

The repair commands also run the owned-fish Journal backfill described below. This allows an old Tide fish that retained only its physical length, and therefore was not classified as a Tideborne legacy specimen by the original repair service, to be deterministically normalized and used to fill missing Journal specimen display data.

## Owned old-fish Journal backfill

`OwnedFishJournalBackfill` fills a missing canonical `latest` Journal specimen from an actual fish item the player still owns. It runs server-side on login and FTB team changes, and is also invoked by the repair commands.

Safety and preservation rules:

- the species must already be unlocked in the authoritative Tide Journal;
- owning a fish does not unlock an uncaught species;
- an existing canonical `latest` Journal specimen is never overwritten;
- current canonical fish are copied as-is;
- recoverable Tideborne legacy fish use the existing deterministic migration boundary;
- old Tide fish that retain only a valid physical length are deterministically canonicalized from their registered species and preserved length;
- the backfill does not call `TidePlayerData.logCatch`;
- total caught, first-catch date, largest/smallest historical stats, record ownership, history events, contributor totals, rewards, and Trait Momentum are not incremented or replayed;
- repeated backfill is idempotent.

This means a player does not need to catch the same species again merely to populate the Fishing System 2.0 specimen portion of an already-unlocked Journal entry, provided an eligible old fish item is still available to the server-side backfill.

## Destructive reroll

Use reroll only when the old specimen identity should intentionally be replaced with a newly generated canonical specimen.

- `/tideborne fishing reroll held --confirm`
- `/tideborne fishing reroll inventory --confirm`

The `--confirm` literal is mandatory. Running the reroll target without it performs no migration and no write. A confirmed reroll keeps the Tide fish species/item but replaces specimen seed, percentile/size, Body Type, Condition, Pigmentation, Quality, Perfect Catch state, and FishScore with a newly generated canonical specimen. Non-specimen stack metadata is not cleared by the recovery service.

Both repair and reroll execute on the server and require operator permission level 2. The client does not author canonical specimen values.

## Validation

Owned-fish Journal backfill implementation commit `44c4803f8f6bee16eb76b162b82883398a8bd3ca` is validated by GitHub Actions run `33323297138`. The clean build, unit tests, all four Fabric GameTest compatibility matrices, dedicated-server/client-connect smoke test, production JAR validation, artifact upload, and release refresh all passed.

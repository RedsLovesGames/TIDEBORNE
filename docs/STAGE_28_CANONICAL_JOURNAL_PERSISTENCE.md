# Stage 28: Canonical personal and team Journal persistence

## Decision

Personal and team Journals retain Tide's existing journal/statistics NBT for compatibility and UI continuity. Fishing System 2.0 adds a sidecar named `FishingSystem2JournalSpecimens`, keyed by canonical species ID, with `latest`, `largest`, and `smallest` specimen snapshots.

Each snapshot is serialized through `CanonicalSpecimenStorage.writeTransferData` and read through `CanonicalSpecimenStorage.readTransferData`. No Journal-specific specimen codec, score calculation, mutation generation, percentile roll, or size roll exists.

## Personal Journal

The server-side catch completion path writes the finalized canonical specimen into the player's persistent root. The existing Tide `TidePlayerData` fish statistics remain intact and continue to drive the current UI. Record snapshots are updated only when the already-finalized canonical `finalLength` beats the pre-catch native largest or smallest value. If old personal data has no canonical sidecar, it is left unchanged and no specimen is invented from aggregate legacy statistics.

## Team Journal

Team catch capture keeps a server-thread reference to the already-finalized caught stack only for the duration of Tide's `logCatch` call. `RecordHolderStore.updateAfterCatch` then stores the same canonical specimen into the FTB team persistent root. Existing `journal`, `record_holders`, contributors, history, and top-fish data are not replaced.

The team FishScore bridge now treats a current canonical specimen as authoritative. It returns the persisted canonical FishScore, and if a current canonical specimen has no finalized score it returns unavailable rather than invoking the legacy score formula. Legacy/noncanonical fish retain the old compatibility fallback.

## Compatibility and migration

- Existing personal and team journal/world data is preserved byte-for-byte outside the new sidecar.
- Legacy-only records are not assigned fabricated canonical percentiles, traits, quality, or FishScore.
- The first later canonical catch for a species can populate `latest`, and can populate `largest` or `smallest` only if it actually wins that native record.
- Repeated capture of the same canonical specimen is idempotent.
- Species entries are isolated by canonical species ID.
- Current Journal UI/network packet shapes remain unchanged in this stage. The sidecar is server-owned persistence for later UI migration.

## Canonical fields

The sidecar carries the complete existing canonical payload: species, schema/generation versions, deterministic seed, base percentile, base length, final length, final percentile, Body Type, Condition, Pigmentation, Specimen Quality, Perfect Catch, raw FishScore, and mapped FishScore. Journal consumers therefore have the canonical percentile, length, traits, quality, and score without recalculation.

## Coverage

`JournalSpecimenStoreTest` verifies complete canonical round-trip serialization, selective latest/largest/smallest updates, preservation of unrelated legacy journal/team NBT, absence of synthetic migration for legacy-only data, idempotent repeated capture, and species isolation.

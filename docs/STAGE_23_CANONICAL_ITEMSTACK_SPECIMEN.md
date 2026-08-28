# Stage 23: Canonical ItemStack specimen persistence

Status: complete on `dev` after validation.

## Canonical ownership

`CanonicalSpecimenStorage` is the single Fishing System 2.0 ItemStack persistence boundary. A current canonical stack stores all specimen identity required to reconstruct `SpecimenData` without consulting compatibility state:

- species ID
- schema version
- generation version
- deterministic seed
- base percentile
- base length
- final length
- final percentile
- percentile definition marker
- Body Type
- Condition
- Pigmentation
- Quality
- Perfect Catch
- raw FishScore when present
- final FishScore when present

The canonical percentile definition is `size_adjusted_final_percentile`. `basePercentile` remains the original natural specimen percentile. `finalPercentile` is the percentile represented by canonical final physical length after deterministic physical modifiers, as frozen by Fishing System 2.0.

The following legacy components remain compatibility mirrors only: mutation seed, size percentile, Body Type, mutation/condition, and Tide fish length. Canonical reads never use them as fallback values.

## Read and write contract

`CanonicalSpecimenStorage.write` writes the complete canonical payload first and then refreshes legacy mirrors.

`CanonicalSpecimenStorage.read` is side-effect free. It does not migrate, clamp, normalize, mirror, regenerate, or reroll any specimen value. It returns a specimen only when the canonical payload is complete and its schema, generation version, and percentile definition match the current implementation.

Optional pre-fight raw/final FishScore fields remain absent when they have not been calculated. Reading a stack never calculates them.

## Explicit migration detection

`CanonicalSpecimenStorage.detectMigration` classifies stacks as:

- `NONE`
- `LEGACY_ONLY`
- `CANONICAL_OLDER_SCHEMA`
- `CANONICAL_NEWER_SCHEMA`
- `CANONICAL_OLDER_GENERATION`
- `CANONICAL_NEWER_GENERATION`
- `CANONICAL_INCOMPLETE`
- `CANONICAL_CURRENT`

Detection never modifies the stack. Incomplete or version-mismatched canonical payloads are not repaired from legacy mirrors.

## Entity and bucket transfer

`SpecimenTransfer` delegates canonical payload serialization and restoration to `CanonicalSpecimenStorage`. The transfer payload contains the same complete canonical identity under `CanonicalSpecimen`.

Canonical transfer reads never fall back to legacy transfer mirrors. If a canonical transfer root exists but is incomplete, has an unsupported version, or carries an unknown percentile definition, restoration refuses that canonical payload instead of synthesizing a specimen from compatibility keys.

Legacy-only transfer data remains supported on its separate compatibility path. Full source-stack snapshots remain an additional generic component preservation mechanism, not a second canonical specimen decoder.

## Serialization coverage

`CanonicalSpecimenStorageTest` covers:

- complete canonical ItemStack round trip of every persisted field
- canonical seed, final percentile, and final length authority when legacy mirrors disagree
- side-effect-free reads with stale mirrors
- incomplete canonical payload detection
- legacy-only migration detection
- older and newer schema detection
- older and newer generation detection
- canonical transfer NBT round trip
- unknown percentile-definition rejection
- incomplete transfer rejection without synthesized defaults
- `SpecimenTransfer` canonical delegation without read-time mirror mutation
- invalid canonical transfer refusal without legacy fallback
- absence-preserving pre-fight FishScore serialization

No read path introduced by this stage samples RNG or calls specimen generation.

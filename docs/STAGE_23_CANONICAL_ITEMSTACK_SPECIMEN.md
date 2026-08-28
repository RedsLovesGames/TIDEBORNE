# Stage 23: Canonical ItemStack specimen persistence

Status: complete on `dev`.

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

Ordinary unit tests in `CanonicalSpecimenStorageTest` cover the registry-independent canonical transfer codec:

- complete round trip of every canonical specimen field
- explicit canonical percentile definition and preservation of both base and final percentiles
- unknown percentile-definition rejection without normalization
- incomplete canonical payload rejection without synthesized defaults
- older and newer schema-version rejection
- older and newer generation-version rejection
- absence-preserving pre-fight FishScore serialization

Fabric server GameTests in `CanonicalSpecimenStorageGameTests` cover actual registered ItemStack behavior:

- complete ItemStack write/read plus transfer round trip of every canonical field
- canonical seed, percentile, length, Body Type, and Condition authority when legacy mirrors disagree
- side-effect-free canonical reads that leave deliberately stale mirrors untouched
- explicit `LEGACY_ONLY` and `CANONICAL_INCOMPLETE` migration classification
- incomplete canonical stacks refusing repair from legacy mirrors
- invalid declared canonical transfer payloads refusing legacy-key fallback

No read path introduced by this stage samples RNG or calls specimen generation.

## Validation

GitHub Actions run `33181805114` is green for commit `fdd51c43c8576bbe3b3da05175307d0203bad1e9`:

- exact external dependency fetch and reconstruction-identifier validation passed
- `./gradlew clean build --stacktrace` passed, including all unit tests
- `./gradlew runGametest --stacktrace` passed, including the canonical ItemStack persistence GameTests
- built JAR artifact upload passed

The first validation run exposed that direct ItemStack unit tests were loading registered mod components outside Fabric server bootstrap. Those assertions were moved to Fabric GameTests rather than weakening the production persistence boundary. The registry-independent transfer codec remains covered by ordinary JUnit tests.

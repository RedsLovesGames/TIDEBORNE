# Stage 31: ItemStack legacy migration integration

Stage 31 wires the Stage 30 deterministic legacy fish migration core into the canonical Fishing System 2.0 ItemStack read path.

## Runtime boundary

`CanonicalSpecimenStorage.read(ItemStack)` now behaves as follows:

- `CANONICAL_CURRENT`: decode the current canonical payload directly and do not consult legacy mirrors.
- `LEGACY_ONLY`: attempt one deterministic migration only when the ItemStack item is present in Tide's authoritative fish registry.
- `CANONICAL_OLDER_SCHEMA`: attempt one deterministic migration for the registered fish item, preferring usable canonical values already present on the older payload before legacy mirrors.
- `CANONICAL_NEWER_SCHEMA`, generation-only version mismatches, `CANONICAL_INCOMPLETE`, `NONE`, empty stacks, and malformed payloads: fail closed without modifying the ItemStack.

A successful migration is written immediately through `CanonicalSpecimenStorage.write`, which stamps the current schema/generation and refreshes compatibility mirrors. A second read therefore takes the `CANONICAL_CURRENT` path and cannot rerun migration from subsequently changed legacy mirrors.

## Value preservation

Migration keeps existing identity data whenever it is usable:

- canonical deterministic seed is preferred over the legacy mutation seed;
- canonical base percentile is preferred over the legacy percentile mirror;
- canonical final physical length is preferred over the legacy Tide fish-length value;
- canonical Body Type is preferred over the legacy Body Type mirror;
- for older-schema payloads, usable canonical base length, final percentile, Condition, Pigmentation, Quality, Perfect Catch state, raw FishScore, and FishScore are carried forward rather than discarded.

Missing values still use the deterministic Stage 30 migration rules. No specimen generator call or new random sample is introduced by ItemStack loading.

## Safety rules

- Non-fish ItemStacks are never migrated, even if they contain legacy-looking Tide Traits components.
- An older-schema payload whose declared species conflicts with the registered fish item fails without being rewritten.
- Incomplete current-schema payloads are not silently repaired from compatibility mirrors.
- A migration exception or invalid legacy value returns an empty read and leaves the original ItemStack unchanged because canonical write occurs only after successful conversion.

## Tests

`CanonicalSpecimenStorageGameTests` covers:

- Giant + Scarred legacy migration;
- Dwarf + Albino legacy migration;
- Perfect Specimen legacy Quality migration;
- preserved seed, percentile, and physical length;
- preservation of usable older canonical values;
- successful migration stamping the current schema;
- repeated reads not remigrating from changed legacy mirrors;
- malformed older-schema fish failing without mutation;
- non-fish items remaining untouched;
- incomplete current-schema payloads continuing to fail closed;
- current canonical reads continuing to ignore stale compatibility mirrors.

The Angler's Satchel persistence regression now reflects the same boundary: reading Satchel contents alone leaves a legacy specimen untouched, the first canonical specimen read migrates it once, later reads use persisted canonical state, and extraction preserves that canonical identity plus unrelated ItemStack metadata such as the custom name. Legacy compatibility mirrors are not treated as post-migration authorities.

## Validation

GitHub Actions run `33210443398` is green for implementation/test commit `ba19d216869732521420b7bc9f2c3859f6e0d164`. The exact-dependency `./gradlew clean build --stacktrace`, unit tests included by the build, all 16 Fabric GameTests, and built-JAR artifact upload completed successfully.

This stage does not redesign later Journal, UI, networking, or general persistence consumers and does not implement a separate FishScore recalculation policy beyond preserving an existing usable canonical score during older-schema migration.

# Tideborne internal fishing API

Updated: 2026-09-04

## Purpose

`com.redslovesgames.tideborne.api.TideborneFishingApi` is the small canonical read/query boundary for Fishing System 2.0 state.

New Tideborne feature code should prefer this facade when it needs information that already has a canonical owner. The facade exists to keep screens, commands, HUD code, record presentation, compatibility code, and future features from reaching directly into persistence adapters or implementation services.

This is an internal Tideborne maintenance boundary, not a compatibility promise for third-party mods.

## Canonical reads exposed

The facade currently provides these groups of reads and queries:

- specimen reads from `ItemStack` and canonical transfer/record NBT;
- stored canonical FishScore and raw FishScore reads;
- explicit fishing-gear modifier queries for Fishing Luck, Trait Luck, Strength, Tempo, Body Type chance, named additive/multiplier modifiers, and category/catch-pool restrictions;
- exact registered fishing-gear profile lookup;
- Tide species profile lookup by fish stack or namespaced species ID, plus stable species-profile enumeration;
- canonical record comparison, record replacement, specimen-identity comparison, highest team score, and Team Top Fish reads.

## Ownership rules

The API does not create a second specimen model. It returns the existing `SpecimenData` and `SpeciesProfile` domain records unchanged.

The API does not own persistence formats. `CanonicalSpecimenStorage` remains the canonical storage/migration boundary and `CanonicalSpecimenRecordIndexer` remains the canonical record-ordering boundary. Feature code should normally reach those implementations through `TideborneFishingApi` instead of importing them directly.

`readSpecimen(ItemStack)` may invoke the existing one-way legacy migration behavior owned by canonical storage. `readTransferredSpecimen(NbtCompound)` is a transfer/record read and does not add a second legacy fallback path.

Stored FishScore reads return the score already attached to the canonical specimen. Read paths do not reroll percentile, size, traits, or specimen identity.

Species-profile reads are context-independent metadata lookups. They do not apply current biome, weather, bait, fishing-luck, or eligibility rules.

## Dependency direction

For covered read/query use cases, prefer:

```text
screen / HUD / command / compatibility feature
                  |
                  v
          TideborneFishingApi
                  |
                  v
canonical domain + persistence/integration owners
```

Do not add UI-specific reconstruction of trait state, FishScore, record ordering, or species metadata when the canonical value is available through this boundary.

## Scope boundary

This API is intentionally not a replacement for the live catch pipeline. Server-authoritative catch generation, species selection, fight setup, Momentum mutation, persistence writes, and record writes remain owned by their existing application/integration services.

Existing callers do not need to be migrated in one large rewrite. Migrate callers when touching the relevant feature or when a dedicated presentation/integration cleanup stage targets them.

## Validation

Initial implementation commit: `7631fb1f3493b709a2bc2e96d01d1c2dd7b3e910`.

The implementation includes focused `TideborneFishingApiTest` coverage for stored/calculated score access, canonical gear queries, and canonical record ordering. The normal `dev` CI workflow for that commit completed successfully in GitHub Actions run `33861656348`.

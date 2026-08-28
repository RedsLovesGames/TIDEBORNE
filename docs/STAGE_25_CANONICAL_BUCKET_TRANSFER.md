# Stage 25: canonical bucket transfer

Updated: 2026-08-28

Status: complete on `dev`.

## Goal

Make bucket capture and release a verified lossless representation change for canonical Fishing System 2.0 specimens:

```text
fish entity
-> bucket item
-> released fish entity
-> fish stack where applicable
```

Bucket transfer is persistence only. It must never become a specimen-generation, reroll, normalization, scoring, or bucketability authority.

## Canonical persistence contract

`CanonicalSpecimenStorage` remains the only canonical serializer/deserializer. Bucket hooks delegate to `SpecimenTransfer`, which copies the existing `CanonicalSpecimen` transfer payload through `DataComponentTypes.BUCKET_ENTITY_DATA` under the existing `TideTraits` entity key.

The bucket round trip preserves every canonical specimen field exactly:

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

Canonical `finalLength` remains authoritative during transfer. Mutating Tide's live `FishLengthHolder` length before bucket capture cannot overwrite canonical size state. On release, canonical `finalLength` is reapplied to the released entity's existing Tide length behavior.

No bucket transfer method calls `SpecimenGenerator`, trait generators, percentile/size generators, FishScore calculation, or any RNG API. A fish with no specimen remains specimen-free across capture, release, and entity-to-stack export.

## Bucketability and compatibility

Stage 25 does not add or replace any bucketability decision.

- `BucketableMixin` runs at the tail of the existing concrete `FishEntity#copyDataToStack` path, after Minecraft/Tide has already chosen to capture that fish.
- `MobBucketItemMixin` wraps the existing `EntityBucketItem#spawnEntity` call to `Bucketable#copyDataFromNbt`; vanilla/Tide restoration runs first, then `SpecimenTransfer` restores the already-persisted specimen payload.
- `TideItemBucketMixin` retains its existing `FishData.fromBucket(...).isPresent()` gate before copying specimen state during Tide's stack-to-bucket conversion.
- no item is made bucketable by Tideborne, no entity type is replaced, and no optional/non-bucketable fish fallback is introduced.
- legacy-only specimen transfer remains available through the existing compatibility payload path.

## GameTests

`CanonicalEntityTransferGameTests` now exercises the real `FishEntity#copyDataToStack` bucket-capture hook instead of directly calling the transfer helper.

The canonical bucket test uses three deliberately different fixtures and verifies the full entity to bucket to released entity to fish-stack chain. Each fixture asserts exact equality for every canonical field and verifies that the bucket remains a cod bucket, the released entity remains a cod, and canonical final length is reapplied after release.

A separate no-specimen GameTest captures an ordinary uninitialized cod, passes its bucket NBT through the same release adapter used by `MobBucketItemMixin`, exports the released fish to a stack, and proves that neither canonical nor legacy specimen identity was generated at any point.

## Validation

Implementation commit `201a4bdb3cb484a08cbc94a259f85b761007a494` is green in GitHub Actions run `33187468739`.

The workflow completed successfully with:

- exact Tide 2.1.1 and Apex Waters 1.1.1 dependency fetch
- reconstruction identifier validation
- `./gradlew clean build --stacktrace`, including unit tests
- `./gradlew runGametest --stacktrace`, including the strengthened canonical bucket GameTests
- built-JAR artifact upload

Stage 25 therefore closes the bucket persistence verification gap without adding a second specimen serializer, changing Tide bucketability, or introducing any regeneration path.

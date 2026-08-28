# Stage 26: Canonical fish display persistence

Updated: 2026-08-28

## Scope

Fish displays now consume the Fishing System 2.0 specimen already persisted on the displayed fish `ItemStack`. This stage does not change Tide's display model selection, display shape, facing/orientation logic, or item placement/removal behavior.

## Canonical display authority

- Tide remains responsible for storing the complete displayed `ItemStack` in `FishDisplayBlockEntity`, serializing that stack under its existing `fish` block-entity field, and returning the same stack through `takeDisplayStack`.
- When `setDisplayStack` succeeds, Tideborne replaces only Tide's cached `lengthCm` value with canonical `SpecimenData.finalLength` when a current canonical specimen is present.
- The canonical species ID, Body Type, Condition, Pigmentation, Specimen Quality, Perfect Catch flag, raw FishScore, normalized FishScore, seed, percentile data, and lengths remain on the stored stack. No display-only specimen copy is introduced.
- The existing client display preview bridge continues to transfer the stored specimen payload to Tide's rendered entity. That payload includes canonical species and all canonical trait/score fields, while Tide continues to choose and orient the model using its native display metadata.
- Display reads are side-effect free. Placing, loading, rendering, taking, or saving a display does not call specimen generation and does not reroll any canonical field.

## Compatibility

Existing saved displays remain valid. A display stack without a current canonical specimen continues through Tide's original `FishLength` or species-average fallback unchanged. Loading an old display does not synthesize a Fishing System 2.0 specimen.

This keeps old block-entity NBT compatible because the serialized display format is unchanged: Tide still saves the original full fish stack under its existing `fish` key. Canonical stacks naturally persist their component payload inside that same item serialization.

## Tests

`FishDisplayPersistenceGameTests` covers:

1. a canonical display with an intentionally stale legacy `FishLength` mirror renders at canonical `finalLength`;
2. canonical species, size, trait axes, FishScore, seed, and natural percentile survive display placement and removal unchanged;
3. Tide display metadata remains present, confirming the migration does not replace Tide's model/display-data path;
4. a legacy display continues to use its Tide length and remains noncanonical after placement/removal, proving no migration-time specimen generation occurs.

## Validation

Implementation commit `2cd78305f4119b70441f27005ea307546ce056f0` is green in GitHub Actions run `33188581378`.

The exact Tide 2.1.1/Apex dependency workflow completed:

- reconstruction identifier validation;
- `./gradlew clean build --stacktrace`, including unit tests;
- `./gradlew runGametest --stacktrace`, including both display persistence GameTests;
- built-JAR artifact upload.

Stage 26 is complete. No later migration slice was started.

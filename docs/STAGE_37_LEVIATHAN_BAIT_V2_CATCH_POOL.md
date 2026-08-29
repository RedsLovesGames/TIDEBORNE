# Stage 37 - Leviathan Bait V2 catch-pool migration

Stage 37 migrates Leviathan Bait's fish-only catch-pool behavior onto the canonical Fishing System 2.0 modifier and species-selection pipeline. This stage changes catch-category eligibility only. The later Leviathan Fishing Luck, Trait Luck, Strength, and Tempo migration remains outside this slice.

## Canonical catch-pool behavior

When Leviathan Bait is active, `TideborneFishingGearModifiers.leviathanBaitCatchPool(...)` now returns a canonical `FishingGearModifiers` category restriction whose allowlist contains only the `fish` catch category.

That means:

- Tide fish remain eligible through the normal Tide fish path.
- non-fish catch categories such as crates, items, junk, or treasure are excluded by the active catch-pool restriction.
- when Leviathan Bait is inactive, the modifier is neutral and Tide's normal catch pool is left unchanged.

The catch-pool restriction is represented in the same composable server-side gear modifier model introduced earlier in Fishing System 2.0 rather than as a new Leviathan-specific selection system.

## Species selection and Tide eligibility

`LeviathanBaitFishing` remains the server-owned Tide catch hook. With the fish-only restriction active it delegates to Tide's existing `manager.getFishSelector().getResult(...)` path instead of directly constructing or summoning any fish.

Tide's fish selector is already bridged by `FishSelectorMixin` to `TideSpeciesSelectionBridge`, so Leviathan Bait uses the normal Fishing System 2.0 species selector. The existing Tide species profile and eligibility adapters continue to determine which Tide fish are actually valid for the current biome, dimension, medium, time, season, temperature, and other canonical Tide conditions before V2 weighting selects among eligible fish.

No special Leviathan fish is created, injected, or summoned by this stage.

## Legacy weighting removal

The unused legacy `LeviathanBaitRules.selectionWeight(...)` helper has been removed. Stage 37 does not restore or reintroduce any `selection_quality` weighting path.

Leviathan Bait's pre-existing configured fish-luck context adjustment remains in place for now and feeds the same normal Tide/V2 fish selector. It is not treated as legacy `selection_quality`. The broader Leviathan Fishing Luck, Trait Luck, Strength, and Tempo redesign remains reserved for its dedicated later migration stages.

## Server authority

Catch-pool selection remains server authoritative:

- `TideFishingHookMixin` delegates the server-side catch decision to `LeviathanBaitFishing`.
- the active bait state is read from the server-side hook/rod state.
- the fish-only branch uses Tide's server-owned fish selector, which is bridged to the canonical V2 species selector.
- no client payload can select or generate a specimen through this path.

## Regression coverage

`LeviathanBaitCatchPoolTest` covers:

- active Leviathan Bait produces an allowlist containing exactly `fish`.
- fish remains allowed while crate, item, junk, and treasure category identifiers are rejected.
- the active policy executes only the fish-selector branch.
- the normal catch selector is not invoked while the fish-only policy is active.
- inactive Leviathan Bait leaves the category restriction neutral.
- inactive behavior continues through Tide's normal catch selector.

These tests freeze the stage 37 boundary without testing or implementing later Leviathan stat redesign work.

## Validation

Implementation commit: `8b6d601c11fdbcb792ad2c88ab6494a907b83003`

GitHub Actions run `33245783074` completed successfully with:

- exact external dependency fetch
- reconstruction identifier validation
- clean Gradle build and unit tests
- Fabric GameTests
- built JAR artifact upload

## Stage boundary

Stage 37 completes only the Leviathan Bait fish-only catch-pool migration. Future Leviathan stages should build on this canonical category restriction and must not recreate a parallel catch selector, bypass Tide species eligibility, restore `selection_quality`, or directly create a special fish.

# Stage 35: Steel Leader canonical gear migration

## Scope

Stage 35 moves Steel Leader gameplay behavior into the canonical Fishing System 2.0 gear/modifier architecture introduced in Stage 33. It preserves the existing attachment representation, Angling Table compatibility, minigame tradeoff, and Apex shark catch-loss protection behavior.

This stage does not migrate Tentacle Line, Swift Line, Leviathan Bait, Shark Tooth Hook, rod behavior, or other compatibility gear.

## Preserved behavior

The existing configured Steel Leader behavior remains unchanged:

- catch-zone area multiplier: `0.90` by default;
- minigame speed multiplier: `1.05` by default;
- shark catch-loss prevention chance: `0.90` by default;
- prevention still uses the strict legacy comparison `roll < chance`;
- no Steel Leader means no protection roll is consumed;
- an attached Steel Leader still consumes one prevention roll when a shark-loss event occurs, including when the configured prevention chance is `0.0`;
- Steel Leader does not change Fishing Luck, Trait Luck, canonical Strength, canonical Tempo, species selection, traits, or specimen identity.

The existing Tentacle Line / Swift Line precedence in `FishingModifiers.modifyMinigame` is preserved. When one of those Myths line branches is active, the Steel Leader minigame penalty is not additionally applied, matching the pre-migration behavior.

## Canonical representation

`FishingGearEffects` defines reusable named canonical gear effects for:

- `catch_zone_area` multiplier;
- `minigame_speed` multiplier;
- `catch_loss_prevention_chance` additive chance;
- `catch_loss_protection_sources` additive source count used to preserve whether a protection RNG roll should occur.

`SteelLeaderGearModifiers` is the single runtime adapter from persisted Steel Leader state to `FishingGearModifiers`. It resolves both the current attached-component representation and the older custom-line representation through `SteelLeaderAttachment.hasOnHook`, then emits one canonical modifier set.

When Apex compatibility is disabled in the server configuration or no Steel Leader is present, the adapter returns `FishingGearModifiers.neutral()`.

## Runtime consumers

Before Stage 35, Steel Leader state was interpreted independently in two gameplay paths:

1. `FishingModifiers.modifyMinigame` directly checked `SteelLeaderAttachment.hasOnHook` and directly read the catch-zone/speed config fields.
2. the server `TideFishingHook` shark-loss retrieve injection independently checked `SteelLeaderAttachment.hasOnHook` and directly read the prevention-chance config field.

Stage 35 removes both direct gameplay checks.

The minigame path now obtains `FishingGearModifiers` from `SteelLeaderGearModifiers` and reads the canonical catch-zone and speed effects through `FishingGearEffects`.

The shark-loss path now obtains the same canonical gear representation and resolves prevention through `FishingGearEffects.preventsCatchLoss`. The surrounding shark-loss event remains server-authoritative: the server still determines whether a fish catch is eligible, calculates shark theft probability, consumes world RNG, invalidates stolen catches, spawns feedback, and sends the loss payload.

## Checks intentionally retained

`SteelLeaderAttachment` remains the compatibility/persistence adapter for detecting and storing the Steel Leader attachment. Angling Table code still uses Steel Leader identity checks to move the leader between its dedicated slot, the rod attachment component, and legacy custom-line representation.

Those checks are not duplicate gameplay behavior and are intentionally retained so existing rods/worlds remain compatible.

## Regression coverage

`SteelLeaderGearModifiersTest` covers:

- exact current default catch-zone, speed, and 90% loss-protection values;
- neutral ordinary catches without Steel Leader;
- no protection RNG consumption without a protection source;
- Apex compatibility disabled behavior;
- composition through the canonical Stage 33 named-modifier model;
- strict `roll < chance` shark-loss boundary behavior;
- exact protection-roll count, including zero configured prevention chance.

Full runtime validation also exercises server startup and the existing compatibility mixins through Fabric GameTests.

## Validation

Implementation commit `b2198af9109769eb9d8abe88d8453e4934618160` is green in GitHub Actions run `33243653492`.

The run completed successfully with:

- exact external dependency fetch;
- reconstruction identifier validation;
- `./gradlew clean build --stacktrace`, including JUnit tests;
- `./gradlew runGametest --stacktrace`;
- built-JAR artifact upload.

Stage 35 is complete. Steel Leader gameplay behavior now enters runtime through the canonical gear/modifier representation while its storage and Angling Table compatibility remain unchanged.

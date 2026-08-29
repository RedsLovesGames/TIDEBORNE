# Stage 54 - Fresh-world regression validation

Stage 54 validates a brand-new Fishing System 2.0 state after Stages 50 through 53 removed the legacy size, mutation, species/fight, and FishScore calculation paths.

## Regression scope

The focused empty-world GameTest gate proves:

- a fresh player persistence root starts with zero per-species Trait Momentum and writes/reads the current versioned `FishingV2TraitMomentum` format without requiring legacy NBT;
- a fresh fish stack starts with no migration state, receives a specimen directly from `SpecimenGenerator`, persists the current schema/generation, canonical base/final size, finalized trait axes, and canonical FishScore, and mirrors only the canonical final length into Tide item data;
- a brand-new fishing rod begins without Steel Leader state, the current Steel Leader attachment component can be added and removed without legacy custom-line state, and the canonical gear adapter resolves the frozen catch-zone, minigame-speed, and catch-loss-protection effects;
- Leviathan Bait remains the canonical fish-only modifier set with +15 Fishing Luck, +8 Trait Luck, Strength x1.15, and Tempo x1.15;
- the already-existing canonical fishing, species eligibility, trait-axis, Perfect Catch, Perfect Specimen, fight, Tide line, bucket, display, Angler's Satchel, Journal, and leaderboard/record tests remain in the same full build/GameTest gate, so Stage 54 does not duplicate stronger existing coverage.

## Regression found and fixed

The fresh rod test reproduced one production defect. `SteelLeaderAttachment` could fail to observe the current `STEEL_LEADER_ATTACHED` component when the attachment lived on a new `ItemStack`, because the compatibility reflection path was not the correct current stack-component access path.

Commit `b087f3e6a386d006afd672533e5c175156dff7db` fixes that regression by reading and writing `TideTraitsComponents.STEEL_LEADER_ATTACHED` directly for `ItemStack` values while preserving the existing reflection fallback for compatibility objects. The frozen Steel Leader balance values are unchanged.

## Change policy

Stage 54 is a regression pass, not a balance or redesign stage. No unrelated production behavior or constants were changed. The only production change is the Steel Leader defect reproduced by the fresh-state regression.

## Validation gate

The Stage 54 production-fix head is validated through the repository's existing Java 21 CI workflow:

- exact external dependency fetch and reconstruction validation;
- `./gradlew clean build --stacktrace`, including unit tests;
- Fabric GameTests without Apex Waters;
- Fabric GameTests with the exact Apex Waters 1.1.1 artifact;
- built-JAR artifact upload.

GitHub Actions run `33263857714` completed successfully for commit `b087f3e6a386d006afd672533e5c175156dff7db`.

Stage 54 regression coverage was introduced in commit `908db40bfd6897d863a07725d4f52afb9b186239`.

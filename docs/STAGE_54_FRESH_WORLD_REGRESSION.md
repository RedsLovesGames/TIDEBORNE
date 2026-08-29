# Stage 54 - Fresh-world regression validation

Stage 54 validates a brand-new Fishing System 2.0 state after Stages 50 through 53 removed the legacy size, mutation, species/fight, and FishScore calculation paths.

## Regression scope

The focused empty-world GameTest gate proves:

- a fresh player persistence root starts with zero per-species Trait Momentum and writes/reads the current versioned `FishingV2TraitMomentum` format without requiring legacy NBT;
- a fresh fish stack starts with no migration state, receives a specimen directly from `SpecimenGenerator`, persists the current schema/generation, canonical base/final size, finalized trait axes, and canonical FishScore, and mirrors only the canonical final length into Tide item data;
- a brand-new fishing rod begins without Steel Leader state, the current Steel Leader attachment component can be added and removed without legacy custom-line state, and the canonical gear adapter resolves the frozen catch-zone, minigame-speed, and catch-loss-protection effects;
- Leviathan Bait remains the canonical fish-only modifier set with +15 Fishing Luck, +8 Trait Luck, Strength x1.15, and Tempo x1.15;
- the already-existing canonical representation, Angler's Satchel, and fish-display GameTests remain in the same full GameTest run, so Stage 54 does not duplicate their stronger round-trip and migration coverage.

## Change policy

Stage 54 is a regression pass, not a balance or redesign stage. Production constants and runtime behavior are changed only if the fresh-state tests reproduce a real defect. Otherwise this stage adds regression coverage and documentation only.

## Validation gate

The Stage 54 commit is validated through the repository's existing Java 21 CI workflow:

- exact external dependency fetch and reconstruction validation;
- `./gradlew clean build --stacktrace`, including unit tests;
- Fabric GameTests without Apex Waters;
- Fabric GameTests with the exact Apex Waters 1.1.1 artifact;
- built-JAR artifact upload.

Exact CI run and job IDs are recorded in the stage completion report after the commit is validated.

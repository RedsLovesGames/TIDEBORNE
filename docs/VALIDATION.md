# Tideborne validation gates

Status: current validation policy for the maintained `dev` branch.

The goal is to prove the changed behavior with the cheapest appropriate gate, then rely on milestone and release validation for broad regression coverage.

## Active workflow

`.github/workflows/build.yml` is the single active GitHub Actions build and release workflow.

### Normal `dev`, `main`, reconstruction-branch, and pull-request validation

When relevant source/build/workflow paths change, the normal workflow performs:

1. Java 21 setup
2. exact external dependency fetch/checksum validation
3. repository structure and release-metadata validation
4. semantic version resolution
5. `./gradlew clean build`
6. core Fabric GameTests with no optional compatibility mods
7. production release-artifact validation
8. unit/GameTest count reporting
9. CI artifact upload

A normal `dev` push is CI-only. It does not publish or replace a GitHub Release.

### Manual-dispatch and tagged validation

Manual workflow dispatches and semantic-version tag builds additionally run:

- GameTests with Apex Waters only
- GameTests with Myths of the Sea only
- GameTests with Apex Waters and Myths of the Sea together
- dedicated-server smoke validation

These expensive compatibility/runtime legs are intentionally not part of every ordinary `dev` push.

### Release publication

Public release publication occurs only for an explicit semantic-version tag handled by the workflow.

The tag must match `gradle.properties` `mod_version` exactly. Publication validates the tagged commit, refuses to replace an existing release, uploads the versioned JAR, and records the commit and artifact digest.

Normal branch pushes must never republish or overwrite an existing public release.

## Local validation routing

Do not run every validation layer after every change.

### Documentation-only changes

No Gradle validation is required unless the documentation change also modifies generated/configured behavior.

Check only that references, versions, paths, and instructions are internally consistent.

### Pure formulas or data tables

Run focused unit tests for the affected calculation or data contract.

Examples:

- gear composition math
- probability curves
- FishScore calculations
- progression/balance tables represented in code

### Multiple pure Java changes

Run:

```bash
./gradlew test
```

Use this when Minecraft runtime behavior is not involved.

### Normal implementation changes

Run:

```bash
./gradlew build
```

Gradle `build` already compiles the project and runs the normal unit-test suite. Do not automatically add a separate compile command and a separate full test command unless isolating a failure requires it.

### Runtime integration changes

For changes involving any of the following, run the relevant focused GameTests plus `./gradlew build`:

- Tide-targeting mixins
- vanilla gameplay mixins
- networking or payload registration
- persistence or migration
- server lifecycle
- canonical catch lifecycle
- Satchel catch interception
- Team Journal catch/record integration
- optional-mod runtime boundaries

Use the smallest relevant GameTest set during iteration. Let normal CI provide the broad core GameTest pass at the milestone commit.

### Client presentation changes

Run a normal build, then verify visual behavior manually in the Minecraft client when correctness depends on appearance or interaction.

Examples:

- screen layout
- clipping
- tooltip formatting
- texture rendering
- animation
- menu usability
- subjective fishing/fight feel

Do not replace simple human visual inspection with repeated expensive automated client launches unless a reproducible runtime regression specifically requires automation.

## Milestone validation

Push a coherent implementation milestone to `dev` and use the normal CI result as the broad regression gate.

A milestone should normally have:

- passing `clean build`
- passing unit tests
- passing core no-optional-mod GameTests
- valid production JAR
- valid repository metadata
- no unintended persisted-ID or payload changes

## Release validation

Before a public version tag, verify all release-sensitive behavior.

Required release confidence includes:

- normal build/unit tests
- core GameTests
- Apex-only GameTests
- Myths-only GameTests
- Apex + Myths GameTests
- dedicated-server smoke
- production artifact validation
- exact semantic version/tag match
- exact dependency/checksum validation
- clean intended repository state
- current `CURRENT_STATE.md` and `TODO.md`

When the release candidate changes Tide mixins, networking, persistence, or multiplayer/server authority, perform the appropriate manual/runtime checks in addition to CI.

## Exact local Tide dependency

For exact local Tide 2.1.1 validation, place:

```text
dev/libs/tide-fabric-1.21.1-2.1.1.jar
```

Then run the needed Gradle task. The repository also provides:

```bash
./gradlew verifyExactDependencies
```

CI fetches and verifies the required exact external artifacts automatically.

## What automated tests should prove

Automated coverage should focus on deterministic and regression-prone contracts.

### Canonical specimen domain

Examples include:

- deterministic trait RNG splitting
- one natural percentile/base-size sample per specimen
- Body Type selection and size transformation
- Condition/Pigmentation/Quality independence
- Trait Luck probability handling
- canonical FishScore behavior
- specimen persistence and transfer round trips
- migration without canonical rerolls

### Fishing gear

Examples include:

- deterministic composition independent of input order
- exact identity/slot resolution
- stacking semantics
- effect clamps and restrictions
- fight/minigame projections
- catch-loss behavior
- no direct canonical percentile/FishScore/trait authoring by gear

### Satchel

Examples include:

- storage/capacity rules
- XP/upgrade behavior
- insert/extract/protection/sorting rules
- canonical specimen preservation
- Auto Stow fallback behavior
- persistence round trips

### Team Journal and records

Examples include:

- team merge behavior
- canonical record comparison
- Team Top Fish ordering
- record-holder persistence
- recovery/migration behavior
- no independent FishScore calculation

### Compatibility and server authority

Examples include:

- Leviathan fish-only selector behavior
- line/hook/leader modifier integration
- optional-mod absence safety
- server-owned specimen generation
- server-owned Momentum
- client inability to author canonical catch state

## Mixins

Every new or materially changed mixin should have a clear target/purpose contract.

For Tide-targeting mixins, consult `docs/TIDE_MIXIN_INVENTORY.md` and verify the relevant runtime path when target signatures or lifecycle assumptions change.

A successful Java compile is not sufficient proof for a changed mixin target.

## Persistence fixtures and historical compatibility

The repository retains reconstruction and migration evidence from Tideborne 1.3.57.

Use that material when changing compatibility-sensitive storage or migration code. Do not make every unrelated feature task reread or rerun reconstruction-era validation.

A refactor that touches persisted data is rejected if it causes unintended loss, rerolls canonical specimen identity, or breaks required legacy migration reads.

## Validation summary rule for coding agents

At the end of a task, report only the validation actually run and its result.

Do not claim client, dedicated-server, multiplayer, optional-mod, or release validation if it was not performed.

Do not spend time running broader gates solely to make a task summary look more complete.
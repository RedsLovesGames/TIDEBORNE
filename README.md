# Tideborne

Tideborne is a Fabric 1.21.1 addon for Tide 2. Fishing System 2.0 is the current server-authoritative fishing architecture and covers specimen generation, traits, fights, FishScore, persistence, migration, gear, Journal, Satchel, records, networking, and compatibility paths.

## Current versions

- current published release: `2.0.0`
- current `dev` version: `2.0.1`
- Minecraft: `1.21.1`
- Java: `21`
- Tide runtime target: `2.1.1`
- optional Apex Waters compatibility: `1.1.1`
- optional Myths of the Sea compatibility: `1.3.0`

The preserved Tideborne 1.3.57 artifact remains the old-world compatibility and reconstruction baseline. Its provenance and frozen hashes are documented in `docs/RECONSTRUCTION.md`.

## Development branch and releases

Active development happens on `dev`.

`main` must not be modified, merged, rebased, or retargeted unless explicitly authorized.

Normal `dev` pushes are CI-only. Public releases are created only from explicit semantic-version tags matching `gradle.properties`. Existing releases are not silently replaced by the workflow.

## Build and validation

Typical local validation:

```bash
./gradlew build
```

Core GameTests when runtime integration requires them:

```bash
./gradlew runGametest
```

For exact local Tide API validation, place the authoritative Tide JAR at:

```text
dev/libs/tide-fabric-1.21.1-2.1.1.jar
```

Then use:

```bash
./gradlew verifyExactDependencies
```

The active GitHub Actions workflow runs dependency/repository validation, a clean build with unit tests, core no-optional-mod GameTests, production-JAR validation, and CI artifact upload for normal development runs.

Optional Apex/Myths compatibility matrices and dedicated-server smoke are reserved for manual-dispatch and tagged release validation rather than every `dev` push.

See `docs/VALIDATION.md` for the current validation routing policy.

## Repository guide

Start with `docs/INDEX.md` instead of reading every historical Markdown document.

- `AGENTS.md`: permanent development, ownership, compatibility, and validation rules
- `docs/CURRENT_STATE.md`: authoritative verified implementation state
- `docs/TODO.md`: authoritative unfinished work
- `docs/ARCHITECTURE.md`: current Fishing System 2.0 architecture and ownership
- `docs/GEAR_PROGRESSION_AUDIT.md`: current post-2.0 gear-path inventory
- `docs/FISHING_SYSTEM_2_SPEC.md`: implemented Fishing System 2.0 behavior contract
- `docs/TIDE_MIXIN_INVENTORY.md`: current Tide mixin ownership and fragility reference
- `docs/RECONSTRUCTION.md`: historical 1.3.57 reconstruction provenance and recovery material

Historical stage reports, migration audits, and old balance reports remain useful evidence, but they do not override the current-state and architecture documents.
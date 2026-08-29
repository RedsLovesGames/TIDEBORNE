# Tideborne

Tideborne is a Fabric 1.21.1 addon for Tide 2. Version 2.0.0 contains the server-authoritative Fishing System 2.0 specimen, trait, fight, score, persistence, migration, gear, Journal, Satchel, and compatibility runtime.

## Release target

- Tideborne: `2.0.0`
- Minecraft: `1.21.1`
- Java: `21`
- Tide runtime: `2.1.1`
- optional Apex Waters compatibility: `1.1.1`
- optional Myths of the Sea compatibility: `1.3.0`

The preserved Tideborne 1.3.57 artifact remains the old-world compatibility and reconstruction baseline. Its provenance and frozen hashes are documented in `docs/RECONSTRUCTION.md`.

## Build and validation

```bash
./gradlew clean build
./gradlew runGametest
./scripts/validate_repository.sh
./scripts/dedicated_server_smoke.sh
./scripts/validate_release_artifact.sh build/libs/tideborne-2.0.0.jar
```

For exact local validation against Tide 2.1.1, place the authoritative Tide JAR at:

```text
dev/libs/tide-fabric-1.21.1-2.1.1.jar
```

CI fetches and verifies the exact Tide and optional compatibility artifacts before running the full four-leg GameTest matrix.

## Repository guide

- `AGENTS.md` contains development and compatibility rules.
- `docs/CURRENT_STATE.md` records the verified implementation and release state.
- `docs/FISHING_SYSTEM_2_SPEC.md` freezes Fishing System 2.0 behavior.
- `docs/RECONSTRUCTION.md` records the 1.3.57 source provenance.
- `docs/VALIDATION.md` defines the build and runtime validation gates.

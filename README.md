# Tideborne

Tideborne is a Fabric 1.21.1 addon built around Tide 2. This repository is the canonical development workspace for the mod.

## Current baseline

The source-recovery branch is rebuilding the project from the authoritative **Tideborne 1.3.57** release JAR before any fishing-system redesign work begins. The rule for this phase is behavioral preservation first, structural cleanup second, new mechanics later.

Authoritative reconstruction artifact:

- Tideborne: `1.3.57`
- SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- Minecraft: `1.21.1`
- Java: `21`
- Tide runtime target: `2.1.1`

## Build

```bash
./gradlew build
```

For exact local development against Tide 2.1.1, put the supplied Tide JAR at:

```text
dev/libs/tide-fabric-1.21.1-2.1.1.jar
```

The Gradle build prefers that exact local JAR when present. CI can fall back to the API-compatible Tide 2.1 Fabric Maven artifact while source reconstruction is being completed.

## Repository guide

Start with:

- `AGENTS.md` for human/AI development rules
- `docs/CURRENT_STATE.md` for what is done and what is next
- `docs/ARCHITECTURE.md` for package/module ownership
- `docs/RECONSTRUCTION.md` for source provenance and recovery rules
- `docs/VALIDATION.md` for the required build/test gates

## Development rule

Do not mix the fishing-system redesign into source reconstruction. First obtain a clean, readable, reproducibly buildable 1.3.57 baseline. Then refactor behind tests. Only after that should the new fishing/specimen/trait system be implemented.

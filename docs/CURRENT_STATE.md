# Current development state

Updated: 2026-08-27

## Baseline

- target release behavior: Tideborne 1.3.57
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- working branch: `reconstruct-1.3.57`

## Verified from the authoritative JAR

- version and Fabric metadata
- entrypoint classes
- mixin config names
- declared required/suggested dependencies
- package/class inventory
- authentic resource inventory
- Tideborne JAR SHA-256
- supplied Tide 2.1.1 JAR SHA-256/SHA-1

## Repository bootstrap status

Completed:

- Gradle/Loom project metadata
- pinned Minecraft/Fabric/Yarn dependency versions
- exact-local Tide 2.1.1 override path
- CI-compatible Tide compile fallback
- human/AI instructions
- recovered package architecture map
- reconstruction provenance rules
- validation requirements
- local dependency isolation rules

In progress:

- reconstructing `src/main/java` from 1.3.57 bytecode
- extracting `src/main/resources` from the release JAR
- producing Gradle wrapper
- compiling recovered source without semantic changes

Not started on this branch:

- Fishing System 2.0
- new mutation rates
- new fishing luck model
- new specimen model
- new FishScore formula
- minigame rebalance beyond characterization

## Highest-priority reconstruction tasks

1. Recover all top-level Java sources and resources.
2. Make `./gradlew build` succeed with exact Tide 2.1.1 locally.
3. Make client and dedicated-server smoke tests start cleanly.
4. Add characterization tests for trait roll, percentile, size transforms, FishScore, Satchel capacity/XP, team records, Leviathan Bait, and minigame modifiers.
5. Move algorithms out of mixins/screens into named services without changing results.
6. Isolate optional Apex/Myths integrations from core class loading.
7. Add migration fixtures from existing 1.3.57 saves/items/config.
8. Merge the clean baseline to `main` only after validation.

## Known structural debt recovered from the JAR

These are refactor targets, not defects by themselves:

- multiple legacy module namespaces inside one final mod JAR
- several static/global service-style classes
- gameplay logic mixed into integration/mixin paths
- large Satchel and team-journal screen classes
- persistence, networking, and domain logic coupled in some services
- separate legacy config systems coordinated by a unified facade
- optional Apex integration that references external classes directly
- repeated Tide hook/player-data integration across modules

## Stop condition for the reconstruction phase

The phase is complete only when:

- source and resources are committed
- clean checkout builds with documented dependency setup
- exact Tide 2.1.1 build passes locally
- CI build passes
- GameTests/characterization tests pass
- no known 1.3.57 feature is intentionally removed
- persistence/network identifiers are inventoried
- the major package boundaries are understandable from code and docs

Only then should the new fishing system branch from this baseline.

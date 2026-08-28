# Current development state

Updated: 2026-08-28

## Baseline and branch

- compatibility baseline: Tideborne 1.3.57
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- `main` must remain untouched
- authoritative redesign contract: `docs/FISHING_SYSTEM_2_SPEC.md`

## Fishing System 2.0 run boundary

This run is limited to specification execution steps 1 through 4:

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. species selection and Fishing Luck
3. `SpecimenData`, exact percentile, and size math
4. fight normalization and size fight scaling

Do not begin Body Type generation, independent traits, Trait Luck, Momentum, Perfect Catch changes, Perfect Specimen, FishScore V2, migration, UI integration, or progression work during this run.

## Current implementation status

| Slice | Status | Verification |
|---|---|---|
| Authoritative Fishing System 2.0 specification | Complete locally | Design persisted in `docs/FISHING_SYSTEM_2_SPEC.md` |
| Reconstructed 1.3.57 compatibility baseline | Blocked in repository | Current `dev` tree contains only 91 of 272 class files in an incomplete reconstruction archive, no reconstructed `src/`, no tests, no resources payload, and no Gradle wrapper |
| Step 1 domain models and canonical rarity | Not started | Waiting for complete baseline inspection |
| Step 2 species selection and Fishing Luck | Not started | Waiting for complete baseline inspection |
| Step 3 specimen data and exact size math | Not started | Waiting for complete baseline inspection |
| Step 4 fight normalization and size scaling | Not started | Waiting for complete baseline inspection |
| Full `./gradlew build` | Blocked | `gradlew` and reconstructed source are absent from the current branch |

## Repository evidence

At the start of this run, `dev`, `main`, and `reconstruct-1.3.57` all pointed to commit `41e53b052660e04e546b07b305c5047b3f646675` with tree `946bae9433a6f7a73e4381ac46b32dd47e463b66`.

The committed class reconstruction payload decodes to a corrupt and incomplete archive:

- expected compressed archive SHA-256: `e7265ad9a6df36f79be098d47b5fcda570532e5488671e58a0047b6ad70f4ac6`
- committed payload SHA-256: `5e05d2065e4c41b72c5830cd17f70abbfc343b5358953ec5e476f264abb2825f`
- expected classes: 272
- readable classes before archive truncation: 91
- missing: remaining class chunks, resource chunks, reconstruction completion marker, reconstructed Java source, reconstructed resources, tests, and Gradle wrapper

Do not invent legacy behavior or claim a compatibility build from this partial payload.

## Verified from the authoritative 1.3.57 metadata

- version and Fabric metadata
- entrypoint classes
- mixin config names
- declared required and suggested dependencies
- package architecture
- Tideborne JAR and content-tree hashes
- Tide 2.1.1 target hashes
- persistence, networking, and ID compatibility requirements documented in `AGENTS.md` and `docs/RECONSTRUCTION.md`

## Exact next action

Complete the 1.3.57 baseline transfer onto `dev` by supplying either:

1. the full reconstructed `src/main/java`, `src/main/resources`, tests, and Gradle wrapper, or
2. the authoritative `Tideborne-1.3.57-perfect-catch-trait-luck.jar` plus the missing reconstruction payload needed to reproduce them.

Then verify the content-tree hash and run the baseline build before implementing step 1. Once the baseline is green, inspect only legacy rarity, selection, percentile, size, and fight code and continue in the order recorded above.

## Later work

The complete prioritized backlog is in `docs/TODO.md`. Do not advance beyond step 4 until this run's tests and full build are green.

# Current development state

Updated: 2026-08-28

## Baseline and branch

- compatibility baseline target: Tideborne 1.3.57
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- `main` remains untouched at `41e53b052660e04e546b07b305c5047b3f646675`
- authoritative redesign contract: `docs/FISHING_SYSTEM_2_SPEC.md`

## Fishing System 2.0 run boundary

Specification execution steps 1 through 4 are complete in the pure V2 domain layer:

1. `FishingContext`, `SpeciesProfile`, and canonical rarity
2. species selection and Fishing Luck
3. `SpecimenData`, direct percentile, and size math
4. fight normalization and size fight scaling

They are not yet wired into the reconstructed Tideborne runtime. Body Type generation, independent traits, Trait Luck, Momentum, Perfect Catch changes, Perfect Specimen, FishScore V2, migration, UI integration, and progression remain gated on the authoritative 1.3.57 baseline.

## Verified implementation status

| Slice | Status | Verification |
|---|---|---|
| Authoritative Fishing System 2.0 specification | Complete | Persisted in `docs/FISHING_SYSTEM_2_SPEC.md` |
| Step 1 domain models and canonical rarity | Domain layer complete | Immutable `FishingContext`, canonical 1 through 5 star rarity, compatibility-ready eligibility/environment boundary, and `SpeciesProfile` |
| Step 2 species selection and Fishing Luck | Domain layer complete | Rarity-aware weighted selection implements `W' = W * (1 + C_R * sqrt(max(L, 0)))`; selection always returns one eligible fish and does not alter overall fish-catch probability |
| Step 3 specimen data and exact size math | Domain layer complete | Immutable canonical `SpecimenData`, deterministic seeded base generation, and direct lognormal CDF and quantile calculations replace sampled lookup behavior in the new layer |
| Step 4 fight normalization and size scaling | Domain layer complete | Formula-based Tempo normalization, bounded Strength, catch-zone calculation, external outlier clamps, and percentile Strength and Tempo multipliers |
| Deterministic unit tests | Green | 17 of 17 tests passed locally on Java 21 |
| Full repository build | Green for maintained source | GitHub Actions run `33138932023` executes `./gradlew clean build --stacktrace` successfully on Java 21 and uploads JAR artifacts |
| Gradle wrapper | Complete | Pinned Gradle 8.12 wrapper with distribution SHA-256 verification |
| Reconstructed 1.3.57 legacy integration | Blocked by unavailable authoritative input | The staged reconstruction archive is corrupt and incomplete, authoritative resources are absent, and the exact 1.3.57 JAR/content tree is not present in the repository or retained artifact sources checked below |

## Implemented files

The pure Fishing System 2.0 domain layer is under:

```text
src/main/java/com/redslovesgames/tideborne/fishing/v2/
```

It currently contains:

- `CanonicalRarity`
- `FishingContext`
- `FishingEnvironment`
- `SpeciesEligibility`
- `SpeciesProfile`
- `SpeciesSelectionService`
- `SizeDistribution`
- `LogNormalSizeDistribution`
- `SpecimenData`
- `SpecimenGenerator`
- `FightProfile`
- `FightProfileService`

Tests are under the matching `src/test/java` package.

## Commits on `dev`

- `e080d8fa9def6b52c80b0899fa72c144f8fba6be`: persist specification and baseline blocker state
- `dbbcd382f33fa7ed0dd428bd20e3162036e2127f`: implement Fishing System 2.0 domain steps 1 through 4
- `babba1747c71d9f08274b085b5e0b599daa7f6d7`: restore the Gradle wrapper and validate `dev` builds
- `248f1dca2d560491dc862f8098f2861d9753b965`: record green Fishing System 2.0 steps 1 through 4
- `da023a6a878486c38e4e5de608c82911560fe144`: record verified 1.3.57 reconstruction blocker

## Reconstruction blocker evidence

Phase 0 was rechecked against the current repository and Git history on 2026-08-28. The staged 1.3.57 class payload cannot satisfy the frozen reconstruction contract:

- expected authoritative release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- expected canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- expected compressed class archive SHA-256: `e7265ad9a6df36f79be098d47b5fcda570532e5488671e58a0047b6ad70f4ac6`
- current staged class archive SHA-256: `5e05d2065e4c41b72c5830cd17f70abbfc343b5358953ec5e476f264abb2825f`
- current staged class archive size: 256,503 bytes
- `xz --test` result: corrupt compressed data
- extraction result: 91 readable `.class` files before corruption, versus 272 required
- historical payload scan: no committed staged class payload reproduces the expected class archive SHA-256
- the historical `Stage remaining Tideborne reconstruction class payload` commit contains a literal `[... truncated for display ...]` marker at character 10,000, so the missing bytes are not recoverable from Git history
- `reconstruction/input` contains no authoritative `resources.tar.xz.b64.part-*` payload
- therefore the required 442-file canonical content tree cannot be reconstructed or hash-verified from the repository as it stands

### Expanded retained-artifact recovery sweep

The same run exhausted the additional retained sources available to this workspace:

- File Library exact-name, version, reconstructed-project, and recent-upload searches found release notes and registry metadata for `Tideborne-1.3.57-perfect-catch-trait-luck.jar`, but not the JAR bytes, a content-identical extracted tree, or a complete reconstructed project archive.
- Connected Google Drive exact-name search found no authoritative 1.3.57 JAR.
- The TIDEBORNE repository has no release asset containing the authoritative binary, and the retained Actions/build artifacts checked do not contain it.
- The Fish Wiki repository `RedsLovesGames/Tide-2-Addons` contains runtime provenance proving that a real Minecraft environment loaded `Tideborne-1.3.57-perfect-catch-trait-luck.jar` with the frozen SHA-256 on 2026-08-27.
- That Fish Wiki runtime export was traced to historical commit `a9dea553f790a7086a36428c07917b9c4bf7636f` and inspected on an isolated reconstruction branch runner.
- The historical runtime export ZIP is 4,377,605 bytes, SHA-256 `b667605d30cdcf7ce15456969ae2e35aa912981285d17206989c11b1b616b971`, and contains 1,056 entries.
- The export contains 0 `.jar` files and 0 `.class` files. No entry matches the authoritative Tideborne JAR SHA-256. It contains rendered PNGs and runtime metadata, not reconstructable Tideborne code/resources.
- Therefore the cross-repository runtime evidence proves the exact binary existed on the machine that generated the export, but it does not recover the missing binary or canonical content tree.

The expected JAR SHA and canonical tree SHA remain frozen verification anchors, not verified artifacts in this run. Phase 0 is now blocked by genuinely unavailable authoritative bytes in the currently accessible sources.

The green maintained-source build verifies the existing pure domain layer only. It does not prove runtime integration with legacy Tideborne code, Tide mixins, Satchel, Journal, records, history, teams, networking, or minigame callers.

## Exact next action

Restore one authoritative reconstruction input that can reproduce the frozen 1.3.57 content tree:

1. the exact Tideborne 1.3.57 release JAR with SHA-256 `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`, or
2. a content-identical 442-file tree, or
3. corrected complete class and resource reconstruction payloads.

Then verify 442 actual files, 272 `.class` files, and canonical content-tree SHA-256 `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`. Reconstruct complete `src/main/java`, `src/main/resources`, baseline tests, and persistence fixtures and run the untouched baseline `./gradlew clean build --stacktrace`.

Only after that baseline is green should legacy rarity, selection, percentile, size, and fight callers be adapted to `com.redslovesgames.tideborne.fishing.v2`, with integration and serialization tests added and superseded calculations removed after migration. Step 5 remains blocked until steps 1 through 4 are actually integrated and green.

## Later work

The prioritized backlog is in `docs/TODO.md`. The next design slice after the integration gate is step 5, Body Type.

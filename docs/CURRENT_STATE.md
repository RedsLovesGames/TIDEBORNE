# Current development state

Updated: 2026-09-04

## Baseline and branch

- compatibility baseline: Tideborne 1.3.57
- current published release: Tideborne 2.0.0
- current development version: Tideborne 2.0.1
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- reconstruction branch: `reconstruct-1.3.57`
- `main` remains untouched unless explicitly authorized
- authoritative Fishing System 2.0 contract: `docs/FISHING_SYSTEM_2_SPEC.md`

Frozen reconstruction anchors:

- Tideborne 1.3.57 release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- reconstructed canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- Tide 2.1.1 Fabric 1.21.1 SHA-256: `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Apex Waters 1.1.1 Fabric 1.21.1 SHA-256: `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`

Historical stage-by-stage implementation details remain available in the dedicated stage documents and Git history. This file records the current authoritative state.

## Exact published 2.0.0 build

The public 2.0.0 release is now treated as frozen legacy release state.

- GitHub release/tag: `TIDEBORN-2.0.0`
- exact release commit: `6f1d2d0c67f38c5d5924f3c3572babe0fe6d2feb`
- artifact: `tideborne-2.0.0.jar`
- artifact size: `1,087,782` bytes
- artifact SHA-256: `1f69f32fbb1bb85675dffddb5b585f33ab8f31a0495d31c3177a3f0d26190637`

That version, commit, filename, size, and digest tuple is the authoritative identity of the published 2.0.0 build. Future `dev` commits must not retarget the release or replace its JAR.

## Release versioning policy

Release publication was corrected on `dev` beginning with the 2.0.1 development line.

`.github/workflows/build.yml` now follows these rules:

- normal pushes to `dev`, `main`, and `reconstruct-1.3.57` are CI-only;
- pull requests are CI-only;
- manual workflow dispatch is CI-only;
- public release publication is reachable only from an explicit numeric semantic-version tag such as `2.0.1`, `2.0.2`, or `2.1.0`;
- `gradle.properties` must contain an exact `MAJOR.MINOR.PATCH` semantic version;
- on a release tag, the tag text must exactly equal `mod_version`;
- the checked-out commit must exactly equal the GitHub tag-event commit;
- the release JAR is built and validated from that tagged commit;
- the artifact filename is derived from the semantic version rather than hardcoded to 2.0.0;
- a release is created only if no release with that tag already exists;
- the workflow contains no release upload `--clobber` path and never edits or retargets an existing release;
- release metadata records the exact commit SHA and JAR SHA-256.

The old `TIDEBORN-2.0.0` name is retained only as the legacy 2.0.0 release identifier. New releases use the exact semantic version as the Git tag, for example `2.0.1`.

GitHub's repository-level Immutable Releases feature is separate from the workflow. The workflow now refuses silent replacement on its own. Enabling GitHub Immutable Releases additionally prevents manual asset replacement and tag movement at the server level.

### Streamlined CI validation

The streamlined `dev` workflow introduced at `32f29b24f5cb777baf9f4228e3dd8e7c6e0c5cc5` was revalidated after making `scripts/validate_release_artifact.sh` semantic-version aware.

Validated CI head: `3308ab5610ff121f13d12a03532c60c41c1f831e`.
GitHub Actions run: `33831374450`.

That normal `dev` push passed:

- exact dependency fetch and checksum validation;
- repository and semantic-version validation for Tideborne 2.0.1;
- clean Gradle build and unit tests;
- the 58-test core Fabric GameTest suite;
- version-aware production JAR validation for `tideborne-2.0.1.jar`;
- final validation-count reporting;
- CI artifact upload.

The optional-mod compatibility matrices and dedicated-server smoke are intentionally skipped on ordinary `dev` pushes and remain available for manual dispatch and tagged release validation. The `publish-release` job was skipped, confirming that a normal `dev` push is CI-only.

After this validation, the public `TIDEBORN-2.0.0` release still targeted `6f1d2d0c67f38c5d5924f3c3572babe0fe6d2feb` and still exposed the same `tideborne-2.0.0.jar` artifact with SHA-256 `1f69f32fbb1bb85675dffddb5b585f33ab8f31a0495d31c3177a3f0d26190637`.

## Fishing System 2.0

Fishing System 2.0 through Stage 64 is complete on `dev`, including the later Fishing Journal and Team Top 15 presentation fixes.

Canonical runtime authority includes:

- server-owned Tide species selection using canonical Fishing Luck weighting and Tide eligibility restrictions;
- one canonical natural specimen percentile/base-size sample per catch;
- independent deterministic Body Type, Condition, Pigmentation, and Specimen Quality axes;
- canonical final physical size and size-adjusted final percentile without a second specimen sample;
- canonical Perfect Catch and Perfect Specimen behavior;
- server-owned per-player, per-species Trait Momentum;
- canonical FishScore V2 as the production score source;
- canonical Strength, Tempo, line, Steel Leader, rod, hook, bait, and Leviathan Bait behavior;
- canonical ItemStack, entity, bucket, display, Satchel, Journal, record, leaderboard, and network persistence/projection paths;
- deterministic one-way migration for recoverable Tideborne 1.3.57 fish and saved-data representations;
- guarded legacy compatibility paths that cannot reroll or overwrite current canonical V2 state;
- canonical Best Specimen per species and derived Team Top 15 records;
- replay-safe record recovery that does not replay live-catch side effects;
- exact namespaced canonical fishing-gear identity;
- dedicated Tideborne creative tab and optional-mod visibility matrix.

### Canonical internal fishing API

The first post-2.0 architecture goal is implemented on `dev` as `com.redslovesgames.tideborne.api.TideborneFishingApi`.

Initial implementation commit: `7631fb1f3493b709a2bc2e96d01d1c2dd7b3e910`.
Initial GitHub Actions run: `33861656348`.

That facade provides the stable internal read/query boundary for:

- canonical specimen reads from fish stacks and transfer/record NBT;
- stored canonical FishScore and raw FishScore reads;
- stable fishing-gear modifier queries;
- Tide species-profile lookup and stable profile enumeration;
- canonical record comparison, replacement, specimen identity, highest team score, and Team Top Fish reads.

The API returns the existing canonical `SpecimenData` and `SpeciesProfile` domain records rather than creating another representation. Covered feature code should prefer this facade over direct imports of canonical persistence, species-adapter, gear-registry, or record-index implementation classes.

The facade is intentionally read/query-only. FishScore calculation remains owned by `FishScoreV2Service`, and fishing-gear registry profiles remain owned by `FishingGearRegistry`; neither score calculation nor `FishingGearRegistry.GearProfile` is exported through the facade. This keeps the API from becoming a second authoring path or leaking implementation registry types.

The initial implementation and focused unit tests passed the normal streamlined `dev` CI workflow. Additional focused coverage validates transfer payload reads and canonical Team Top Fish ordering. The API boundary and ownership rules are documented in `docs/TIDEBORNE_INTERNAL_API.md`.

### Canonical specimen presentation

The second post-2.0 architecture goal is complete on `dev` as the shared read-only presentation contract `com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation`.

The presentation contract owns:

- the stable canonical trait axis order: Body Type, Condition, Pigmentation, Quality;
- full and short trait labels;
- semantic trait colors;
- canonical FishScore text;
- canonical percentile text;
- canonical length text;
- canonical rarity-star text;
- immutable `TraitDisplay` and complete specimen `View` projections over the existing `SpecimenData` record;
- compatibility-only projection of historical synchronized trait strings without creating a second specimen model.

The shared presentation class remains common-side safe and contains no client-only rendering classes. `FishingUiFormat` is now restricted to genuinely client-specific formatting, currently catch timestamps, and no longer exposes parallel specimen FishScore, length, percentile, trait, or unavailable-value formatting.

Covered consumers routed through the shared presentation contract now include:

- Fishing Journal Best Specimen trait rendering;
- Team Top 15 and Top Fish list/detail presentation;
- canonical team record projection and compact Team Records event rows;
- Satchel canonical specimen projection, specimen detail panel, record badges, personal-record length summaries, and record FishScore labels;
- the normal fish ItemStack tooltip, which reads a complete current canonical specimen through `TideborneFishingApi` instead of reconstructing specimen fields from components;
- Tide fish-profile possible-size and recorded-FishScore overlay formatting;
- operator `/tideborne fishing inspect` specimen output, including final percentile, final length, rarity, trait names/values, and FishScore.

Focused source-safety and unit tests lock the tooltip, Top Fish, Satchel, team-record, fish-profile, and operator inspection paths to the canonical presentation layer. As a final sweep guard, removing specimen-formatting methods from `FishingUiFormat` forced compilation to identify the last two residual consumers, which were then migrated directly.

Final Stage 1 validation head: `7e2498542cd21215c6c931cca208ff8b4337963b`.
GitHub Actions run: `33892874198`.

That normal `dev` run completed successfully, including the clean Gradle build/unit tests, normal no-optional-mod GameTests, production artifact validation, validation-count reporting, and CI artifact upload. Optional-mod matrices and dedicated-server smoke remained skipped under the intentionally streamlined normal-push policy.

No meaningful independent specimen-presentation implementation is intentionally retained. Layout-specific positioning, clipping, localized screen text, and timestamp formatting remain client/UI responsibilities, while specimen values and semantics come from the canonical presentation layer.

### Real Tide balance simulator

The authoritative tuning simulator now uses the live Tide 2.1.1 species catalog instead of the synthetic equal-rarity test pool.

Implementation head: `918b5fce07036b888cebb505ffec4faebeb1f0af`.
GitHub Actions run: `33832555397`.
Authoritative report: `docs/FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`.

The runtime-backed harness:

- loads all 106 Tide 2.1.1 fish from `TideData.FISH`;
- runs each fish through Tide's real `shouldKeep(context)` eligibility and context modifiers;
- preserves real rarity, encounter weight, size distribution, strength, and speed metadata through `TideSpeciesProfileAdapter`;
- samples nine representative fishing contexts covering river, swamp/rain/night, warm ocean, deep ocean/night, frozen ocean, lush cave, dripstone cave, Nether lava, and End void fishing;
- exposes 61 unique Tide species across that representative matrix;
- measures rarity distribution, FishScore distribution, notable-trait frequency, Perfect Specimen frequency, Fishing Luck value, Trait Luck value, and catch-count progression;
- uses controlled same-seed sensitivity runs to isolate Fishing Luck from Trait Luck.

The validation run passed 282 unit tests and all 59 required core GameTests, including the real Tide balance projection. Production JAR validation and CI artifact upload also passed. The release publication job was skipped because this was a normal `dev` push.

The historical `docs/FISHING_SYSTEM_2_BALANCE_REPORT.md` remains useful as a deterministic synthetic probability/unit-regression record, but it is no longer the authoritative content-balance report for actual Tide gameplay.

Detailed implementation and validation records remain in:

- `docs/FISHING_SYSTEM_2_SPEC.md`
- `docs/STAGE_57_58_FINAL_RELEASE_VALIDATION.md`
- `docs/STAGE_59_FISHING_UI_POLISH.md`
- `docs/FISHING_RECOVERY.md`
- `docs/STAGE_60_61_RECOVERY_AND_FINAL_POLISH.md`
- `docs/STAGE_63_TIDEBORNE_CREATIVE_TAB.md`
- `docs/FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`
- `docs/FISHING_SYSTEM_2_BALANCE_REPORT.md` for the historical synthetic regression model
- `docs/TIDEBORNE_INTERNAL_API.md`

## Current execution gate

- `dev` is the active development branch at version 2.0.1.
- 2.0.0 is frozen to the exact published commit and artifact digest listed above.
- normal `dev` pushes run the streamlined CI-only validation path and do not publish a GitHub Release.
- the next public release must be produced from an explicit semantic-version tag matching `gradle.properties` exactly.
- tagged releases run the fuller release validation path before publication.
- real Fishing System 2.0 balance tuning must use `docs/FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md` rather than the historical equal-rarity synthetic report.
- new covered fishing read/query features should prefer `TideborneFishingApi` over direct implementation-layer reads.
- canonical specimen presentation migration is complete; the active architecture stage is now Tide-targeting mixin inventory and replacement planning.
- broad legacy cleanup remains after the mixin architecture pass, not before it.
- `main` must not be merged, rebased, or modified unless explicitly authorized.

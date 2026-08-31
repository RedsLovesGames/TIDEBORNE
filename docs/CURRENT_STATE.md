# Current development state

Updated: 2026-08-31

## Baseline and branch

- compatibility baseline: Tideborne 1.3.57
- current release line: Tideborne 2.0.0
- Minecraft: 1.21.1
- Java: 21
- Tide runtime target: 2.1.1
- active development branch: `dev`
- reconstruction branch: `reconstruct-1.3.57`
- `main` remains untouched at `41e53b052660e04e546b07b305c5047b3f646675`
- authoritative Fishing System 2.0 contract: `docs/FISHING_SYSTEM_2_SPEC.md`

Frozen reconstruction anchors:

- Tideborne 1.3.57 release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- reconstructed canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- Tide 2.1.1 Fabric 1.21.1 SHA-256: `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Apex Waters 1.1.1 Fabric 1.21.1 SHA-256: `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`

Historical implementation details remain available in dedicated stage documents and Git history. This file records the current authoritative state.

## Fishing System 2.0 is complete

The current `dev` branch contains the completed Fishing System 2.0 implementation, its release validation, recovery/admin tooling, UI polish, canonical gear registry, creative-tab work, and the Stage 64 canonical-record and balance audit.

Canonical runtime authority includes:

- server-owned Tide species selection using canonical Fishing Luck weighting and existing Tide eligibility restrictions;
- one canonical natural specimen percentile/base-size sample per catch;
- independent deterministic Body Type, Condition, Pigmentation, and Specimen Quality axes;
- canonical final physical size and size-adjusted final percentile without a second specimen sample;
- canonical Perfect Catch integration and Perfect Specimen behavior;
- server-owned per-player, per-species Trait Momentum;
- canonical FishScore V2 as the production score source;
- canonical Strength, Tempo, line, Steel Leader, rod, hook, bait, and Leviathan Bait behavior;
- canonical ItemStack, entity, bucket, display, Satchel, Journal, record, leaderboard, and network persistence/projection paths;
- deterministic one-way migration for recoverable Tideborne 1.3.57 fish and saved-data representations;
- guarded legacy compatibility paths that cannot reroll or overwrite current canonical V2 state.

The final release validation for the core 2.0.0 implementation is documented in `docs/STAGE_57_58_FINAL_RELEASE_VALIDATION.md`.

## Post-release stages 59 through 63 are complete

Stage 59 established the shared canonical fishing presentation layer and corrected score-projection and layout issues across fishing-facing UI. Canonical specimen/FishScore state remains the read-only UI source of truth.

Stage 60 added operator-only identity-preserving repair and explicitly confirmed destructive reroll tooling for legacy fish. Recovery details are documented in `docs/FISHING_RECOVERY.md`.

Stage 61 closed remaining player-facing integration issues, including canonical progression for legitimate fish entering Tide's normal catch-accounting path and clearer Top Fish specimen details.

The owned legacy-fish Journal backfill then filled missing canonical `latest` display snapshots from actual old fish a player still owns without replaying catch progression or unlocking uncaught species.

Stage 62 hardened gear identity around one exact namespaced canonical fishing-gear registry. Runtime modifiers, advanced tooltips, and operator diagnostics resolve through the same registered identity instead of substring or display-name guesses.

Stage 63 added the dedicated `tideborne:tideborne` Creative Mode tab with the Angler's Satchel icon, stable gameplay-role ordering, correct optional-mod visibility, and no duplicate Tideborne injection into vanilla Tools or Ingredients.

Stage 63 implementation head `24cc3a42e30f9dc8a51bf9abef469585e0d2b48b` passed GitHub Actions run `33360256194`. Its published `tideborne-2.0.0.jar` snapshot had SHA-256 `ff9ef2a8f8aa975336816ae56240302c31dcc777606bb1a88d23c947b799b937` before the later Stage 64 release refresh.

## Stage 64 canonical record and balance audit is complete

Stage 64 closes the final Fishing System 2.0 record semantics, record-recovery safety, specimen projection, and built-in equipment/stacking audit.

### Canonical Best Specimen

`FishScoreLedger` persists one canonical Best Specimen per species. It is separate from the Journal's `latest` specimen snapshot and from historical largest/smallest aggregates.

Best Specimen comparison is deterministic:

1. higher FishScore;
2. higher percentile;
3. higher length;
4. lower deterministic seed;
5. older server catch timestamp;
6. player UUID lexical order.

Canonical record identity includes species, server catch timestamp, deterministic seed, FishScore rounded to six decimals, and player UUID. Duplicate canonical identity is suppressed.

### Team Top 15

Team Top 15 is derived from the canonical Best Specimen map and uses the same deterministic comparator.

- the projection is capped at 15;
- only one current canonical Best Specimen per species participates;
- duplicate canonical record identity is blocked;
- repeated catches of the same species remain valid history, but do not occupy multiple current Top 15 slots;
- no second FishScore formula or `latest`-specimen reconstruction is used.

This supersedes stale Top 12 wording from pre-Stage-64 documentation.

### Replay-safe recovery

`FishRecords.rebuildRecordsFromCatch` can rebuild durable record projections from an already-canonical catch without simulating a new live catch.

Recovery may restore or improve:

- per-player best FishScore;
- canonical Best Specimen;
- derived Team Top 15.

It does not increment recent/total live-catch counters, does not advance challenge progress, and does not emit the normal catch listener/event signal used by live-catch reward paths.

### Canonical UI projection

The Team Records 3D preview is driven by the selected canonical specimen state. The Journal Best Specimen panel exposes FishScore, percentile, length, Body Type, Condition, Pigmentation, Quality, and Perfect Catch from canonical data.

Regression coverage includes Iridescent pigmentation transfer through normal world/entity storage and record-preview state.

### Equipment and stacking audit

The deterministic Stage 64 audit covers:

- all 5 built-in rod tiers;
- all 32 built-in Tide bobbers;
- all 7 built-in hooks;
- all 3 built-in bait entries;
- native/Tideborne line progression;
- Steel Leader line protection;
- Leviathan Bait boss reachability and Fishing System 2.0 modifier ownership;
- tagged third-party bobber compatibility;
- representative full-kit stacking, degraded/corrupted/summoned states, and drop transfer.

Equipment cannot directly author FishScore or canonical specimen geometry. Each modifier layer is applied through its owning system, and FishScore is calculated once from final canonical specimen state.

Detailed audit values and progression tables are in `docs/FISHING_SYSTEM_2_BALANCE_REPORT.md`.

## Stage 64 validation

Validated implementation head:

- `f6aac3e07a7ff7a0955427cc76605bdb94dad086` - `test: finish fishing-system-2.0 balance audit`

GitHub Actions run `33367391365` passed:

- repository/dependency validation;
- clean Java 21 Gradle build;
- unit tests;
- Fabric GameTests with no optional compatibility mods;
- Fabric GameTests with Apex Waters only;
- Fabric GameTests with Myths of the Sea only;
- Fabric GameTests with Apex Waters and Myths of the Sea together;
- dedicated-server smoke;
- client-connect smoke;
- production release JAR validation;
- artifact upload;
- release publishing.

No Stage 64 implementation failure remains after that run.

The Stage 64 documentation closure is committed after the validated implementation head and does not change runtime behavior.

## Current execution gate

Fishing System 2.0 through Stage 64 is complete on `dev`.

There is no known Fishing System 2.0 blocker or unfinished Fishing System 2.0 implementation item. Remaining work in `docs/TODO.md` is intentionally outside the completed Fishing System 2.0 scope, currently long-term licensing policy and future version compatibility.

Do not merge, rebase, or modify `main` unless explicitly authorized.

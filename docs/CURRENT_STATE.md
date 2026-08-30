# Current development state

Updated: 2026-08-30

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

Frozen reconstruction anchors remain:

- Tideborne 1.3.57 release JAR SHA-256: `0c8cd9e9706c2e1cc0a6ca3708c050d5f1d501a0df63d75047188e9fb4b4c4f5`
- reconstructed canonical content-tree SHA-256: `5a825aa33436ed24110b984390455f5d048a651499e4cecd68efa1402ee6aec6`
- Tide 2.1.1 Fabric 1.21.1 SHA-256: `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Apex Waters 1.1.1 Fabric 1.21.1 SHA-256: `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`

Historical implementation details remain available in the dedicated stage documents and Git history. This file records the current authoritative state.

## Fishing System 2.0 is complete

The current `dev` branch contains the completed Fishing System 2.0 implementation and its post-release recovery/polish work.

Canonical runtime authority includes:

- server-owned Tide species selection using canonical Fishing Luck weighting and existing Tide eligibility restrictions;
- one canonical natural specimen percentile/base-size sample per catch;
- independent deterministic Body Type, Condition, Pigmentation, and Specimen Quality axes;
- canonical final physical size and size-adjusted final percentile without a second specimen sample;
- canonical Perfect Catch integration and Perfect Specimen behavior;
- server-owned per-player, per-species Trait Momentum;
- canonical FishScore V2 as the production score source;
- canonical Strength, Tempo, line, Steel Leader, rod, and Leviathan Bait behavior;
- canonical ItemStack, entity, bucket, display, Satchel, Journal, record, leaderboard, and network persistence/projection paths;
- deterministic one-way migration for recoverable Tideborne 1.3.57 fish and saved-data representations;
- guarded legacy compatibility paths that cannot reroll or overwrite current canonical V2 state.

The final release validation for the core 2.0.0 implementation is documented in
`docs/STAGE_57_58_FINAL_RELEASE_VALIDATION.md`.

## Stage 59 UI correctness and polish is complete

Stage 59 established the shared canonical fishing presentation layer and corrected remaining
score-projection and layout issues across the fishing-facing UI.

Current UI contracts include:

- canonical specimen/FishScore data remains the source of truth;
- UI code does not generate or mutate specimen state;
- shared formatting is used for score, percentile, length, traits, timestamps, and unavailable values;
- Team Records, History, leaderboards, Top Fish, species views, tooltips, and the Angler's Satchel use bounded, human-readable layouts;
- contributor and event score projections consume canonical score state rather than a duplicate formula.

Detailed Stage 59 behavior is documented in `docs/STAGE_59_FISHING_UI_POLISH.md`.

## Stage 60 legacy fish recovery tooling is complete

Stage 60 adds operator-only recovery tooling for fish created before Fishing System 2.0.

Repair commands:

```text
/tideborne fishing repair held
/tideborne fishing repair inventory
```

Repair delegates to the existing `CanonicalSpecimenStorage` one-way migration boundary. It does
not call the specimen generator and does not invent a new specimen. Recoverable legacy identity
is preserved where available, including species, deterministic seed, percentile/physical size,
Giant/Dwarf state, mapped Condition, Pigmentation, Perfect Specimen state, and compatible stack
metadata. Missing canonical FishScore is calculated from the final preserved specimen through the
single V2 score service.

Destructive reroll commands:

```text
/tideborne fishing reroll held --confirm
/tideborne fishing reroll inventory --confirm
```

Reroll intentionally replaces specimen identity with newly generated canonical data. The
`--confirm` literal is mandatory. An unconfirmed reroll performs no migration and no write.
Both repair and reroll execute server-side and require operator permission level 2.

Focused GameTests prove deterministic/idempotent repair, identity preservation, confirmation
safety, and deterministic reroll behavior for an explicit replacement seed.

Command details are documented in `docs/FISHING_RECOVERY.md` and combined Stage 60/61 validation
is documented in `docs/STAGE_60_61_RECOVERY_AND_FINAL_POLISH.md`.

## Stage 61 final fishing integration and polish is complete

Stage 61 closes the remaining player-facing integration issues without creating another specimen
or score authority.

Current contracts:

- legitimate registered Tide fish entering Tide's normal `TidePlayerData.logCatch` accounting path
  with physical length but no canonical specimen are deterministically canonicalized before normal
  catch progression executes;
- existing canonical fish remain authoritative and are never rerolled by this bridge;
- the normal Tide catch-accounting path continues to own Journal/discovery, Team Journal,
  leaderboard, history, and record progression rather than a parallel crate-only progression system;
- Top Fish canonical specimen details use full human-readable labels for FishScore, Percentile,
  Length, Body Type, Condition, Pigmentation, and Quality;
- the Top Fish panel preserves the shared Stage 59 formatting/color conventions and bounded hover
  behavior while avoiding the cramped abbreviated `Cond`, `Pig`, and `Qual` presentation;
- UI code remains read-only with respect to canonical specimen generation and mutation.

Focused GameTests cover the progression bridge and canonical-state preservation.

## Final Stage 60/61 validation

Stage 60 implementation commit:

- `72f9a3b55851b0e5cbe8ff68f37d464b4720eadb` - `feat: add legacy fish repair and guarded reroll tooling`

Stage 61 implementation commit:

- `48983670bbb996fc09d9fb2cab0f533fa762c86d` - `fix: polish specimen details and restore crate fish progression`

The first Stage 61 workflow correctly caught that the two new GameTest classes had not been
registered as Fabric GameTest entrypoints. That repository-validation failure occurred before
Java compilation and was fixed by:

- `9619f756c9ecd61139acd5ffc687d68be61a4e04` - `test: register Stage 60 and 61 GameTests`

GitHub Actions run `33319707597` is green on that validated implementation head. It passed:

- exact frozen dependency retrieval and repository validation;
- clean Java 21 Gradle build and the full unit-test suite;
- Fabric GameTests with no optional compatibility mods;
- Fabric GameTests with Apex Waters 1.1.1 only;
- Fabric GameTests with Myths of the Sea 1.3.0 only;
- Fabric GameTests with Apex Waters and Myths of the Sea together;
- dedicated-server/client-connect smoke validation;
- production `tideborne-2.0.0.jar` validation;
- final validation-count checks;
- built-JAR artifact upload;
- release publication/refresh.

No Stage 60 or Stage 61 implementation failure remains after that run.

## Current execution gate after Stage 61

Fishing System 2.0, its legacy recovery/admin tooling, and its final player-facing integration
polish are complete on `dev`.

There is no known Fishing System 2.0 blocker or unfinished Fishing System 2.0 implementation item.
Remaining work in `docs/TODO.md` is intentionally outside the completed Fishing System 2.0 scope,
primarily long-term licensing policy and future version compatibility.

Do not merge, rebase, or modify `main` unless explicitly authorized.

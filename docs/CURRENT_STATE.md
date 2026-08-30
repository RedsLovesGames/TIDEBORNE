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

## Owned legacy fish Journal backfill is complete

A post-release migration gap was found in old Journal entries that retained Tide catch history but
had no canonical `latest` specimen snapshot. Aggregate Tide Journal data can reconstruct historical
largest/smallest lengths, but it cannot safely invent the exact last specimen. The fix therefore
uses an actual old fish item the player still owns as the authoritative missing specimen source.

Implementation commit:

- `44c4803f8f6bee16eb76b162b82883398a8bd3ca` - `fix: backfill owned legacy fish into journal specimens`

Current behavior:

- backfill runs server-side on player login and FTB team changes;
- the repair commands also invoke the backfill so it can be applied immediately without a new catch;
- only species already unlocked in the authoritative Tide Journal are eligible;
- owning a fish never unlocks a species that was never caught;
- an existing canonical `latest` specimen is never overwritten;
- current canonical fish are copied without rerolling;
- recoverable Tideborne legacy fish use the existing deterministic migration boundary;
- old Tide fish with only a valid preserved physical length are deterministically canonicalized
  from registered species plus that length, fixing the class of old fish the original repair
  command classified as having no Tideborne legacy specimen payload;
- total caught, first-catch date, largest/smallest stats, history events, record ownership,
  contributor totals, rewards, and Trait Momentum are not replayed or incremented;
- the backfill never calls Tide's `logCatch` path and is idempotent.

Focused GameTests prove that a length-only owned old fish fills the missing `latest` specimen while
preserving the historical Tide Journal compound byte-for-byte, repeated backfill is stable, and an
owned fish cannot unlock an uncaught species.

GitHub Actions run `33323297138` is green on implementation head
`44c4803f8f6bee16eb76b162b82883398a8bd3ca`. It passed the clean build and unit suite, all four
Fabric GameTest matrices, dedicated-server/client-connect smoke validation, production JAR
validation, artifact upload, and release publication/refresh.

The `TIDEBORN-2.0.0` release now targets that implementation commit. Its refreshed
`tideborne-2.0.0.jar` asset has SHA-256
`0209af64b53617433b5a5cd8bf66b0a3923e3f3636fd81b34985e2cde83d72ab`.

## Stage 62 canonical fishing gear registry is complete

Stage 62 hardens fishing gear identity without changing Fishing System 2.0 balance. Gear behavior is
now keyed by one explicit canonical registry of registered `Item` identities instead of duplicated
consumer-side recognition logic.

Current gear-registry contracts:

- every supported native Tide line and Tideborne fishing gear item has exactly one canonical
  `GearProfile`, origin, and equipment slot classification;
- resolution uses the registered `Item` identity only; display names, translation keys, class names,
  and substring similarity cannot grant fishing behavior to an unregistered lookalike;
- native Tide Copper, Iron, Golden, and Diamond line effects resolve through the shared registry;
- Tideborne Tentacle/Swift lines, Seafarer's/Shark Tooth hooks, and Kujira rod runtime selection
  resolve through the same registry while preserving their existing contextual/configured effects;
- Steel Leader remains applied from its authoritative persisted attachment state, while its item and
  tooltip identity are represented by the same canonical gear profile;
- Leviathan Bait remains applied from the authoritative active-bait state, while its item and tooltip
  identity are represented by the canonical profile;
- advanced Tideborne equipment tooltips consume the same profile identity used by runtime gear
  resolution, preventing UI/runtime identity drift;
- `/tideborne debug gear` provides read-only operator diagnostics for canonical gear item IDs,
  profiles, origins, and slots, and is exposed from the clickable Debug Tools panel;
- modifier arithmetic continues through `FishingGearModifiers.compose`; no second gear stacking or
  balance formula was introduced.

Focused tests cover complete profile registration, rejection of unregistered lookalikes, profile
metadata/reverse identity, and cross-gear stacking across native line, Tideborne line, Steel Leader,
and Leviathan Bait effects.

## Current execution gate

Fishing System 2.0, its legacy recovery/admin tooling, final player-facing integration polish, the
owned legacy-fish Journal backfill, and canonical fishing-gear registry hardening are complete on
`dev`.

There is no known Fishing System 2.0 blocker or unfinished Fishing System 2.0 implementation item.
Remaining work in `docs/TODO.md` is intentionally outside the completed Fishing System 2.0 scope,
primarily long-term licensing policy and future version compatibility.

Do not merge, rebase, or modify `main` unless explicitly authorized.
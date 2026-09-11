# P9 Low-Token Final Audit Context

This file is the fast context map for `agent/p9-final-audit`. It intentionally summarizes completed architecture work so continuation chats do not reread the repository.

## Locked baseline

Baseline SHA: `575f7525eb15988a6567e3942627a9021c756b60`.

P1–P8 are integrated, including replacement P4 consolidation. The baseline passed repository validation, Java 21 compilation, PMD/CPD/Semgrep, Qodana, the combined Java/AI gate, normal build/tests, and release-artifact validation.

Do not reopen completed phases without evidence of an integrated defect.

## Architecture already established

- One Java root: `com.redslovesgames.tideborne`.
- No active `tidetraits`, `tideteamjournal`, `tideboundcompatibility`, or `fishing.v2` Java ownership.
- Genuine Fabric entrypoints are Tideborne-owned.
- Unified Tideborne config/store/network/UI architecture.
- Canonical specimen ownership is current Fishing System; mutation-era compatibility is explicit legacy/migration territory.
- Fishing equipment belongs to `fishing.gear`; Tide adapters to `fishing.tide`; chum/sharks/ecosystem behavior to `ecosystem`; optional mods to `compat`.
- Current resources are Tideborne-owned; retained historical resource namespaces are documented compatibility aliases.
- Historical runtime/public IDs are centralized through explicit legacy owners while serialized/wire/registry values remain unchanged.
- Satchel sorting helpers were consolidated into `SatchelSorting` during P4.
- Java/AI quality CI is delta-oriented and includes repository validation + Java 21 production compile, PMD/CPD/Semgrep, Qodana, and an aggregate gate.

## Authoritative evidence — read only when needed

1. `AGENTS.md` — repository invariants + Ponytail-style minimal-diff discipline.
2. `docs/ARCHITECTURE.md` — current ownership map.
3. `docs/CURRENT_STATE.md` — current implementation/validation state.
4. `docs/TODO.md` — current remaining work, if still current.
5. `.github/TIDEBORNE_QUALITY_GATE.md` — quality policy.
6. `.github/TIDEBORNE_P7_COMPATIBILITY_ALIASES.md` — resource compatibility evidence.
7. `.github/TIDEBORNE_P8_ID_LEDGER.md` — historical-ID classifications and exact-value evidence.
8. `docs/TIDE_MIXIN_INVENTORY.md` — mixin ownership/version sensitivity.

Historical reconstruction/balance docs are evidence, not authority over the current files above.

## High-value audit hotspots

### A. Legacy specimen dependency direction
Report every normal-production import/reference to `fishing.specimen.legacy`. Classify each as:
- migration/compatibility read boundary — potentially valid;
- canonical runtime dependency — defect;
- presentation/diagnostic compatibility — document if valid.

Known example to inspect: `mixin/specimen/TideFishingHookMixin` uses legacy mutation/trait reads while deriving Satchel sorting metadata. Do not change it until ownership/behavior is proven.

### B. `backend` ownership
A top-level `com.redslovesgames.tideborne.backend` package exists. `docs/ARCHITECTURE.md` describes backend/facade composition as a composition-root responsibility, but P9 must verify the actual classes follow that rule rather than being a dumping ground.

### C. Post-P4 stale references
There must be no live reference to removed standalone sorting helpers:
- `SatchelSorter`
- `SatchelSortDescriptor`
- `SatchelTraitSortData`
- `TideSatchelSortMetadataResolver`

### D. Docs vs integrated tree
Current architecture docs may still mention historical package-qualified names in current-tense prose. Historical evidence is allowed; stale current ownership claims are not. Example: audit current Team Journal client ownership wording in `docs/ARCHITECTURE.md`.

### E. Worker tooling residue
P7/P8 context, ledgers, scripts, and branch-only checkpoint workflows are useful provenance but may be stale execution tooling. Classify what should remain as evidence vs what should be retired. Do not delete merely to minimize file count.

## Phase-specific reading strategy

- P9.0: state/context/ledger + repository tree + CI evidence only.
- P9.1: entrypoints, `fabric.mod.json`, config package, `backend`, relevant architecture tests.
- P9.2: `fishing.specimen.legacy`, migration owners, P7 alias map, P8 ID ledger, resource/ID tests.
- P9.3: gear/tide/ecosystem/compat/mixins/network/persistence/Satchel paths only.
- P9.4: current docs, comments and worker tooling only after code ownership is settled.
- P9.5: validators/build/tests/quality/artifact scripts and reports.
- P9.6: audit ledger + final diff only.

## Validation policy

Required: repository validator, Java 21 build/tests, architecture tests, quality gate, release artifact. GameTests and Minecraft boot tests are deliberately not required blockers.

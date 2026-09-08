# Tideborne documentation index

Use this file to avoid loading the repository's entire historical documentation set for every task.

## Authority order

When documents disagree, use this order unless a task explicitly asks for historical behavior:

1. `AGENTS.md`
2. `docs/CURRENT_STATE.md`
3. `docs/TODO.md`
4. `docs/ARCHITECTURE.md`
5. the current subsystem document listed below
6. historical stage, reconstruction, migration, and balance documents

Source code and passing current tests remain the final implementation truth when documentation is stale.

For intended post-2.0 gear behavior, `POST_2_0_GEAR_PROGRESSION_SPEC.md` is the frozen design authority. Source/audit differences identify implementation gaps; they do not override its values or identities. `CURRENT_STATE.md` distinguishes that design freeze from runtime completion.

## Default reading set

For most coding tasks, read only:

- `AGENTS.md`
- `docs/CURRENT_STATE.md`
- `docs/TODO.md`
- one task-specific current document

Do not read every Markdown file by default.

## Current authoritative documents

### Project state and architecture

- `CURRENT_STATE.md`: verified current implementation, versions, completed architecture work, and execution gate
- `TODO.md`: unfinished work only
- `ARCHITECTURE.md`: current package ownership, canonical service boundaries, and dependency direction
- `VALIDATION.md`: current local/CI/release validation routing
- [CODE_CLEANUP_QUEUE.md](CODE_CLEANUP_QUEUE.md): source-backed cleanup batches, deletion estimates, compatibility exclusions and performance validation gates

### Fishing System 2.0

- `FISHING_SYSTEM_2_SPEC.md`: implemented behavior contract and invariants for Fishing System 2.0
- `TIDEBORNE_INTERNAL_API.md`: stable internal fishing read/query facade
- `FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`: authoritative real Tide 2.1.1 runtime-backed balance reference

The older `FISHING_SYSTEM_2_BALANCE_REPORT.md` is a deterministic synthetic regression record, not the current real-content tuning authority.

### Post-2.0 gear progression

- [POST_2_0_GEAR_PROGRESSION_SPEC.md](POST_2_0_GEAR_PROGRESSION_SPEC.md): authoritative frozen post-2.0 gear design, slot identities, archetypes, exact values, composed limits, canonical ownership rules, and source-backed implementation conflicts
- `GEAR_PROGRESSION_AUDIT.md`: pre-rework inventory of rods, lines, hooks, bobbers, bait, leaders, Leviathan paths, Satchel behavior, duplicated ownership, hard-coded checks, and APIs to preserve

The frozen gear mechanics and Satchel tackle manager are implemented; statistical measurements are complete in POST_2_0_GEAR_BALANCE_REPORT.md. Default boss targeting and full balance/manual acceptance remain open. Extend or consolidate the existing `FishingGearRegistry`, `FishingGearModifiers`, `FishingGearEffects`, `FightProfileService`, `TideborneFishingApi`, and canonical species/specimen boundaries. A parallel loadout/registry/modifier/resolver/specimen layer requires current-source proof that the existing architecture cannot represent a required effect.

### Mixins and compatibility

- `TIDE_MIXIN_INVENTORY.md`: current mixin ownership/classification, remaining Tide coupling, and fragility notes
- `ARCHITECTURE.md`: current package-ownership rationale and preserved migration/legacy boundaries

### Recovery and reconstruction

- `RECONSTRUCTION.md`: authoritative 1.3.57 reconstruction provenance and frozen compatibility anchors
- `FISHING_RECOVERY.md`: current recovery tooling/behavior where recovery work is relevant

These are task-specific references, not default reading for normal feature development.

## Historical/reference-only documents

The repository retains many stage reports and legacy audits because they are useful for regression history, migration reasoning, or reconstructing why a compatibility path exists.

Examples include:

- `archive/LEGACY_MIGRATION_AND_FISHING_SYSTEM_2_AUDITS.md`
- `STAGE_*` documents
- older synthetic balance reports
- one-off reconstruction reports

Use them only when the task involves the behavior they document.

Historical documents do not override current code, `CURRENT_STATE.md`, `TODO.md`, or `ARCHITECTURE.md`.

## Task reading recipes

### Normal bug fix

Read:

- `AGENTS.md`
- `CURRENT_STATE.md`
- `TODO.md`
- affected source/tests

Open a subsystem document only if the ownership or compatibility contract is unclear.

### Gear progression design or implementation

Read:

- `AGENTS.md`
- `CURRENT_STATE.md`
- `TODO.md`
- `POST_2_0_GEAR_PROGRESSION_SPEC.md`
- `GEAR_PROGRESSION_AUDIT.md`

Read affected source/tests to resolve the spec's implementation notes. Use `ARCHITECTURE.md` or `FISHING_SYSTEM_2_SPEC.md` only when an ownership or canonical invariant question needs additional context. Older gear values do not supersede the frozen post-2.0 contract.

### Specimen/FishScore change

Read:

- `AGENTS.md`
- `CURRENT_STATE.md`
- `FISHING_SYSTEM_2_SPEC.md`
- relevant V2 source/tests

Open legacy audits only when migration or old-save behavior is actually touched.

### Mixin or Tide-version change

Read:

- `AGENTS.md`
- `CURRENT_STATE.md`
- `ARCHITECTURE.md`
- `TIDE_MIXIN_INVENTORY.md`
- affected Tide adapter/source/tests

### Persistence or migration change

Read:

- `AGENTS.md`
- `CURRENT_STATE.md`
- affected canonical storage/migration code
- `RECONSTRUCTION.md` and the relevant legacy audit only as needed

### UI/presentation change

Read:

- `AGENTS.md`
- `CURRENT_STATE.md`
- `ARCHITECTURE.md`
- `CanonicalSpecimenPresentation` and the affected consumer

Do not recreate specimen trait/score formatting in an individual screen.

## Documentation maintenance

Keep current documents concise.

When implementation state changes:

- update `CURRENT_STATE.md` if verified current behavior changed
- update `TODO.md` if backlog state changed
- update the relevant current subsystem document if its contract changed

Do not append a new historical narrative to every current document after every prompt. Put one-off historical detail in a stage/reference document only when it has long-term value.

## Current Java package ownership

Active production Java is owned by `com.redslovesgames.tideborne.*`. Canonical fishing code uses `tideborne.fishing` and feature children such as `fishing.specimen`, `fishing.gear`, and `fishing.tide`; Journal, Satchel, discovery, ecosystem, compatibility, presentation, networking, registry, mixin, and legacy migration code use their corresponding Tideborne feature packages. Historical serialized identifiers and resource namespaces remain intentionally unchanged for compatibility.

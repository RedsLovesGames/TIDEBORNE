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

### Fishing System 2.0

- `FISHING_SYSTEM_2_SPEC.md`: implemented behavior contract and invariants for Fishing System 2.0
- `TIDEBORNE_INTERNAL_API.md`: stable internal fishing read/query facade
- `FISHING_SYSTEM_2_REAL_BALANCE_REPORT.md`: authoritative real Tide 2.1.1 runtime-backed balance reference

The older `FISHING_SYSTEM_2_BALANCE_REPORT.md` is a deterministic synthetic regression record, not the current real-content tuning authority.

### Post-2.0 gear progression

- `GEAR_PROGRESSION_AUDIT.md`: current inventory of rods, lines, hooks, bobbers, bait, leaders, Leviathan paths, Satchel behavior, duplicated ownership, hard-coded checks, and APIs to preserve

Until a dedicated post-2.0 gear design/spec document is created and frozen, gear implementation should not invent a second registry/modifier architecture. Extend or consolidate the existing `FishingGearRegistry`, `FishingGearModifiers`, `FishingGearEffects`, `FightProfileService`, and related current boundaries first.

### Mixins and compatibility

- `TIDE_MIXIN_INVENTORY.md`: current mixin ownership/classification, remaining Tide coupling, and fragility notes
- `STAGE_4_LEGACY_PACKAGE_CLEANUP.md`: current package-ownership rationale and preserved migration/legacy boundaries

### Recovery and reconstruction

- `RECONSTRUCTION.md`: authoritative 1.3.57 reconstruction provenance and frozen compatibility anchors
- `FISHING_RECOVERY.md`: current recovery tooling/behavior where recovery work is relevant

These are task-specific references, not default reading for normal feature development.

## Historical/reference-only documents

The repository retains many stage reports and legacy audits because they are useful for regression history, migration reasoning, or reconstructing why a compatibility path exists.

Examples include:

- `FISHING_SYSTEM_2_LEGACY_*_AUDIT.md`
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
- `ARCHITECTURE.md`
- `GEAR_PROGRESSION_AUDIT.md`

Use `FISHING_SYSTEM_2_SPEC.md` only for canonical specimen/authority invariants that the gear redesign must preserve.

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
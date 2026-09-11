# Tideborne development instructions

This file is the first stop for humans and coding agents working on the maintained `dev` branch.

## Current project state

Tideborne is a Fabric 1.21.1 addon for Tide 2. Fishing System 2.0 is implemented and is the current production architecture.

Current branch policy:

- active development branch: `dev`
- current development version: `2.0.1`
- current published release: `2.0.0`
- Minecraft: `1.21.1`
- Java: `21`
- Tide runtime target: `2.1.1`
- do not modify, merge, rebase, or retarget `main` unless the user explicitly authorizes it

`docs/CURRENT_STATE.md` is the authoritative record of what is implemented now. `docs/TODO.md` is the authoritative list of unfinished work.

## Read only the context needed for the task

Do not read every Markdown file by default.

For normal development work, start with:

1. `AGENTS.md`
2. `docs/CURRENT_STATE.md`
3. `docs/TODO.md`
4. the task-specific authoritative document named by `docs/INDEX.md`

Open reconstruction reports, historical stage reports, migration audits, old balance reports, or compatibility evidence only when the current task actually depends on them.

Historical documents are evidence and recovery material, not a competing source of current implementation instructions.

## Minimal-diff agent discipline

Tideborne uses a project-safe adaptation of the Ponytail "lazy senior developer" discipline from `DietrichGebert/ponytail`: be efficient, never careless. The best new code is code that does not need to exist.

After understanding the task and tracing the real flow, stop at the first option that fully solves it:

1. Do we need a change at all?
2. Does Tideborne already have the needed owner/helper/pattern? Reuse it.
3. Does Java's standard library already solve it cleanly?
4. Does Fabric, Minecraft, or Tide already own the behavior?
5. Does an already-installed dependency solve it without creating a worse boundary?
6. Can the existing code be simplified or reused instead of adding another layer?
7. Only then add the smallest implementation that completely satisfies the task.

This is a minimization rule, not permission to cut correctness. Never remove or weaken validation at trust boundaries, persistence/data-loss safeguards, server authority, compatibility behavior, security, accessibility, or required error handling just to reduce line count.

For bug fixes, find the root owner before patching the visible symptom. Inspect the callers/consumers of the behavior you change and prefer one correct shared fix over copies at individual call sites.

Use surgical diffs:

- touch only lines/files that trace directly to the assigned task;
- do not reformat, rename, "improve," or delete adjacent unrelated code while passing through it;
- remove imports/variables/helpers that your own change makes dead, not unrelated pre-existing debt;
- if unrelated cleanup is discovered, report it for the appropriate cleanup task rather than silently expanding scope;
- do not add abstractions, wrappers, factories, managers, helpers, or dependencies unless the task actually needs them;
- prefer boring existing patterns over clever new ones;
- do not golf code into a less readable or less robust form merely because it is shorter.

Every non-trivial behavior change should leave the smallest runnable automated check that proves the changed behavior under Tideborne's validation routing below. Do not create a new testing framework or runtime harness just to satisfy this rule.

## Current architecture rules

Fishing System 2.0 is already implemented. Do not create a parallel replacement architecture unless an existing boundary is proven insufficient.

Prefer the existing canonical boundaries:

- `TideborneFishingApi` for covered canonical read/query access
- `FishingGearRegistry` for exact gear identity and slot resolution
- `FishingGearModifiers` for immutable composable gear modifiers
- `FishingGearEffects` for named canonical gear effects
- `SpeciesSelectionService` for canonical species weighting and selection inputs
- `SpecimenGenerator` for canonical specimen construction
- `FishScoreV2Service` for canonical FishScore calculation
- `FightProfileService` for canonical fight transformation and minigame projection
- `CanonicalCatchStateManager` and canonical storage services for catch lifecycle authority
- `CanonicalSpecimenPresentation` for specimen display semantics

Do not introduce a second specimen model, second FishScore calculator, second gear identity registry, second gear modifier authority, or UI-specific reconstruction of canonical specimen traits.

## Fishing System 2.0 ownership

Server-owned canonical flow is:

```text
fishing context and equipped gear
        -> species eligibility and selection
        -> canonical specimen generation
        -> canonical FishScore
        -> canonical fight projection
        -> persistence, records, Journal, Satchel, networking, and presentation
```

Gear may provide inputs to the canonical pipeline. Gear must not directly author natural percentile, canonical size, FishScore, canonical trait results, specimen seed, or specimen identity.

Client code may render, cache, preview, and request actions, but it must not be the source of truth for score, traits, records, inventory, XP, team progression, catch selection, or specimen generation.

## Package ownership

Keep all active Java under `com.redslovesgames.tideborne` and use feature ownership beneath that root. Maintained boundaries include `fishing` (with `fishing.specimen`, `fishing.gear`, and `fishing.tide`), `satchel`, `journal`, `discovery`, `ecosystem`, `compat`, `config`, `presentation`, `command`, `network`, `registry`, `mixin`, and `migration.legacy`. Historical specimen compatibility may remain isolated under an explicitly legacy child package until its migration readers can be retired.

Serialized/resource namespaces are separate from Java ownership. Historical IDs such as `tide_traits:*`, `tide_team_journal:*`, and `tidebound_compatibility:*` remain where compatibility requires them.

Do not create new circular dependencies between feature domains.

## Persistence and compatibility safety

Treat the following as compatibility-sensitive public contracts:

- NBT keys
- saved-state file names
- config keys
- network payload IDs and wire fields
- component IDs
- item and entity IDs
- recipe IDs
- valid Fabric entrypoint registrations and side-safe class paths
- mixin config names
- command names and permissions
- external fish IDs

Any change requires backward compatibility or an explicit migration.

Preserve the historical `tidebound_compatibility:steel_leader` registry ID even though the current semantic tier is Iron Leader.

Optional integrations must remain behind compatibility boundaries. Tideborne without an optional mod must never eagerly resolve that mod's classes.

## Mixin policy

Mixins are adapters, not gameplay owners.

A mixin should capture, redirect, or inject and then delegate to a named service when practical. Do not put large gameplay algorithms inside mixin classes.

For Tide-targeting mixins, use `docs/TIDE_MIXIN_INVENTORY.md` before changing targets or assumptions. The remaining Tide-targeting hooks are version-sensitive integration points unless the current architecture document says otherwise.

Every new or materially changed mixin should document:

- target class and method
- why a normal Fabric event or API is insufficient
- expected failure mode if Tide changes
- whether the mixin is required or optional

## Post-2.0 gear progression rule

The existing gear-path audit is complete in `docs/GEAR_PROGRESSION_AUDIT.md`.

The next gear work must extend and consolidate the existing gear architecture rather than blindly creating a new loadout stack.

In particular:

- prefer `FishingGearRegistry` over new scattered item/string checks
- prefer `FishingGearModifiers` and `FishingGearEffects` over a second modifier model
- extend `FightProfileService` or a clearly owned adjacent service when new fight dimensions are required
- consolidate duplicated bobber, line, leader, and Leviathan ownership as those paths are migrated
- preserve Tide-native eligibility/accessory behavior when it remains the correct owner
- preserve server-authoritative specimen generation
- freeze coordinated gear design before large gameplay rebalance work

## Validation routing

Do not run every validation layer after every small change.

Use the cheapest gate that proves the changed behavior:

- Markdown-only change: no Gradle validation required
- pure formulas or data: targeted unit tests
- multiple pure Java changes: `./gradlew test`
- normal implementation change: `./gradlew build`
- mixin, networking, persistence, server lifecycle, or runtime integration change: relevant GameTests plus `./gradlew build`
- client presentation change: build plus a manual in-client check when visual correctness matters
- major integration milestone: normal CI on `dev`
- release candidate/tag: full release validation, optional compatibility matrices, and dedicated-server smoke

The active workflow is `.github/workflows/build.yml`. Normal `dev` pushes are CI-only. Public releases are created only from explicit semantic-version tags that match `gradle.properties`.

Do not duplicate `build` with separate compile/test commands unless isolating a failure requires it.

## Manual Minecraft checks

Prefer human in-client validation for subjective or visual behavior such as:

- screen layout and clipping
- tooltip readability
- animation/rendering
- fishing feel
- fight feel
- menu usability

Automated tests should cover deterministic rules, server authority, persistence, networking, compatibility boundaries, and regression-prone calculations.

## Naming and structure

Prefer names that state domain intent. Avoid new catch-all `Utils`, `Helper`, `Manager2`, `Misc`, or similarly vague classes.

As a default:

- keep public methods focused on one operation
- extract calculations from screens, mixins, and networking code
- keep config parsing separate from gameplay logic
- keep serialization separate from domain decisions
- reuse canonical domain records instead of creating UI or compatibility copies

## Working-state discipline

After a meaningful implementation pass, update `docs/CURRENT_STATE.md` when the verified state actually changed and update `docs/TODO.md` when backlog state changed.

Do not append historical stage narration to current-state files merely because a prompt completed. Keep current docs concise enough that the next agent can resume without rereading the repository's entire history.

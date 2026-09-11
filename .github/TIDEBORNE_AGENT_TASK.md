# Tideborne Agent Task — P8 Historical ID Centralization

Repository: `https://github.com/RedsLovesGames/TIDEBORNE`
Assigned branch: `agent/p8-legacy-ids`
Model: GPT-5.6 Sol High

Work ONLY on this branch. Never modify/push/merge/rebase/retarget/force-update `main`, `dev`, or another worker branch.

## Start / continuation fast path

1. Read `.github/TIDEBORNE_AGENT_PROMPTS.md` from `main` once.
2. Run `python3 scripts/p8_agent.py report`.
3. Read only these branch-local files before coding:
   - `.github/TIDEBORNE_AGENT_STATE.json`
   - `.github/TIDEBORNE_AGENT_HANDOFF.md`
   - `.github/TIDEBORNE_P8_CONTEXT.md`
   - `.github/TIDEBORNE_P8_ID_LEDGER.md`
4. Follow `next_action` from the state file. Do NOT restart the whole audit in a continuation chat.

If a chat/tool budget is getting low, stop broad exploration. Finish the smallest coherent edit, validate it, commit the product change, then run the checkpoint command described below so another chat can resume exactly.

## Dependency gate

P1–P7 are integrated into the baseline from which this branch was created.

P8 baseline: `94052f905b7c3bda33e9f1b0b3988ebf93dd327f`

Before product edits verify:
- this branch descends from that baseline;
- P7 resource ownership is present;
- P7 compatibility aliases remain intact;
- P7 final state was COMPLETE.

If not, STOP and report the divergence. Never force-update.

## Goal

Create one explicit, understandable ownership architecture for Tideborne's remaining HISTORICAL/PUBLIC COMPATIBILITY IDENTIFIERS while preserving their externally observable values exactly.

P8 is primarily a centralization/classification task, NOT an ID migration task.

Canonical current identifiers should remain feature-owned. Historical identifiers that must survive for old worlds, ItemStacks, components, saves, network peers, datapacks, dependency checks, commands, recipes, tags, config compatibility, or published extension points should be owned explicitly as legacy/compatibility identities instead of being scattered as unexplained string literals throughout normal runtime code.

Preferred conceptual ownership is under `com.redslovesgames.tideborne.migration.legacy` (or a small focused subpackage such as `migration.legacy.ids`). Avoid one giant `LegacyIds` God class when separate small owners are clearer.

## Historical namespace families to classify completely

Every production occurrence of these Tideborne-owned historical namespace names must end P8 in one of three states: explicit compatibility owner, intentional resource alias documented by P7, or documented external/upstream reason.

- `tide_traits`
- `tide_team_journal`
- `tidebound_compatibility`

P7 already established that retained historical resource trees are compatibility aliases. Do NOT redo P7 or move those aliases merely for cosmetic uniformity.

Audit remaining Java/runtime identity families including:
- Fabric `provides` aliases / historical mod identities
- item and entity registry IDs
- data-component IDs
- payload/network channel IDs
- persistent-state/save IDs
- NBT/component/save keys where historical ownership is embedded
- Journal/team/discovery identifiers
- Satchel compatibility identifiers
- specimen compatibility identifiers
- reload-listener IDs
- config filenames/compatibility IDs
- command/function compatibility surfaces
- public recipe/tag IDs referenced from Java/tests
- migration lookup aliases
- optional integration identities
- tests/scripts/docs that define or verify the above

Do not treat plain historical documentation text as production runtime debt unless it is used as a published compatibility contract.

## Preserve EXACTLY

Do not change externally observable identity values unless a proven compatibility bridge retains the old value and the task explicitly requires a canonical current alias.

Preserve:
- fish/item/entity/recipe/tag registry IDs
- component IDs
- NBT/save keys and persistent-state IDs
- payload IDs and networking semantics
- old-world/item migration behavior
- Fabric historical dependency aliases
- Satchel persistence/behavior
- Journal/history/team/Top Fish persistence/behavior
- specimen identity, BodyType, Condition, Pigmentation, SpecimenQuality
- FishScore, Trait Momentum, RNG behavior/order
- commands
- gear/Tide bait/gameplay balance
- optional Apex/Myths behavior and absence safety
- dedicated-server safety
- server-authoritative specimen generation
- P7 resource compatibility aliases

A successful P8 may change WHERE an ID constant is defined and HOW code references it. It must not casually change WHAT old external data sees.

## Target architecture

Prefer:
- feature-owned constants for canonical current IDs;
- focused legacy compatibility owners for historical IDs;
- explicit names such as `LegacyNamespaces`, `LegacyRegistryIds`, `LegacyNetworkIds`, `LegacyPersistenceIds`, or smaller feature-specific equivalents when useful;
- helper methods only when they remove repeated compatibility logic, not just to hide strings;
- comments only where the compatibility reason is non-obvious;
- tests that make accidental historical-ID changes fail loudly.

Avoid:
- broad global string replacement;
- duplicate canonical+legacy constants with unclear authority;
- reflection-based ID routing;
- wrapper classes with no real ownership purpose;
- migrating IDs just to remove a historical namespace literal;
- a single mega-class containing every ID in the mod.

## Resumable phase plan

### P8.0 — Inventory + classification
Use the precomputed P7 handoff/context. Enumerate remaining historical literals narrowly, update the ID ledger, and classify each runtime family before changing code.

### P8.1 — Registry + component identities
Centralize historical item/entity/data-component/registry identities. Preserve exact serialized registry IDs.

### P8.2 — Network + persistence identities
Centralize payload/channel, persistent-state, NBT/save, Journal/team/discovery/specimen compatibility identities. Preserve exact wire/save values.

### P8.3 — Compatibility/service identities
Centralize historical mod aliases, config compatibility names, reload-listener IDs, public command/function and Java-referenced tag/recipe compatibility identities where appropriate. Do not move P7 alias resources.

### P8.4 — Remove stray ownership leaks
Replace unexplained historical literals in normal production code with the correct canonical or explicit legacy owner. Add architecture/source tests preventing new unclassified leaks.

### P8.5 — Compatibility regression audit
Prove every retained historical identity has a reason and exact value. Update the ledger to FINAL classifications; ensure P7 aliases and optional integrations still point at the intended IDs.

### P8.6 — Full validation + closure
Run the current repository gate and produce the final completion report/checkpoint.

## Checkpoint discipline

Product work and checkpoint metadata are separate commits.

After a coherent product commit:

`python3 scripts/p8_agent.py checkpoint --phase P8.X --completed --next "<exact next action>" --note "<important result>"`

Add validation results as needed:

`--validation repository_structure=pass`
`--validation java21_clean_build=pass`
`--validation unit_architecture_tests=pass`
`--validation compatibility_id_audit=pass`
`--validation optional_compatibility_safety=pass`
`--validation release_artifact=pass`

The checkpoint tool refuses dirty worktrees. This is intentional: continuation state must never claim unsaved edits are safe.

For a blocker, commit any safe coherent product work first, then checkpoint with:

`--status BLOCKED --blocker "<exact blocker>" --next "<exact recovery action>"`

For final closure use `--complete`.

## Validation

Use the repository's current policy. GameTests and Minecraft boot tests are not required blockers.

Before COMPLETE:
1. `scripts/validate_repository.sh` passes.
2. Java 21 clean Gradle build passes.
3. Normal unit + architecture tests pass.
4. Historical-ID source/architecture tests pass.
5. Exact persisted/wire/registry values are regression-tested where practical.
6. P7 compatibility resources remain valid.
7. Optional Apex-only, Myths-only, combined, and absent-mod boundaries remain safe where relevant.
8. `scripts/validate_release_artifact.sh` passes.
9. Production release artifact is produced.
10. No unexplained `tide_traits`, `tide_team_journal`, or `tidebound_compatibility` literal remains in normal production code.

Do not weaken tests to make them green.

## Completion report

Return:
1. final branch SHA
2. historical namespaces/families audited
3. new legacy-ID ownership classes/files
4. exact ID values deliberately preserved
5. canonical IDs left feature-owned
6. P7 compatibility aliases retained
7. remaining historical literals and exact reason for each category
8. architecture/regression tests added or changed
9. repository validator result
10. Java 21 build + normal test result
11. optional compatibility safety result
12. release artifact validation + artifact SHA if available
13. exact integration notes for fast-forwarding P8 into `dev`

Do not begin P9 from this branch.
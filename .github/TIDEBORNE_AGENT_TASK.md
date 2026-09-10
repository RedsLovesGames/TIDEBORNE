# Tideborne Agent Task — P7 Resource Ownership

Repository: `https://github.com/RedsLovesGames/TIDEBORNE`

Assigned branch: `agent/p7-resources`

Use GPT-5.6 Sol High.

Work ONLY on this branch.

Never modify, push, merge, rebase, retarget, or force-update:

- `main`
- `dev`
- any other worker branch

## AI execution protocol — mandatory

P7 is intentionally optimized for short, resumable worker chats. Do not spend the beginning of each chat reconstructing project history.

### Minimal startup

On every fresh or continued worker chat:

1. Fetch current repository state and verify the branch is `agent/p7-resources`.
2. Read `.github/TIDEBORNE_AGENT_STATE.json` first.
3. Read this task file.
4. Read `.github/TIDEBORNE_P7_CONTEXT.md`.
5. Read `.github/TIDEBORNE_AGENT_PROMPTS.md` from `main` once for global constraints.
6. Run `python3 scripts/p7_agent.py report` when a shell checkout is available.
7. Execute only the state's current `phase` and `next_action` before expanding scope.

Do NOT begin by rereading the whole repository, all documentation, or all P1-P6 history. Use exact path/identifier searches driven by the current phase and context map.

### Resumable phase plan

Work in these independently checkpointable phases:

- `P7.0` — inventory and classify every historical resource family as current ownership, compatibility identity, upstream extension, optional integration, or non-runtime documentation.
- `P7.1` — client assets and translation-key ownership.
- `P7.2` — data resources, recipes, tags, datapack paths and ownership.
- `P7.3` — Java resource lookups, reload IDs, dynamic resource IDs and focused tests.
- `P7.4` — mixin configs, refmaps, Fabric metadata and Gradle/resource-processing references.
- `P7.5` — compatibility alias audit; explain every historical runtime namespace that remains.
- `P7.6` — full current validation gate and closure report.

Prefer one phase per chat. Start a later phase only after the current phase is committed and checkpointed and there is clearly enough execution budget remaining.

### Mandatory checkpoint discipline

After every coherent phase or subphase, and BEFORE tool/context limits become risky:

1. Run focused validation for the work just completed.
2. Commit the smallest coherent product/resource/code change to this branch.
3. Ensure the worktree is clean.
4. Run `python3 scripts/p7_agent.py checkpoint` with the current phase, status, completed phase(s), exact next action, blockers, and any validation results.
5. Commit `.github/TIDEBORNE_AGENT_STATE.json` separately as a lightweight checkpoint commit.
6. Push the branch.
7. Run `python3 scripts/p7_agent.py report` and include its compact output in the chat status/completion message.

Example:

`python3 scripts/p7_agent.py checkpoint --phase P7.2 --status IN_PROGRESS --complete P7.1 --next "Migrate the remaining classified data resources and update their direct references" --validation resource_paths_translations=pass`

The helper refuses to create a checkpoint while uncommitted work exists. Never mark a phase complete before its product changes are committed.

If a chat is approaching a tool limit, checkpoint early rather than attempting one more large audit. A fresh chat must be able to resume from the last pushed checkpoint without needing the previous chat transcript.

### Continuation recovery

`.github/TIDEBORNE_AGENT_STATE.json` is the continuation source of truth.

If branch HEAD differs from `last_product_checkpoint_sha`, do not restart the task. The normal reason is the separate state checkpoint commit or later product commits. Inspect only commits after the recorded product checkpoint, repair the state if it is stale, and resume its `next_action`.

Do not redo phases listed in `completed_phases` unless a later validation failure proves that phase regressed.

Unpushed edits from a previous chat are not assumed recoverable in a fresh execution environment. Never claim unpushed work survived.

### Automatic reporting

Every push to this branch runs `.github/workflows/p7-agent-checkpoint.yml`, which executes the compact P7 report and publishes it to the GitHub Actions job summary plus a short-lived artifact.

This automatic report is a continuation aid only. It does not replace required build/test/artifact validation.

See `.github/TIDEBORNE_AGENT_HANDOFF.md` for the minimal fresh-chat procedure.

## Required reading

Read `.github/TIDEBORNE_AGENT_PROMPTS.md` from `main` first after loading the branch-local state/task/context files described above.

## Dependency gate

P1, P2, P3, P5, and P6 are already integrated into the `dev` baseline from which this branch was created.

Baseline commit:

`b2ea30d927c4bae15f2e9ccbdc48b61331ead9f4`

Before production editing:

1. Verify this branch still descends from the baseline above.
2. Verify P3 config unification is present.
3. Verify P5 canonical/legacy specimen isolation is present.
4. Verify P6 feature re-ownership is present.
5. If the branch has diverged or required architecture is missing, STOP, checkpoint/report the blocker, and state exactly why instead of guessing.

## Goal

Unify CURRENT active Tideborne resources under Tideborne ownership while preserving persisted compatibility identities needed by existing worlds, items, saves, networking, and optional integrations.

Resource ownership and persisted identity are separate concerns.

Do not blindly rename historical IDs merely because active resources move under Tideborne ownership.

## Target active resource ownership

Current active assets should converge toward:

- `assets/tideborne/`
- `data/tideborne/`

Current translation keys should converge toward Tideborne ownership, including families such as:

- `config.tideborne.*`
- `tooltip.tideborne.*`
- `message.tideborne.*`
- `screen.tideborne.*`
- `toast.tideborne.*`
- `key.tideborne.*`

Consolidate historical mixin config filenames under Tideborne ownership where safe, conceptually toward:

- `tideborne.mixins.json`
- `tideborne.client.mixins.json`

Keep integration-specific mixin/resource boundaries only where they have a real optional-mod or classloading reason.

## Audit

Audit current equivalents of all active resources and references, including:

- `assets/tide/`
- `assets/tide_traits/`
- `assets/tide_team_journal/`
- `assets/tidebound_compatibility/`
- `assets/tideborne/`
- analogous `data/*` namespaces
- textures
- models
- item/block model references
- lang files
- translation keys
- recipes
- tags
- loot/resource data
- datapack paths
- dynamically-created resource identifiers
- reload listener identifiers
- resource lookups in Java
- Fabric metadata
- mixin configs
- refmaps
- client-only resources
- optional Apex/Myths resources
- scripts/tests that encode resource locations
- extension points that expect historical resource namespaces

Do not assume all historical namespaces can be deleted. Determine whether each occurrence is current resource ownership or persisted compatibility identity.

Use `.github/TIDEBORNE_P7_CONTEXT.md` as the precomputed initial inventory and `scripts/p7_agent.py report` to refresh counts after changes.

## Preserve exactly

Preserve gameplay and persisted compatibility semantics.

Do not change:

- fish IDs
- item IDs
- recipe identities that are externally persisted/referenced unless a compatibility alias is retained
- component IDs
- NBT/save keys
- payload IDs
- canonical specimen identity
- BodyType / Condition / Pigmentation / SpecimenQuality behavior
- FishScore behavior
- Trait Momentum
- RNG behavior or RNG call order
- Satchel persistence and behavior
- Journal/history/team/Top Fish persistence and behavior
- records/badges
- commands
- networking semantics
- gear balance
- Tide bait behavior
- optional Apex/Myths behavior
- server-authoritative specimen generation
- dedicated-server safety
- old-world migration behavior

Historical namespace identifiers such as:

- `tide_traits:*`
- `tide_team_journal:*`
- `tidebound_compatibility:*`

may still be required as compatibility identities.

Do not rename those persisted identifiers just to make resources look uniform.

P8 exists specifically to centralize the remaining historical identifier families after this resource pass.

## Ownership rules

Use Tideborne namespace for current active presentation/data resources whenever compatibility does not require the historical namespace.

If an old namespace must remain for compatibility:

- keep the smallest necessary alias/bridge
- make the compatibility purpose explicit
- do not continue using the historical namespace as the normal current ownership surface
- document why it remains

Avoid duplicate live resources in both historical and Tideborne namespaces unless the duplicate is intentionally required as a compatibility alias.

## Mixin/resource safety

When consolidating mixin configuration:

- preserve client/common separation
- preserve optional-mod classloading guards
- preserve target resolution
- preserve refmap behavior
- do not load client classes on dedicated server
- do not make Apex/Myths classes mandatory when their mods are absent

Verify any renamed mixin config is reflected consistently in Fabric metadata, Gradle/resource processing, tests, and artifact validation.

## Scope exclusions

Do NOT:

- perform P8 historical-ID centralization beyond what is necessary for this task
- delete old-world compatibility identifiers
- redesign P3 configuration
- redo P5 specimen architecture
- redo P6 gameplay ownership
- perform broad class consolidation
- mass-format unrelated Java
- rename persisted IDs for cosmetic consistency
- change gameplay or balance
- change RNG probabilities or call ordering
- remove migration readers still needed by old saves/items

The branch-local P7 state/context/handoff/reporting workflow and script are AI-execution scaffolding. Do not let them alter production behavior. They may be dropped during final integration or removed in the final cleanup phase after P7 is safely complete.

## Validation

Use the repository's current validation policy.

Required before declaring P7 complete:

1. `scripts/validate_repository.sh` passes.
2. Java 21 clean build passes.
3. All normal unit and architecture tests pass through the Gradle build.
4. Resource-path and translation-key tests are updated or added where useful.
5. Fabric metadata resolves all entrypoints and mixin configs.
6. Mixin/refmap validation passes.
7. Optional Apex-only resource/mixin loading remains safe.
8. Optional Myths-only resource/mixin loading remains safe.
9. Apex + Myths together remain safe.
10. Dedicated-server-sensitive resource/classloading boundaries remain valid.
11. `scripts/validate_release_artifact.sh` passes.
12. Production release artifact is produced successfully.
13. No active current resource accidentally remains under a historical namespace without an explicit compatibility reason.

GameTests and Minecraft boot tests are not part of the current required validation gate and must not be reintroduced as blockers.

Do not weaken tests merely to make them green.

Record validation results in `.github/TIDEBORNE_AGENT_STATE.json` through the checkpoint helper as they become known.

## Commit rules

Commit only to `agent/p7-resources`.

Never update `dev` or `main` from this worker.

Never force-update any ref.

Keep product commits logically scoped and reviewable. Keep checkpoint-state commits separate and lightweight so continuation chats can distinguish actual implementation from orchestration state.

## Completion report

Before declaring complete, checkpoint with `--status COMPLETE --complete P7.6` and ensure all required validation fields are accurately recorded.

Return:

1. final branch SHA
2. active asset namespaces before/after
3. active data namespaces before/after
4. translation-key migrations
5. mixin config/refmap changes
6. Java resource identifier changes
7. compatibility aliases retained and exact reasons
8. any historical namespaces still present and whether each is persisted compatibility or optional integration
9. structural validator result
10. Java 21 build/unit/architecture test result
11. optional-mod resource/classloading validation result
12. release artifact validation result
13. exact integration notes for merging P7 into `dev`
14. the final compact output of `python3 scripts/p7_agent.py report`

Do not begin P8 or P9 from this branch.

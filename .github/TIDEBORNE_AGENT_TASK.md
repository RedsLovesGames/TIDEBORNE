# Tideborne Agent Task — P7 Resource Ownership

Repository: `https://github.com/RedsLovesGames/TIDEBORNE`

Assigned branch: `agent/p7-resources`

Use GPT-5.6 Sol High.

Work ONLY on this branch.

Never modify, push, merge, rebase, retarget, or force-update:

- `main`
- `dev`
- any other worker branch

## Required reading

Read `.github/TIDEBORNE_AGENT_PROMPTS.md` from `main` first.

## Dependency gate

P1, P2, P3, P5, and P6 are already integrated into the `dev` baseline from which this branch was created.

Baseline commit:

`b2ea30d927c4bae15f2e9ccbdc48b61331ead9f4`

Before editing:

1. Fetch current repository state.
2. Verify this branch still descends from the baseline above.
3. Verify P3 config unification is present.
4. Verify P5 canonical/legacy specimen isolation is present.
5. Verify P6 feature re-ownership is present.
6. If the branch has diverged or required architecture is missing, STOP and report exactly why instead of guessing.

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

Do not weaken tests merely to make them green.

## Commit rules

Commit only to `agent/p7-resources`.

Never update `dev` or `main` from this worker.

Never force-update any ref.

Keep commits logically scoped and reviewable.

## Completion report

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

Do not begin P8 or P9 from this branch.
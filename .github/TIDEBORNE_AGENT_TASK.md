# Tideborne Agent Task — P3 Configuration Unification

Repository: `https://github.com/RedsLovesGames/TIDEBORNE`

Assigned branch: `agent/p3-config`

Use GPT-5.6 Sol High.

Work ONLY on this branch.

Never modify, push, merge, rebase, retarget, or force-update:

- `main`
- `dev`
- any other worker branch

## Required reading

Read `.github/TIDEBORNE_AGENT_PROMPTS.md` from `main` first.

## Dependency gate

P1, P2, P5, and P6 have already been integrated into the validated `dev` baseline from which this branch was created.

The baseline commit used to create this branch is:

`0a79327168ffb3f6946f89f1e7edb546fcb3504d`

That baseline passed the current validation gate:

- repository structural validation
- Java 21 clean build
- unit tests
- production release artifact validation
- artifact upload

GameTests and Minecraft boot tests are no longer part of the current required validation gate. Do not reintroduce them as blockers.

Before editing:

1. Fetch current repository state.
2. Verify this branch still descends from the validated baseline above.
3. Verify the completed P2 initialization cleanup is present: only genuine Tideborne Fabric entrypoints behave as Fabric entrypoints.
4. Verify P5 canonical/legacy specimen isolation and P6 gameplay reownership are present.
5. If the branch has diverged or those dependencies are missing, STOP and report exactly why instead of guessing.

## Goal

Replace Tideborne's fragmented historical configuration systems with one coherent Tideborne-owned configuration architecture and one coherent in-game configuration UI.

## Audit

Audit all current equivalents of:

- config classes
- config managers
- server setting holders
- client setting holders
- ModMenu implementations
- config screens
- config networking
- feature-specific settings screens
- reflection between config systems
- legacy config mirror writers
- duplicated defaults and validation logic

Historical/current candidates include equivalents of:

- `TideborneUnifiedConfigScreen`
- `TideborneConfigBackend`
- `TideborneTraitsDraft`
- `TideTraitsConfig`
- `TideTraitsConfigManager`
- `ServerConfig`
- `ClientConfig`
- `ClientConfigScreen`
- `ClientServerSettings`
- `TideboundConfig`
- `TideboundClientConfig`
- `ClientTideboundSettings`
- `TideboundModMenu`

Do not assume those exact names still exist after prior phases; audit their current equivalents.

## Target architecture

Converge toward a small Tideborne-owned configuration surface such as:

- `TideborneConfig`
- `TideborneClientConfig`
- `TideborneConfigStore`
- `TideborneConfigNetworking`
- `TideborneConfigScreen`
- thin `TideborneModMenu`
- `LegacyConfigImporter`

Use equivalent names if the actual codebase makes a clearly better ownership boundary, but preserve the architectural intent.

### Ownership rules

Server configuration owns gameplay-affecting settings.

Client configuration owns local presentation-only settings.

Normal current files should converge toward:

- `config/tideborne.json`
- `config/tideborne-client.json`

Provide one coherent configuration screen rather than multiple historical config UIs.

Expected permission behavior:

- singleplayer: server + client settings editable
- multiplayer operator: client + authorized synced server settings editable
- multiplayer non-operator: client settings editable, server settings read-only

Journal settings should be represented as a Tideborne configuration category rather than a separate historical configuration identity.

## Migration behavior

Historical configuration files are compatibility/migration INPUTS only.

They may be read to import old settings when necessary, but should not remain continuously synchronized mirrors and should not keep being rewritten as parallel current configs.

Remove internal reflection between Tideborne configuration systems when direct typed ownership is possible.

Do not silently discard recoverable historical user settings.

## Preserve exactly

Do not change gameplay balance or externally persisted identity as part of this task.

Preserve:

- existing setting defaults unless correcting an objectively duplicated/incorrect historical mirror
- server authority and operator permissions
- configuration networking semantics
- fallback behavior on malformed/missing historical configs
- fish/item/recipe IDs
- component/NBT IDs
- payload IDs unless an explicit compatibility alias is retained
- Satchel behavior and persistence
- Journal/history/team/Top Fish behavior and persistence
- records/badges
- commands
- canonical specimen identity
- Fishing System RNG behavior and RNG call order
- FishScore behavior
- BodyType / Condition / Pigmentation / SpecimenQuality behavior
- Trait Momentum
- gear and Tide bait behavior
- optional Apex/Myths safety
- dedicated-server classloading safety
- existing old-world compatibility

Historical identifiers such as `tide_traits:*`, `tide_team_journal:*`, and `tidebound_compatibility:*` may be persisted compatibility IDs. Do not blindly rename them during P3.

## Scope exclusions

Do NOT:

- perform P4 consolidation work merely to reduce file count
- perform P7 resource namespace migration
- perform P8 historical-ID centralization
- remove migration readers that are still needed
- rename persisted IDs for cosmetic consistency
- mass-format unrelated files
- redesign gameplay
- change RNG probabilities or call ordering
- create a giant God config class containing unrelated UI/network/storage logic

## Validation

Use the repository's CURRENT validation policy, not obsolete GameTest instructions.

Required before declaring P3 complete:

1. `scripts/validate_repository.sh` passes.
2. Java 21 clean build passes.
3. All unit tests and architecture tests pass through the normal Gradle build.
4. Configuration migration/import tests pass or are added where necessary.
5. Configuration networking/permission behavior has focused automated coverage where practical.
6. `scripts/validate_release_artifact.sh` passes.
7. Production release artifact is produced successfully.
8. No extra Fabric entrypoints are reintroduced.
9. No P2/P5/P6 architecture regression is introduced.

Do not weaken tests merely to make them green.

## Commit rules

Commit only to `agent/p3-config`.

Never update `dev` or `main` from this worker.

Never force-update any ref.

Keep commits logically scoped and easy to review.

## Completion report

Return:

1. final branch SHA
2. concise summary of the old configuration architecture removed
3. final configuration ownership architecture
4. final config files and UI flow
5. legacy config import behavior
6. server/client permission behavior
7. networking changes
8. files/classes removed, renamed, or consolidated
9. structural validator result
10. Java 21 build/unit/architecture test result
11. release artifact validation result
12. remaining risks or intentionally retained legacy config dependencies
13. exact integration notes for merging P3 into `dev`

Do not begin P7, P8, or P9 from this branch.
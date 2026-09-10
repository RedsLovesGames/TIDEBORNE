# P7 AI Continuation Handoff

This file exists so a fresh worker chat can resume P7 without reconstructing the project history.

## Launch

Send only:

`Follow agent/p7-resources`

The worker must treat `.github/TIDEBORNE_AGENT_STATE.json` as the current checkpoint source of truth and `.github/TIDEBORNE_AGENT_TASK.md` as the task contract.

## Minimal startup order

1. Verify the checked-out branch is `agent/p7-resources` and fetch current remote state.
2. Read `.github/TIDEBORNE_AGENT_STATE.json`.
3. Read `.github/TIDEBORNE_AGENT_TASK.md`.
4. Read `.github/TIDEBORNE_P7_CONTEXT.md`.
5. Run `python3 scripts/p7_agent.py report`.
6. Read only files needed for the state's `phase` and `next_action`.

Do not begin by rereading the entire repository, all docs, or all prior commits. Completed phases in the state file are closed unless later validation proves a regression.

## Checkpoint before a chat/tool limit

Do this early enough that the final calls are available:

1. Finish the smallest coherent substep.
2. Run its focused validation.
3. Commit product/resource/code changes to `agent/p7-resources`.
4. Run, for example:

   `python3 scripts/p7_agent.py checkpoint --phase P7.2 --status IN_PROGRESS --complete P7.1 --next "Continue data resource ownership migration for the remaining classified namespaces"`

5. Commit the changed `.github/TIDEBORNE_AGENT_STATE.json` as a separate lightweight checkpoint commit.
6. Push the branch.
7. Run `python3 scripts/p7_agent.py report` and paste the compact report into the chat.

The checkpoint command intentionally records the product commit SHA that existed immediately before the state-only checkpoint commit.

## If a new chat sees unexpected branch state

Do not restart P7. Compare the current branch HEAD to `last_product_checkpoint_sha` and inspect only commits after that checkpoint. If the state file is stale, reconstruct the smallest accurate state from those recent commits, checkpoint it, then resume `next_action`.

If an interrupted chat failed before pushing, unpushed edits are not assumed recoverable in a fresh environment. Never claim they survived. Resume from the last pushed checkpoint.

## Automatic reporting

Every push to `agent/p7-resources` runs the lightweight `P7 AI Checkpoint Report` workflow. It emits the same compact inventory/status report to the GitHub Actions job summary and uploads it as a small artifact. This is reporting only; it is not a substitute for the task's required final validation.

## Phase boundaries

- `P7.0` — verify inventory; classify current ownership vs persisted/legacy identity.
- `P7.1` — client assets and translation keys.
- `P7.2` — data resources, recipes, tags, datapack ownership.
- `P7.3` — Java resource lookups, reload IDs, dynamic identifiers, tests.
- `P7.4` — mixin configs, refmaps, Fabric metadata and processing references.
- `P7.5` — compatibility alias audit; explain every historical namespace still retained.
- `P7.6` — full current validation gate and completion report.

Prefer one phase per chat. Continue into another phase only when the current one is committed, checkpointed, and there is clearly enough execution budget left.

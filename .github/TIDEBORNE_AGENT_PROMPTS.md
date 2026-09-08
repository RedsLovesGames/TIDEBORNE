# Tideborne Parallel Agent Prompt Plan

This file coordinates the post-Prompt-1 Tideborne architecture cleanup.

## Global rules

- `main` is documentation/reference only for this operation. Worker chats must never modify product code on `main`.
- `dev` is integration-only. Worker chats must never directly edit or push product work to `dev`.
- Prompt 1 package unification must finish and produce a validated final `dev` commit before any downstream worker edits code.
- Every worker branch was created early from a Prompt-1-era `dev` ancestor. Before editing, the worker MUST fetch current `dev` and fast-forward its own branch to the latest validated `dev` commit. Never force-update. If the branch cannot fast-forward cleanly, stop and report the divergence.
- Preserve persisted IDs, registry IDs, NBT/component IDs, payload IDs, save keys, recipes, commands, deterministic RNG, FishScore semantics, Satchel persistence, Journal/history/Top Fish, Tide integration, and optional-mod classloading unless the assigned task explicitly concerns migration.
- Do not weaken tests to make a refactor pass.
- Do not perform unrelated formatting churn.
- Each worker owns only its assigned branch and task.
- Each worker finishes with a commit SHA, changed-file list, validation results, risks, and integration notes.
- Integration into `dev` happens only after review and dependency checks.

## Branches and dependency order

| Task | Branch | Depends on | Purpose |
|---|---|---|---|
| P2 | `agent/p2-initialization` | Prompt 1 complete | Remove fake historical sub-mod initializers |
| P3 | `agent/p3-config` | P2 integrated | Unified Tideborne config + in-game config UI |
| P4 | `agent/p4-consolidation` | Prompt 1 complete | Conservative trivial class/file consolidation |
| P5 | `agent/p5-legacy-specimen` | Prompt 1 complete | Isolate mutation-era specimen compatibility |
| P6 | `agent/p6-feature-reownership` | Prompt 1 complete | Move normal gameplay out of compatibility ownership |
| P7 | `agent/p7-resources` | P3, P5, P6 integrated | Unify current resource namespaces |
| P8 | `agent/p8-legacy-ids` | P7 integrated | Centralize remaining historical IDs and enforce isolation |
| P9 | `agent/p9-final-audit` | P2-P8 integrated | Final architecture audit, validation, docs cleanup |

## Safe parallel groups

After Prompt 1 is validated, P2, P4, P5, and P6 may run in parallel on their own branches.

P3 should begin only after P2 has been integrated because config initialization and ModMenu ownership overlap.

P7 waits for P3, P5, and P6. P8 waits for P7. P9 waits for everything.

## Worker bootstrap

Every worker prompt must begin by:

1. Reading this file from `main`.
2. Fetching current `dev`.
3. Verifying Prompt 1 is complete: no temporary Astra transport/workflow files remain and active production Java no longer uses the historical top-level Java packages or `fishing.v2`.
4. Fast-forwarding only its assigned worker branch to current validated `dev` if needed.
5. Confirming all declared dependencies are integrated before editing.
6. Working only on its assigned branch.

Do not edit `main` or `dev` from a worker chat.

## Integration policy

The coordinator reviews worker commits, checks overlap, then integrates in dependency order. Do not integrate two workers that modified the same ownership boundary without reviewing the diff. Run targeted validation after each integration and the full matrix at P9.

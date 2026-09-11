# P8 Continuation Handoff

Branch: `agent/p8-legacy-ids`
Baseline: `94052f905b7c3bda33e9f1b0b3988ebf93dd327f`

This file is intentionally short. It exists so a fresh worker chat can continue P8 without rereading the repository.

## Resume sequence

1. Verify you are on `agent/p8-legacy-ids`.
2. Run `python3 scripts/p8_agent.py report`.
3. Read `.github/TIDEBORNE_AGENT_STATE.json`.
4. Read `.github/TIDEBORNE_P8_CONTEXT.md` and `.github/TIDEBORNE_P8_ID_LEDGER.md` only as needed for the current phase.
5. Execute the state's exact `next_action`.

Do not restart P8.0 if the state says a later phase is active.

## Safety

Never modify `main`, `dev`, or another worker branch. Never force-update. Preserve all historical external ID values unless a compatibility bridge is explicitly proven and required.

P7 resource alias decisions are upstream facts for P8, not open questions. Do not move/delete P7 compatibility resources just to reduce historical namespace counts.

## Tool-limit recovery

When remaining tool budget is low:
- stop broad searching;
- finish the smallest coherent edit;
- run the narrowest relevant test;
- commit the product change;
- run `python3 scripts/p8_agent.py checkpoint ...` with an exact next action;
- leave no uncommitted work.

The checkpoint commit updates only coordination state/handoff. `last_product_checkpoint_sha` points to the safe product commit immediately before it.

## Completion

P8 is complete only when every remaining production historical namespace occurrence is explicitly classified, compatibility ID values remain stable, normal tests/build/artifact validation pass, and the state is checkpointed COMPLETE.

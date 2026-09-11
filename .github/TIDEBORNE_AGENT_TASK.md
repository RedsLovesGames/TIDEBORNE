# Tideborne Agent Task — P9 Final Architecture Audit

Repository: `https://github.com/RedsLovesGames/TIDEBORNE`
Assigned branch: `agent/p9-final-audit`
Model: GPT-5.6 Sol High

Work ONLY on this branch. Never modify, push, merge, rebase, retarget, or force-update `main`, `dev`, or another worker branch.

## Start / continuation fast path

1. Read `.github/TIDEBORNE_AGENT_PROMPTS.md` from `main` once.
2. Run `python3 scripts/p9_agent.py report`.
3. Read only:
   - `.github/TIDEBORNE_AGENT_STATE.json`
   - `.github/TIDEBORNE_AGENT_HANDOFF.md`
   - `.github/TIDEBORNE_P9_CONTEXT.md`
   - `.github/TIDEBORNE_P9_AUDIT_LEDGER.md`
4. Follow `next_action` exactly. Do not restart the full audit in a continuation chat.
5. Pull P7/P8 ledgers or subsystem docs only when the active phase needs their evidence.

If chat/tool budget is getting low: stop broad exploration, finish the smallest coherent edit, validate it, commit the product change, then checkpoint so another chat can resume exactly.

## Dependency gate

All architecture workers P1–P8, including the replacement P4 consolidation worker, are integrated into the baseline.

P9 baseline: `575f7525eb15988a6567e3942627a9021c756b60`

The baseline has already passed:
- repository validation;
- Java 21 production compilation on the agent quality gate;
- PMD + CPD + Semgrep delta analysis;
- Qodana JVM delta analysis;
- the combined Java/AI quality gate;
- the normal `Build Tideborne` workflow including tests and release-artifact validation.

Before product edits, verify this branch still descends from that baseline and `dev` has not moved to an unreviewed architecture baseline. If the dependency baseline has materially diverged, STOP and report it. Never force-update.

## Goal

Perform the FINAL architecture audit of Tideborne after P1–P8. Prove the intended architecture is actually true in the integrated tree, fix only concrete defects found by evidence, reconcile current documentation/tooling with reality, and produce a release-quality closure report.

P9 is NOT permission for another redesign. Do not rename packages, rewrite subsystems, collapse classes, or migrate IDs merely because another arrangement looks cleaner. Prefer verification over churn and minimum diffs over speculative cleanup.

## Preserve exactly

Do not change behavior or external identity without a proven bug and a compatibility-safe reason. Preserve:
- registry/item/entity/recipe/tag IDs;
- component IDs, NBT/save keys and persistent-state IDs;
- payload IDs, wire fields and networking semantics;
- old-world/item/config migration behavior;
- Fabric historical dependency aliases and P7 resource aliases;
- specimen identity, BodyType, Condition, Pigmentation, SpecimenQuality;
- FishScore, Trait Momentum, RNG behavior/order;
- commands;
- Satchel persistence and item conservation;
- Journal/history/team/Top Fish persistence;
- fishing gear, Tide bait and balance semantics;
- optional Apex/Myths behavior and absence safety;
- client/server ownership and server-authoritative specimen generation.

## Final audit responsibilities

Verify with evidence:
- package and ownership architecture;
- only genuine Tideborne Fabric entrypoints initialize the mod;
- unified Tideborne config ownership and UI;
- canonical specimen model vs explicit legacy/migration boundaries;
- fishing gear / Tide / ecosystem ownership;
- optional compatibility ownership and absence-safe linkage;
- Satchel consolidation has no stale symbol/reference leaks;
- networking and persistence ownership;
- P7 canonical resources and documented compatibility aliases;
- P8 historical-ID owners and exact preserved values;
- mixin responsibilities and client/server boundaries;
- architecture/source tests are aligned with current ownership;
- `docs/ARCHITECTURE.md`, `docs/CURRENT_STATE.md`, `docs/TODO.md`, and related current docs describe the actual tree;
- worker/checkpoint tooling left in the repository is either still useful or explicitly retired without deleting useful audit evidence.

## Known audit hotspots — inspect, do not assume

1. Normal runtime references to `fishing.specimen.legacy` must be classified. A read-only compatibility bridge may be valid; normal gameplay depending on mutation-era authority is not.
2. The top-level `tideborne.backend` package exists in the integrated tree. Determine whether it is legitimate application/composition ownership or stale architecture residue before changing anything.
3. P4 folded Satchel sorting helpers into `SatchelSorting`. Prove there are no stale references to `SatchelSorter`, `SatchelSortDescriptor`, `SatchelTraitSortData`, or `TideSatchelSortMetadataResolver`.
4. Current docs may still contain historical Java package/class prose. Distinguish intentionally historical evidence from stale current-architecture claims.
5. P7/P8 checkpoint/context files are historical execution evidence. Do not delete them just to reduce file count; classify them during the tooling/docs phase.

## Resumable phase plan

### P9.0 — Dependency gate + baseline inventory
Verify ancestry, CI/build/artifact baseline, top-level package map, current entrypoints, and audit-ledger scope. Do not redesign.

### P9.1 — Ownership / entrypoint / config audit
Verify package ownership, application/composition boundaries, initialization, config storage/network/UI ownership, and classify `backend`.

### P9.2 — Legacy / resource / historical-ID boundary audit
Verify P5 canonical-vs-legacy specimen dependency direction, P7 resource aliases, P8 historical ID owners, and exact compatibility boundaries. Classify every normal-runtime legacy import.

### P9.3 — Runtime integration boundary audit
Verify fishing gear/Tide/ecosystem/optional-compat ownership, client/server boundaries, networking/persistence, mixins, and the post-P4 Satchel consolidation. Fix only proven defects.

### P9.4 — Minimal cleanup + docs/tooling consistency
Remove or update only proven stale current docs, misleading comments, dead scaffolding, or obsolete worker residue. Keep historical evidence clearly labeled. No broad formatting churn.

### P9.5 — Full validation + release artifact
Run repository validation, Java 21 clean build/tests, architecture tests, Java/AI gate, compatibility checks, and release artifact validation. GameTests/Minecraft boot are not required gates under current policy.

### P9.6 — Final closure
Every ledger row must be PASS, FIXED, or ACCEPTED_EXCEPTION with evidence. Produce final architecture/validation report and exact integration notes.

## Checkpoint discipline

Product work and checkpoint metadata are separate commits.

After a coherent product commit:

`python3 scripts/p9_agent.py checkpoint --phase P9.X --completed --next "<exact next action>" --note "<important result>"`

Add validation snapshots with repeated:

`--validation key=value`

For blockers, commit any safe coherent work first, then checkpoint with:

`--status BLOCKED --blocker "<exact blocker>" --next "<exact recovery action>"`

For final closure use `--complete`.

The checkpoint tool intentionally refuses dirty worktrees.

## Required final validation

Before COMPLETE:
1. `scripts/validate_repository.sh` passes.
2. Java 21 clean Gradle build passes.
3. Normal unit + architecture tests pass and test count is reported.
4. Java/AI quality gate passes: repository+compile, PMD/CPD/Semgrep, Qodana, combined gate.
5. Historical-ID ownership regression checks pass.
6. P7 compatibility aliases remain valid.
7. Optional integration/classloading boundaries are safe where relevant.
8. `scripts/validate_release_artifact.sh` passes.
9. Production release artifact is produced and SHA-256 recorded.
10. Final audit ledger contains no unexplained PENDING/BLOCKED item.

Do not weaken tests or quality rules to make P9 green.

## Completion report

Return:
1. final branch SHA;
2. every architecture area audited and disposition;
3. concrete defects found/fixed;
4. accepted exceptions and why they are safe;
5. package/entrypoint/config findings;
6. legacy/resource/ID compatibility findings;
7. runtime/client/server/optional-mod findings;
8. docs/tooling cleanup performed;
9. repository/build/test/quality-gate results;
10. test count;
11. release artifact filename + SHA-256;
12. remaining risks, if any;
13. exact fast-forward integration notes for `dev`.

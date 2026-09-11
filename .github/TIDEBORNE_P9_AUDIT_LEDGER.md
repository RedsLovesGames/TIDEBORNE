# P9 Final Architecture Audit Ledger

Baseline: `575f7525eb15988a6567e3942627a9021c756b60`

Allowed final statuses: `PASS`, `FIXED`, `ACCEPTED_EXCEPTION`. `PENDING` and `BLOCKED` are not allowed at P9 completion.

Do not mark a row PASS from intention or a previous prompt. Record current-tree evidence: files/tests/commands/CI results.

| Area | Status | Evidence / required proof |
|---|---|---|
| P1–P8 + P4 integration ancestry | PASS | Baseline created after all worker integration; re-verify ancestry in P9.0. |
| Baseline repository + Java 21 compile | PASS | Post-P4 agent compile gate green before P9 baseline. |
| Baseline PMD/CPD/Semgrep/Qodana | PASS | Post-P4 Java/AI quality gate green before P9 baseline. |
| Baseline normal build + release artifact validation | PASS | `dev` Build Tideborne run green on baseline. |
| Production package ownership | PENDING | No forbidden historical Java roots or `fishing.v2`; every current top-level package has a coherent owner. |
| `backend` package classification | PENDING | Prove composition/application ownership or fix proven residue. |
| Fabric entrypoints + initialization | PENDING | Only genuine Tideborne entrypoints; no historical sub-mod bootstrap behavior. |
| Unified config/store/network/UI | PENDING | One coherent server/client config architecture; legacy configs migration-only. |
| Canonical specimen authority | PENDING | Current specimen generation/storage/score paths use canonical model. |
| Legacy specimen dependency direction | PENDING | Every normal-runtime `fishing.specimen.legacy` reference classified; no legacy authority over current gameplay. |
| Fishing gear ownership | PENDING | Gear rules under canonical fishing ownership; no duplicate specimen/score authority. |
| Tide adapter ownership | PENDING | Tide hooks/adapters are thin and feature-owned. |
| Ecosystem ownership | PENDING | Chum/sharks/scent behavior under ecosystem, not optional compatibility. |
| Optional Apex/Myths boundaries | PENDING | Optional integration stays under compat and is absence-safe. |
| Satchel consolidation | PENDING | No stale P4 symbols; storage/sort/protection semantics preserved. |
| Journal/history/team/Top Fish | PENDING | Persistence and canonical specimen/score consumption preserved. |
| Networking ownership | PENDING | Payload IDs/semantics preserved; no historical ownership leak. |
| Persistence/migration ownership | PENDING | Save/NBT/component/state identities preserved; migration explicit. |
| Client/server authority | PENDING | Client does not author canonical specimen/server state; dedicated-server classloading safe by static evidence/tests. |
| Mixins | PENDING | Thin lifecycle/version adapters; no duplicate gameplay authority. |
| P7 canonical resources | PENDING | Current resources Tideborne-owned. |
| P7 compatibility aliases | PENDING | Intentional historical alias trees remain exact and documented. |
| P8 legacy ID owners | PENDING | Historical IDs central, exact values preserved, no unexplained literals. |
| Architecture/source regression tests | PENDING | Tests protect final ownership boundaries and exact compatibility contracts. |
| Current architecture docs | PENDING | `ARCHITECTURE`, `CURRENT_STATE`, `TODO`, index/current docs match actual tree. |
| Historical docs separation | PENDING | Historical/reconstruction material is clearly non-authoritative where needed. |
| P7/P8/P9 agent tooling | PENDING | Useful provenance retained; obsolete execution-only residue retired only with evidence. |
| Java/AI quality system | PENDING | Compile + PMD/CPD/Semgrep + Qodana + aggregate gate operate on P9 changes. |
| Final Java 21 clean build/tests | PENDING | Record command/CI evidence and test count. |
| Final release artifact | PENDING | Validator passes; record artifact filename + SHA-256. |
| Final diff scope | PENDING | No speculative redesign, unrelated formatting churn, or accidental compatibility changes. |

## Accepted-exception rule

An `ACCEPTED_EXCEPTION` must name the exact file/symbol/value, explain why changing it is riskier or incorrect, and point to the compatibility/architecture evidence that makes retention intentional.

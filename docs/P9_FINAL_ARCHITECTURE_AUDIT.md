# Tideborne P9 Final Architecture Audit

Date: 2026-09-11

Baseline: `575f7525eb15988a6567e3942627a9021c756b60`
Branch: `agent/p9-final-audit`

## Disposition

P9 completed as a verification-first audit. No production Java or runtime resource defect was found that justified a product-code change. The integrated P1-P8 runtime architecture is accepted as release-quality under the current validation policy.

The P9 branch changes only audit/checkpoint tooling and current documentation. Registry IDs, component/NBT/save identities, payloads, commands, specimen behavior, FishScore, RNG, Satchel state/conservation, Journal/team/Top Fish persistence, fishing balance, and optional-mod behavior remain unchanged from the validated baseline.

## Architecture findings

- Active production Java is unified under `com.redslovesgames.tideborne`; the P9 inventory found 291 production Java files, 90 test Java files, 17 coherent top-level production package directories, zero forbidden historical Java roots, and zero stale P4 Satchel symbols.
- `Tideborne` and `TideborneClient` are the genuine Fabric initializers. Historical mod identities remain only as compatibility aliases through `fabric.mod.json` `provides`.
- `backend/TideborneBackend` is a thin composition facade over discovery/journal services, not a gameplay or persistence owner.
- Config ownership is canonical through `TideborneConfig`, `TideborneClientConfig`, `TideborneConfigStore`, `TideborneConfigNetworking`, `TideborneConfigScreen`, and `TideborneModMenu`. Historical config files are one-way migration inputs; the canonical store writes `tideborne.json`.
- Current specimen authority is `fishing.specimen.SpecimenData` plus the canonical generator/score services. Mutation-era classes are isolated under `fishing.specimen.legacy` and constrained by architecture tests to migration, compatibility, config, or admin-facing use.
- Fishing equipment is owned under `fishing.gear`, Tide adapters under `fishing.tide`, and chum/shark/scent behavior under `ecosystem`. Optional Apex ownership remains under `compat.apex`.
- P7 resource aliases and P8 historical IDs remain exact, centralized, documented compatibility boundaries rather than duplicate current ownership.
- Current mixins remain lifecycle/version adapters. Optional Apex mixins are isolated in a `required: false` config and are gated before optional classes link.
- Satchel sorting is consolidated under `SatchelSorting`, with no stale `SatchelSorter`, `SatchelSortDescriptor`, `SatchelTraitSortData`, or `TideSatchelSortMetadataResolver` references.
- Journal storage and networking consume persisted canonical specimen/score data without recalculation or client synthesis of server identity.

## Compatibility boundaries accepted intentionally

Historical resource namespaces, Fabric dependency aliases, serialized identifiers, migration readers, and the explicit legacy specimen package remain because removing or renaming them would risk old-world/item/config/resource compatibility.

`OptionalCompatMixinPlugin` retains one dormant `.myths.` branch containing the obsolete `myths_of_the_sea` ID. No current Myths mixin is configured, so the branch is unreachable and has no runtime effect; live Myths activation uses `mythishmobs`. P9 records this as an accepted non-blocking exception rather than changing production Java for cosmetic cleanup.

## Regression evidence

Architecture/source tests directly protect the final boundaries, including `LegacySpecimenIsolationArchitectureTest`, `FeatureCompatibilityOwnershipArchitectureTest`, `HistoricalIdOwnershipTest`, and `MixinResourceOwnershipTest`. Satchel tests protect deterministic sorting and state behavior. Journal tests protect exact canonical storage, record ordering, score persistence, legacy preservation, display-only network projection, and server-only field exclusion.

## Validation

The production source/resource tree remains exactly the P9 baseline, so the baseline CI evidence is the final runtime validation evidence:

- `Tideborne Java + AI Quality Gate` run `34608026331`: repository validation, Java 21 production compilation, PMD, CPD, Semgrep, Qodana JVM delta, and aggregate quality gate all passed.
- `Build Tideborne` run `34607809854`: clean Java 21 build and normal tests passed; 370 unit tests completed with zero reported failures.
- Repository validation reported 256 top-level types and 18 GameTest classes.
- Release artifact validation passed for `tideborne-2.0.1.jar`.
- Artifact SHA-256: `c6a40549dcbbb5d0886cccd31061cc24084ffe615ff26b5206db2682abf64c61`.

P9 deliberately does not claim a new runtime build for documentation-only commits. Instead, it preserves the exact already-built production source/resource baseline and verifies the final branch diff contains no runtime changes.

## Remaining risks

The remaining risks are manual or future-policy items already tracked in `docs/TODO.md`: human Satchel/fishing UI validation, full seven-archetype balance sign-off including the default boss roster, future dependency/version upgrades, repository-level immutable releases, and the long-term source-distribution license decision. None is a P9 architecture blocker.

## Integration

Before integration, verify `dev` is still `575f7525eb15988a6567e3942627a9021c756b60` or otherwise review any intervening commits. If it is unchanged, `agent/p9-final-audit` remains a direct descendant and can be fast-forwarded into `dev` without merge/rebase/force-update. P9 must never retarget or modify `main`.

The detailed row-by-row proof is in `.github/TIDEBORNE_P9_AUDIT_LEDGER.md`.

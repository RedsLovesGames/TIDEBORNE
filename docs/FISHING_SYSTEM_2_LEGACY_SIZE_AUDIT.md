# Fishing System 2.0 legacy size/percentile audit

Stage 50 audited the reconstructed Tide Traits compatibility path against the canonical V2 runtime.

## Active catch path

The remaining reconstructed hook is:

`TideFishingHookMixin -> CatchTraitService.individualizeNewCatches -> assignIfAbsent`

Canonical V2 fish already carry `SPECIMEN_SCHEMA_VERSION` and `SPECIMEN_SPECIES_ID` before that hook runs. `assignIfAbsent` therefore treats canonical fish as compatibility-mirror reads only and does not generate size, percentile, Body Type, or Condition.

## Removed generation behavior

The following Tideborne 1.x behaviors are no longer part of any new-catch path:

- fallback `FishData#getRandomLength` second length roll
- legacy `SpecimenSizeService.applyNew` percentile/size generation
- `TraitAxesRuntime.normalizeNew`, including the P97 Giant and P3 Dwarf gates, legacy Perfect Specimen percentile gate, and legacy body-size multiplier
- `PerfectCatchTraitBoost` percentile rewriting, Giant/Dwarf gates, and post-fight length recomputation

`PerfectCatchTraitBoost.apply` remains as a no-op compatibility API because reconstructed callers may still invoke it. Perfect Catch rewards are owned only by the canonical V2 two-phase specimen generator.

## Retained compatibility and migration

Old-world migration remains supported. `CatchTraitService.migrateExistingClassification` may classify an already-persisted legacy physical length when a saved legacy specimen lacks a percentile. It does not sample a new fish length and canonical V2 specimens cannot enter that branch.

Legacy percentile baselines and physical-effect readers remain available where needed for migration, old saved data, and compatibility display/edit behavior. Every canonical V2 physical-effect helper returns without applying legacy multipliers.

## Canonical invariant

For new V2 catches there is exactly one natural percentile/base-length sample in `SpecimenGenerator.generateBase`. Body Type deterministically derives the one final physical size from that base length, and `finalPercentile` is derived from the final size through the canonical species distribution rather than sampled independently.

Runtime GameTests cover canonical immunity to legacy hooks, absence of new legacy size/percentile generation, and successful old-world migration.

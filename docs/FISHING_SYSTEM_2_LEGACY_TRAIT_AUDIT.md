# Fishing System 2.0 legacy trait generation audit

Stage 51 audited the active Tide catch hooks against the canonical Fishing System 2.0 specimen pipeline.

## Active new-catch ownership

`TideSpeciesSelectionBridge` is the server-authoritative new-catch path. It selects the Tide species with the V2 selector, creates the pre-fight specimen with `SpecimenGenerator`, and persists that canonical specimen before the legacy TideTraits catch hooks run. Post-fight Condition, Pigmentation, Specimen Quality, and Perfect Catch state are finalized on the same canonical specimen.

The retained TideTraits hooks (`TideFishingHookMixin` and `TidePlayerDataMixin`) therefore serve compatibility and stack-splitting only. They do not create traits or specimen identity for fresh catches.

## Removed/neutered generation

The legacy `MutationSelector` probability walk is removed. `CatchTraitService` no longer:

- rolls a mutually exclusive legacy mutation,
- creates Giant or Dwarf as mutations,
- rolls Scarred or Parasite-Ridden,
- rolls Albino or Iridescent,
- rolls Perfect Specimen,
- invents a legacy mutation seed for a fresh or incomplete compatibility stack,
- consumes the Tide hook RNG while handling canonical V2 or compatibility-only stacks.

A fresh noncanonical fish passed through the retained compatibility hook is left without generated legacy mutation, seed, size percentile, or length. Multi-count compatibility stacks may still be split into one-item stacks, but splitting does not assign traits or consume RNG.

## Retained compatibility and migration

Legacy trait names remain parseable because old worlds may contain them. `FishMutation`, `TraitAxesRuntime.migrateLegacy`, `LegacyItemStackMigration`, and `LegacyFishMigrationService` retain deterministic parsing/mapping of persisted legacy values, including Giant, Dwarf, Scarred, Parasite/Parasite-Ridden, Albino, Iridescent, and Perfect Specimen.

Already-saved physical lengths may still be classified to a percentile for migration when a persisted legacy identity seed is available. Missing migration identity can be derived deterministically by the canonical migration service where that migration contract permits it; the active catch hook never generates a random replacement seed.

## Canonical invariant

For all new catches, Body Type, Condition, Pigmentation, and Specimen Quality are generated only by the Fishing System 2.0 axis services from the canonical specimen seed and named RNG salts. The legacy compatibility layer has no independent trait RNG path.

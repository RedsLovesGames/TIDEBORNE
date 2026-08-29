# Stage 39 Tide species profile audit

Stage 39 audits only the native Tide `FishData` to Fishing System 2.0 `SpeciesProfile` boundary.

## Authoritative source

The runtime adapter remains data-driven. `TideSpeciesSelectionBridge` iterates the resolved values in Tide's `TideData.FISH` registry and passes those authoritative `FishData` records through `TideSpeciesProfileAdapter`. No Tide fish IDs are copied into a Tideborne table and no new fish are invented by this stage.

The project targets the exact Tide 2.1.1 Fabric runtime. The audit also checked Tide's `FishData`, `SizeData`, and `MinigameBehavior` contracts to verify the meaning of every field adapted by Tideborne.

## Canonical field mapping

For every resolved Tide fish considered by Tide fishing:

- species ID is the namespaced registry ID of `FishData.fish()`;
- rarity is the canonical rarity mapped from Tide journal/profile rarity stars;
- base species weight is Tide `selection_weight` / `FishData.weight()`;
- runtime encounter weight starts from that base weight and preserves Tide fishing modifiers plus Tideborne compatibility fish-weight modifiers;
- environment restrictions remain authoritative in Tide through `FishData.shouldKeep(context)` before a candidate profile is created;
- Strength maps directly from `FishData.strength()`;
- Tempo maps directly from Tide `FishData.speed()`;
- behavior follows Tide's codec contract exactly: `MinigameBehavior.name()` lowercased with `Locale.ROOT`, preserving serialized IDs such as `linear_wrap` without depending on a Mojang/Yarn mapping-sensitive interface method;
- physical size uses Tide `typical_low_cm` and `typical_high_cm` as the P10 and P90 anchors of the canonical log-normal distribution;
- fish with no Tide physical size use `NoPhysicalSizeDistribution`;
- bucket, display, journal, parent, and other Tide-only metadata remain attached to the authoritative `FishData` carried by `TideSpeciesProfileAdapter.Candidate` rather than being duplicated into `SpeciesProfile`;
- the runtime bridge continues to use Tide bucket metadata when marking living catches bucketable and does not regenerate specimen data for display/bucket representation.

`SpeciesEligibility.always()` on the constructed profile is intentional. The profile is already context-normalized: a candidate is constructed only after Tide's complete `shouldKeep(context)` condition set accepts the current cast. This preserves biome, dimension, depth, medium, weather, time, season, bait, and other Tide-defined fishing restrictions without reimplementing those rules in Tideborne.

## Size fitting

Tide defines `typical_low_cm` and `typical_high_cm` around the middle 80 percent of its fitted log-normal distribution. The canonical adapter therefore keeps Tide's standard-normal anchors:

- P10 z = `-1.2815515655446004`
- P90 z = `1.2815515655446004`

The adapter solves log-space sigma and median from those two anchors. Targeted tests require the resulting canonical distribution to return the original Tide low bound at P10 and high bound at P90, and to invert those lengths back to approximately 10 and 90 percentile.

## Fishing Luck ownership

Tide's `selection_quality` is intentionally not folded into `SpeciesProfile.encounterWeight`. Fishing System 2.0 applies Fishing Luck once in `SpeciesSelectionService` from canonical rarity after the Tide adapter has produced its context-normalized base encounter weight.

This keeps the split explicit:

1. Tide `selection_weight` plus non-luck Tide/environment/compatibility weight modifiers define the base encounter weight for the current cast.
2. Canonical `SpeciesSelectionService` applies Fishing Luck once from canonical rarity.
3. Tide `selection_quality` does not create a second Fishing Luck weighting path.

## Corrections made in Stage 39

The audit found two adapter canonicalization defects:

1. migration profiles replaced Tide's authoritative base `selection_weight` with `1.0`; `adaptForMigration` now preserves `FishData.weight()`;
2. behavior used enum `toString()`; the adapter now derives Tide's exact serialized behavior ID from the enum name with `Locale.ROOT` lowercasing.

No other species-field mapping required a behavior change.

## Targeted tests

`TideSpeciesProfileAdapterTest` keeps its pure-JUnit coverage bootstrap-free and now covers:

- the migration encounter-weight seam preserving Tide `selection_weight` directly, with no `selection_quality` input;
- Tide's serialized behavior naming contract, including `LINEAR_WRAP` to `linear_wrap`;
- exact P10/P90 size fitting and percentile inversion.

The direct species ID, rarity, Strength, Tempo, `shouldKeep(context)` eligibility gate, and authoritative Tide metadata retention are structural adapter/bridge mappings that remain runtime-owned. They are validated by the exact Tide 2.1.1 compile/runtime integration path rather than by constructing `FishData` in plain JUnit, because Tide's `FishData` static codec initialization requires Minecraft registry bootstrap state.

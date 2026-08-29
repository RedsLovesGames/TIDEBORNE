# Stage 40 Apex Waters compatibility species profile audit

Stage 40 processes one optional compatibility mod only: Apex Waters 1.1.1 for Fabric 1.21.1.

## Authoritative compatibility surface

The exact Apex Waters artifact already pinned by CI is `apexwaters-1.21.1-fabric-1.1.1.jar`, SHA-256 `00f1c5eaf5b7c2e79a2c64cdeac1a89f2430b2c9ab5f56043f148bde170dba37`.

Upstream Apex Waters 1.1.1 registers exactly this gameplay content relevant to the audit:

- `apexwaters:great_white_shark`, a water-animal entity;
- `apexwaters:great_white_shark_spawn_egg`, an item;
- `apexwaters:great_white_shark_raw`, a food item;
- `apexwaters:great_white_shark_cooked`, a food item.

The upstream 1.1.1 source/resource tree contains no Tide data, no Tide `FishData`, and no fish registration. Tide 2.1.1 also contains no Apex Waters compatibility fish registration. Therefore Apex Waters 1.1.1 contributes zero fish to Tide's fishing selector.

## Canonical SpeciesProfiles

The complete canonical Apex Waters SpeciesProfile mapping is intentionally empty:

| Tide-catchable Apex species | Rarity | Weight | Environment | Size | Strength | Tempo | Behavior |
| --- | --- | --- | --- | --- | --- | --- | --- |
| none | n/a | n/a | n/a | n/a | n/a | n/a | n/a |

The Great White Shark is a spawned entity, not a Tide fishing result. Its spawn egg and meat drops are items, not fish species. Stage 40 therefore does not invent rarity, encounter weight, environmental fishing restrictions, size distribution, Strength, Tempo, or behavior for any of those IDs.

`ApexWatersSpeciesProfileAdapter` makes this result explicit. Its official Tide-catchable species set is empty and it returns no `SpeciesProfile` for the shark entity or Apex item IDs.

## Runtime behavior

No species-selection behavior changes for Apex Waters because there is no official Apex fish candidate to adapt. The generic Tide V2 path remains data-driven and continues to adapt actual `TideData.FISH` records only.

Existing Apex integration remains limited to its real compatibility features such as shark AI, scent, prey interaction, catch-loss behavior, and shark-tooth loot. Those systems do not create fishing species.

## Optional-mod safety

The compatibility species adapter imports no Apex Waters class. It can load when Apex is absent and does not probe optional classes to create profiles.

The exact Apex JAR remains `modCompileOnly` in ordinary builds. Stage 40 adds an opt-in `includeApexRuntime` Gradle property so CI can execute the same Fabric GameTest server twice:

1. ordinary `runGametest`, with Apex Waters absent;
2. `runGametest -PincludeApexRuntime=true`, with the exact Apex Waters 1.1.1 JAR present at runtime.

This preserves the existing absent-mod safety check while adding an explicit present-mod server boot check.

## Targeted tests

`ApexWatersSpeciesProfileAdapterTest` verifies:

- the complete official Tide-catchable Apex species set is empty;
- the Great White Shark entity, spawn egg, raw shark meat, and cooked shark meat are not treated as Tide species;
- no synthetic `SpeciesProfile` is produced for those IDs;
- the adapter API has no direct Apex Waters class dependency;
- malformed species IDs fail closed.

## Stage boundary

This slice processes Apex Waters only. It does not canonicalize Myths of the Sea or any other compatibility mod. The next compatibility mod to audit is Myths of the Sea 1.3.0.

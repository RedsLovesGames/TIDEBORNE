# Fishing System 2.0 Real Tide Balance Report

Status: authoritative real-catalog balance report for Tideborne 2.0.1 development

Generated from commit: `918b5fce07036b888cebb505ffec4faebeb1f0af`

Validation workflow: GitHub Actions run `33832555397`

Runtime target:

- Minecraft 1.21.1
- Tide 2.1.1
- Tide Modrinth version `ztNu3GtG`
- Tide JAR SHA-256 `498a5e8dda940866c9b0decadf7960724ef489fb49215b30f70c18d12f07b1c8`
- Tideborne 2.0.1 development line

## Executive summary

The old balance report used a deliberately synthetic nine-fish pool with identical encounter weights. That fixture remains useful for deterministic unit-regression testing, but it is not representative of actual Tide gameplay.

The new simulator reads the live Tide 2.1.1 fish registry at runtime. It found 106 Tide fish and exercised nine representative fishing contexts covering surface freshwater, weather and night fishing, several ocean conditions, underground water, Nether lava, and End void fishing. Those contexts made 61 of the 106 species reachable.

Across the representative location mix, starter fishing is dominated by low-rarity fish:

- 1-star: 55.100%
- 2-star: 31.648%
- 3-star: 11.628%
- 4-star: 1.416%
- 5-star: 0.208%

The median starter FishScore is 642 and the mean is 653.35. Endgame gear raises the mean to 692.06 and the median to 675 while still leaving high-rarity catches uncommon.

Controlled sensitivity runs show the two luck stats are doing different jobs:

- Fishing Luck changes which rarity tiers are encountered. At Fishing Luck 18 with Trait Luck fixed at 0, 1-star share falls from 55.027% to 47.027%, 4-star share rises from 1.140% to 1.960%, and mean FishScore rises by about 4.53%.
- Trait Luck does not change the rarity distribution. At Trait Luck 6 with Fishing Luck fixed at 0, any-trait frequency rises from 15.120% to 21.927%, Perfect Specimen frequency rises from 1.507% to 2.127%, and mean FishScore rises by about 1.64%.

For the modeled progression stages, the median first 5-star catch improves from 400 catches at starter gear to 117 catches at the endgame profile. Median progress to 90% of the species reachable in the representative matrix improves from 327 to 257 catches.

## What the real simulator replaces

`docs/FISHING_SYSTEM_2_BALANCE_REPORT.md` was based on a synthetic equal-weight fixture. It intentionally used a tiny pool so deterministic simulator behavior could be checked without depending on external Tide data.

The real balance path is now:

1. read every live Tide `FishData` entry from `TideData.FISH`;
2. construct a real Tide `FishingContext` for a representative location;
3. call the same Tide eligibility path used by runtime fishing, including `FishData.shouldKeep(context)`;
4. apply Tide context-dependent fishing modifiers and obtain the effective encounter weight;
5. adapt Tide rarity, size distribution, strength, speed, and other canonical profile metadata through `TideSpeciesProfileAdapter`;
6. select species through Tideborne's production `SpeciesSelectionService`;
7. generate specimens through the production `SpecimenGenerator`;
8. calculate canonical FishScore through the normal Fishing System 2.0 path.

The simulator therefore does not maintain a duplicate hand-authored Tide fish catalog.

## Representative fishing locations

Each location is sampled with equal probability in the distribution simulations. The percentages in this report therefore describe this representative scenario mix, not the physical biome-area distribution of a randomly generated Minecraft world.

| Scenario | Tide context | Eligible species |
| --- | --- | ---: |
| `river_day_clear` | River, surface water, clear day | 6 |
| `swamp_night_rain` | Swamp, surface water, rainy night | 7 |
| `warm_ocean_day` | Warm ocean, surface water, clear day | 6 |
| `deep_ocean_night` | Deep ocean, deeper water, clear night | 7 |
| `frozen_ocean_day` | Frozen ocean, surface water, clear day | 8 |
| `lush_cave` | Lush cave, underground water | 12 |
| `dripstone_cave` | Dripstone cave, underground water | 13 |
| `nether_lava` | Nether Wastes, lava fishing | 11 |
| `end_void` | The End, void fishing | 14 |

The nine scenarios expose 61 unique species out of the 106-species Tide 2.1.1 catalog. The remaining 45 species require other niche biome, condition, seasonal, spatial, or special fishing contexts and are not treated as globally unavailable.

The location model varies the Tide context fields that determine eligibility and encounter modifiers, including:

- exact biome and nearest biome
- dimension
- fishing medium
- depth / Y position
- temperature
- moon phase
- world time
- rain state
- Tide season from the live runtime context

## Gear progression profiles

Perfect Catch probability is fixed at 15% for every gear profile. This isolates gear value from player minigame skill.

| Stage | Fishing Luck | Trait Luck | Representative setup |
| --- | ---: | ---: | --- |
| Starter | 0 | 0 | No luck bonuses |
| Midgame Amethyst | 3 | 1 | Luck of the Sea III plus Amethyst Bobber |
| Lategame Echo | 3 | 2 | Luck of the Sea III plus Echo Bobber |
| Endgame Leviathan | 18 | 6 | Luck of the Sea III plus Echo Bobber plus Leviathan Bait |

The Fishing System 2.0 gear values used here match the production modifiers:

- Amethyst Bobber: +1 Trait Luck
- Echo Bobber: +2 Trait Luck
- Leviathan Bait: +15 Fishing Luck and +4 Trait Luck

Strength and tempo modifiers still matter to actual fight difficulty and elapsed time, but this report measures progression primarily in catches. It does not convert those fight modifiers into real-time minutes or catches per hour.

## Real rarity distribution

Each progression profile used 25,000 simulated catches.

| Gear | 1-star | 2-star | 3-star | 4-star | 5-star |
| --- | ---: | ---: | ---: | ---: | ---: |
| Starter | 55.100% | 31.648% | 11.628% | 1.416% | 0.208% |
| Midgame Amethyst | 51.272% | 33.024% | 13.796% | 1.532% | 0.376% |
| Lategame Echo | 51.488% | 32.744% | 14.032% | 1.500% | 0.236% |
| Endgame Leviathan | 47.620% | 34.108% | 15.840% | 2.076% | 0.356% |

The small non-monotonic movement in very rare 5-star outcomes between the stage rows is Monte Carlo noise because each stage uses an independent deterministic seed. The controlled same-seed sensitivity runs below are the correct source for measuring the causal value of Fishing Luck.

## FishScore distribution

| Gear | Mean | P50 | P90 | P99 | Minimum | Maximum observed |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Starter | 653.35 | 642 | 1120 | 1549 | 1 | 2195 |
| Midgame Amethyst | 666.55 | 653 | 1138 | 1595 | 1 | 2533 |
| Lategame Echo | 669.57 | 658 | 1145 | 1580 | 1 | 2537 |
| Endgame Leviathan | 692.06 | 675 | 1178 | 1678 | 1 | 2397 |

The observed maxima are not theoretical maxima. They are the highest results in each 25,000-catch Monte Carlo sample and are especially sensitive to rare species and trait combinations.

The real catalog puts the center of the score distribution much lower than the old synthetic equal-rarity fixture because more than half of starter encounters in this location mix are 1-star fish.

## Trait and Perfect Specimen frequency

| Gear | Any non-normal trait | Perfect Specimen | Perfect Catch observed |
| --- | ---: | ---: | ---: |
| Starter | 15.596% | 1.496% | 15.124% |
| Midgame Amethyst | 16.796% | 1.528% | 15.200% |
| Lategame Echo | 17.352% | 1.560% | 14.740% |
| Endgame Leviathan | 22.024% | 2.320% | 15.012% |

Perfect Catch remains near the fixed 15% input rate. Perfect Specimen remains much rarer because a Perfect Catch is only one input into final specimen quality and does not make every catch a Perfect Specimen.

## Controlled value of Fishing Luck

These runs use the same deterministic random seed, 15,000 catches per profile, Trait Luck fixed at 0, and the same 15% Perfect Catch input. This isolates Fishing Luck from Trait Luck.

| Fishing Luck | Mean score | P50 | P90 | P99 | 1-star | 2-star | 3-star | 4-star | 5-star |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 0 | 649.44 | 637 | 1119 | 1539 | 55.027% | 31.413% | 12.193% | 1.140% | 0.227% |
| 3 | 661.90 | 650 | 1135 | 1564 | 51.500% | 33.000% | 13.720% | 1.547% | 0.233% |
| 18 | 678.87 | 666 | 1155 | 1631 | 47.027% | 34.673% | 15.940% | 1.960% | 0.400% |

From Fishing Luck 0 to 3:

- mean FishScore: +12.46, about +1.92%
- median FishScore: +13
- P99 FishScore: +25
- 1-star share: -3.527 percentage points
- 3-star share: +1.527 percentage points
- 4-star share: +0.407 percentage points

From Fishing Luck 0 to 18:

- mean FishScore: +29.43, about +4.53%
- median FishScore: +29
- P99 FishScore: +92
- 1-star share: -8.000 percentage points
- 2-star share: +3.260 percentage points
- 3-star share: +3.747 percentage points
- 4-star share: +0.820 percentage points
- 5-star share: +0.173 percentage points

Trait frequency remains effectively unchanged in these Fishing Luck-only runs. This confirms Fishing Luck is acting primarily as encounter-quality progression rather than trait progression.

## Controlled value of Trait Luck

These runs use the same deterministic random seed, 15,000 catches per profile, Fishing Luck fixed at 0, and the same 15% Perfect Catch input.

| Trait Luck | Mean score | P50 | P90 | P99 | Any non-normal trait | Perfect Specimen |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 0 | 649.44 | 637 | 1119 | 1539 | 15.120% | 1.507% |
| 1 | 651.38 | 638 | 1124 | 1547 | 16.387% | 1.647% |
| 2 | 653.20 | 639 | 1128 | 1562 | 17.500% | 1.740% |
| 6 | 660.07 | 642 | 1141 | 1603 | 21.927% | 2.127% |

All four Trait Luck-only profiles produced exactly the same rarity shares in the controlled run:

- 1-star: 55.027%
- 2-star: 31.413%
- 3-star: 12.193%
- 4-star: 1.140%
- 5-star: 0.227%

From Trait Luck 0 to 6:

- any-trait frequency: +6.807 percentage points
- Perfect Specimen frequency: +0.620 percentage points
- mean FishScore: +10.63, about +1.64%
- species rarity distribution: unchanged

This is a strong separation of roles. Trait Luck makes unusual and high-quality specimens more common without turning it into a second rarity-farming stat.

## Progression speed

Progression trials cycle through the nine representative locations so players are assumed to travel instead of remaining in one fishing spot. Each stage uses 40 trials with a cap of 2,000 catches per trial.

The completion percentages refer to the 61 species reachable in this representative location matrix, not all 106 Tide species.

| Gear | 25% discovered | 50% | 75% | 90% | First 4-star | First 5-star | First Perfect Specimen | First FishScore 2000+ |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Starter | 20 | 53 | 146 | 327 | 45 | 400 | 48 | 914 |
| Midgame Amethyst | 18 | 52 | 131 | 299 | 36 | 396 | 32 | 804 |
| Lategame Echo | 20 | 53 | 134 | 264 | 30 | 270 | 61 | 692 |
| Endgame Leviathan | 18 | 52 | 125 | 257 | 27 | 117 | 27 | 516 |

Starter to endgame directional change:

- 25% discovery: 20 to 18 catches, 10% fewer
- 50% discovery: 53 to 52 catches, about 1.9% fewer
- 75% discovery: 146 to 125 catches, about 14.4% fewer
- 90% discovery: 327 to 257 catches, about 21.4% fewer
- first 4-star: 45 to 27 catches, 40% fewer
- first 5-star: 400 to 117 catches, about 70.8% fewer
- first Perfect Specimen: 48 to 27 catches, about 43.8% fewer
- first FishScore 2000+: 914 to 516 catches, about 43.5% fewer

These progression-stage comparisons combine Fishing Luck and Trait Luck and use independent deterministic seeds by stage, so they should be read as progression-scale estimates rather than precise causal measurements. The same-seed sensitivity sections are the authoritative isolation tests for each luck stat.

## Balance findings

### 1. The real rarity economy is much more common-fish-heavy than the synthetic fixture

The representative starter mix is about 86.7% 1-star or 2-star fish. Only about 1.62% are 4-star or 5-star. This makes rare catches actual milestones instead of equally represented test categories.

### 2. Fishing Luck has meaningful value without deleting rarity

Even the aggressive endgame Fishing Luck 18 sensitivity profile still produces 47.0% 1-star fish. Five-star encounters rise from roughly 0.23% to 0.40% in the controlled sample. That is a large relative improvement for an ultra-rare event, but only a small absolute share of catches.

The larger effect is the broad migration from 1-star into 2-star, 3-star, and 4-star encounters. This raises score quality and late-game discovery speed while retaining low-rarity fish in the pool.

### 3. Trait Luck is cleanly orthogonal to Fishing Luck

The controlled Trait Luck runs have identical rarity distributions. Trait Luck increases Body Type, Condition, Pigmentation, and Specimen Quality event output without changing which species rarity tier the player catches.

This is desirable for build identity:

- Fishing Luck answers: "How valuable is the species I encounter?"
- Trait Luck answers: "How unusual or exceptional is this individual specimen?"

### 4. Trait Luck meaningfully raises interesting catches, but score inflation is restrained

Trait Luck 6 raises any-trait frequency from 15.12% to 21.93%, a 6.81 percentage point increase. Mean FishScore only rises by about 1.64%. The player sees more notable specimens without receiving a proportional score explosion.

### 5. Endgame progression mostly accelerates the difficult tail

The first half of the representative catalog is discovered at almost the same speed at starter and endgame gear. The large gains appear at 75% to 90% completion and rare-catch milestones.

That progression shape is healthy for collection gameplay because early discovery remains fast, while upgraded gear has increasing value as the player starts chasing rare species and exceptional specimens.

### 6. Five-star frequency needs larger samples for fine tuning

At a few tenths of one percent, 15,000 to 25,000 catches only produce dozens of five-star observations per profile. The direction of Fishing Luck is clear, but small differences between nearby stage profiles should not be tuned from one Monte Carlo seed.

Any future balance change aimed specifically at 5-star frequency should use at least several hundred thousand catches per controlled profile or an exact analytical weight calculation over each context.

## Caveats

This is the real Tide-backed simulator, but it is not an exhaustive model of every possible Minecraft fishing position.

- The source catalog is complete Tide 2.1.1 with 106 fish, but the nine representative scenarios expose 61 unique species.
- Location scenarios are weighted equally. They are not weighted by biome area, player travel behavior, or time spent in each dimension.
- The harness supplies real Tide `FishingContext` fields for biome, dimension, medium, depth, temperature, moon phase, time, weather, and season. Very niche conditions that directly inspect nearby physical blocks or other world state may require dedicated physical GameTest scenes.
- The runtime GameTest is hosted by one test server world while scenario-specific Tide context fields represent other dimensions and media. This is sufficient for conditions driven by the context values, but direct calls against the backing `ServerWorld` can still require dedicated dimension-specific tests.
- Progression is measured in number of catches, not real-world time. Rod strength, line strength, tempo, fish fight duration, hook speed, and failure rate can change catches per hour and are not converted into elapsed-time progression here.
- Perfect Catch is modeled at a fixed 15% success probability for all gear levels. This keeps skill constant and is not a claim that all players have a 15% real success rate.
- Stage rows use independent deterministic seeds. Controlled sensitivity rows use the same seed and should be preferred for stat-value comparisons.
- Maximum observed FishScore and ultra-rare 5-star percentages are Monte Carlo sample observations, not theoretical bounds.
- Optional Apex Waters and Myths of the Sea runtime fish are not included in this base Tide report. The report measures the requested Tide 2.1.1 catalog plus Tideborne Fishing System 2.0.

## Reproducibility

Implementation:

- `src/main/java/com/redslovesgames/tideborne/fishing/v2/simulation/RealTideFishingSimulator.java`
- `src/main/java/com/redslovesgames/tideborne/fishing/v2/gametest/RealTideBalanceGameTests.java`
- `src/main/java/com/redslovesgames/tideborne/fishing/v2/integration/TideSpeciesProfileAdapter.java`
- `src/main/java/com/redslovesgames/tideborne/fishing/v2/integration/TideFishingContextAdapter.java`

Sampling configuration:

- progression distribution catches per gear stage: 25,000
- controlled sensitivity catches per profile: 15,000
- progression trials per gear stage: 40
- progression trial cap: 2,000 catches
- distribution seed: `0x5449444542414C41`
- sensitivity seed: `0x4C55434B56414C55`
- progression seed: `0x5245414C42414C32`

Validation result for commit `918b5fce07036b888cebb505ffec4faebeb1f0af`:

- repository validation passed
- 282 unit tests passed
- 59 required GameTests passed
- real Tide balance GameTest passed
- production Tideborne 2.0.1 JAR validation passed
- CI artifact upload passed
- `publish-release` was skipped because this was a normal `dev` push

The numbers in this document are taken from GitHub Actions run `33832555397` and should be regenerated when Tide's fish catalog, Tideborne's selection formula, luck formula, trait probabilities, specimen generation, FishScore formula, or representative context matrix changes.

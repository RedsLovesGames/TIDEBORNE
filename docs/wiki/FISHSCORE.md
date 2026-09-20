# FishScore

[← Specimens & Traits](SPECIMENS_AND_TRAITS.md) | [Wiki Home](README.md) | [Angler's Satchel →](ANGLERS_SATCHEL.md)

**FishScore V2** gives every canonical Tideborne specimen a public score from **1 to 3000**. The score is designed to compare how exceptional a specimen is without reducing everything to raw physical length.

## What FishScore uses

FishScore combines:

1. Tide species rarity
2. the specimen's **final percentile**
3. Condition
4. Body Type
5. Pigmentation
6. Specimen Quality

Team ownership, personal records, record age, and who caught the fish do not add points to FishScore.

## Raw score formula

```text
RawScore = SpeciesValue
         + 3 × FinalPercentile
         + ConditionValue
         + BodyTypeValue
         + PigmentationValue
         + QualityValue
```

`FinalPercentile` is on the 0-100 scale.

## Species points

| Tide rarity | Points |
| --- | ---: |
| 1 star | 50 |
| 2 star | 100 |
| 3 star | 175 |
| 4 star | 250 |
| 5 star | 350 |

## Trait points

| Category | Result | Points |
| --- | --- | ---: |
| Condition | Normal | 0 |
| Condition | Scarred | 20 |
| Condition | Parasite-Ridden | 35 |
| Body Type | Normal | 0 |
| Body Type | Giant | 40 |
| Body Type | Dwarf | 40 |
| Pigmentation | Normal | 0 |
| Pigmentation | Albino | 70 |
| Pigmentation | Iridescent | 100 |
| Specimen Quality | Normal | 0 |
| Specimen Quality | Perfect Specimen | 100 |

## Mapping raw score to 1-3000

The canonical 2.1.0 implementation maps the raw range linearly onto the public range.

```text
Normalized = (RawScore - 50) / 875
FishScore  = round(1 + 2999 × Normalized)
```

The result is clamped to **1 through 3000**.

The raw minimum is 50. The raw maximum is 925:

```text
350 species
+ 300 percentile
+ 35 condition
+ 40 body type
+ 100 pigmentation
+ 100 quality
= 925 raw
```

That theoretical maximum maps to **3000 FishScore**.

## Example

Suppose a 4-star fish has:

- final percentile: 98
- Giant Body Type
- Scarred Condition
- Iridescent Pigmentation
- normal Specimen Quality

Its raw score is:

```text
250 + (3 × 98) + 40 + 20 + 100 + 0
= 704
```

Public score:

```text
round(1 + 2999 × ((704 - 50) / 875))
≈ 2242
```

So that specimen is approximately **2242 FishScore**.

## Why final percentile matters

The score uses the **final percentile**, not simply the original natural percentile. This lets the physical result of a Giant or Dwarf transformation be reflected in how exceptional the completed specimen is while still preserving the one-roll specimen-generation rule.

## FishScore vs records

A high FishScore and a size record are different achievements.

- **Largest/smallest records** care about physical size for a species.
- **FishScore** evaluates a broader combination of rarity, percentile, and traits.
- A tiny Dwarf can score very well even though it is competing for a smallest record.
- A huge ordinary fish can hold a largest record without being the team's highest FishScore.

## Where FishScore appears

FishScore is used by Tideborne's specimen presentation, team statistics, Team Top Fish/Top 15 data, leaderboards, history, and record-oriented interfaces. Stored canonical scores are reused rather than recalculated independently by every screen.
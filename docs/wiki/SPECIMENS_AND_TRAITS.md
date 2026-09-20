# Specimens & Traits

[← Getting Started](GETTING_STARTED.md) | [Wiki Home](README.md) | [FishScore →](FISHSCORE.md)

Tideborne treats a caught fish as an individual **specimen**, not just an item with a species ID. The server generates one persistent specimen state and reuses it across the rest of Tideborne.

## The specimen axes

The current system uses four independent trait categories.

| Category | Outcomes |
| --- | --- |
| Body Type | Normal, Giant, Dwarf |
| Condition | Normal, Scarred, Parasite-Ridden |
| Pigmentation | Normal, Albino, Iridescent |
| Specimen Quality | Normal, Perfect Specimen |

Because the categories are independent, one catch can carry one result from every category at the same time. A fish can therefore be Giant, Scarred, Iridescent, and a Perfect Specimen in the same specimen.

## Natural size and physical size

Each catch starts with one natural size percentile. Tideborne does not repeatedly reroll the underlying percentile until it finds a result it likes. Body Type is applied afterward as a physical-size transformation.

### Giant

The Body Type event has a base 5% event probability before current rarity, Trait Luck, and other canonical modifiers are applied. If a specimen becomes Giant, its physical length receives a deterministic multiplier between **1.10x and 1.30x**.

### Dwarf

A Dwarf specimen receives a deterministic physical-length multiplier between **0.60x and 0.82x**.

Giant and Dwarf therefore change the final physical fish, but they do not create a second natural-size roll.

## Conditions

**Scarred** and **Parasite-Ridden** are condition traits. They are separate from Body Type and Pigmentation, so a Scarred fish can also be Giant, Albino, Iridescent, or Perfect.

Conditions also contribute to [FishScore](FISHSCORE.md).

## Pigmentation

**Albino** and **Iridescent** are pigmentation traits. Iridescent carries the larger FishScore trait value, but both are uncommon specimen characteristics and remain independent from size.

## Perfect Specimen

Perfect Specimen is a specimen-quality result. It is not the same thing as a **Perfect Catch** in the fishing minigame.

The canonical Perfect Specimen curve strongly favors naturally exceptional fish. Before Trait Luck and Perfect Catch modifiers, the documented V2 curve is approximately:

| Natural percentile | Base Perfect Specimen chance |
| ---: | ---: |
| Below 95 | 0% |
| 95 | 2% |
| 97.5 | 8% |
| 99 | 25% |
| 99.9+ | 60% |

This makes Perfect Specimen a quality signal tied to an exceptional underlying specimen rather than a flat random mutation.

## Perfect Catch

A Perfect Catch is a fishing-performance result. In Fishing System 2.0 it can improve your temporary Trait Luck opportunity, increase the Body Type event chance, and help the Perfect Specimen roll. It does **not** reroll the fish's natural size.

The documented Body Type event multiplier for a Perfect Catch is **1.25x**. The standard Perfect Catch temporary Trait Luck reward is **+10** before other applicable rules.

## Trait Luck

Trait Luck modifies trait-event probabilities using the canonical transformation:

```text
P' = 1 - (1 - P)^(1 + T / 10)
```

Where:

- `P` is the probability before Trait Luck
- `T` is Trait Luck
- `P'` is the adjusted probability

This is deliberately not a simple `P + T%` system. The transformation gives Trait Luck useful scaling without treating every event as if it had the same base odds.

## Species rarity compensation

Rarer species receive stronger trait probability scaling. The V2 rarity factors are:

| Tide rarity | Factor |
| --- | ---: |
| 1 star | 1.00x |
| 2 star | 1.15x |
| 3 star | 1.40x |
| 4 star | 1.80x |
| 5 star | 2.40x |

The compensation is applied through the trait probability service rather than being a separate visible trait.

## Trait Momentum

Trait Momentum rewards repeatedly targeting the same species. Normal catches can build temporary Trait Luck for that species, while a notable trait result resets or reduces the stored momentum according to the canonical rules.

The player-facing V2 design uses roughly **+1 temporary Trait Luck per ordinary catch**, with a practical cap around **+15** for a species. Momentum is server-owned and tracked per player and per species.

## Why the categories are separate

Tideborne deliberately avoids one single "mutation rarity" roll that decides everything. The independent axes make catches more varied and allow different systems to care about different parts of a specimen. Size hunters, trait collectors, FishScore hunters, and team record hunters can all value the same fish differently.

## What is stored

A canonical specimen preserves the information needed for consistent presentation and scoring, including its deterministic identity, size data, trait axes, and score-related state. Stored specimens are reused by Satchels, records, history, Journal views, displays, entities, buckets, and network projections instead of being regenerated from scratch.
# Fishing System 2.0

[← Getting Started](GETTING_STARTED.md) | [Wiki Home](README.md) | [Specimens & Traits →](SPECIMENS_AND_TRAITS.md)

Fishing System 2.0 is Tideborne's canonical catch pipeline. Tide still provides the fish content and fishing activity, while Tideborne makes species selection, specimen generation, score, fight projection, persistence, and records agree on one server-owned catch state.

## Catch pipeline

```text
Fishing context + equipped gear
        ↓
Tide species eligibility
        ↓
Fishing Luck weighted species selection
        ↓
One canonical specimen generation
        ↓
Body Type + Condition + Pigmentation + Quality
        ↓
Final physical size + final percentile
        ↓
FishScore V2
        ↓
Strength / Tempo fight projection
        ↓
Persistence, Satchel, Journal, records, history, networking
```

The order matters. Gear can provide inputs to the pipeline, but it does not directly invent a specimen percentile, FishScore, or identity.

## Species eligibility comes first

Tideborne does not use Fishing Luck to make impossible fish appear. Tide's normal habitat and eligibility rules are resolved before weighted selection. A fish that is not eligible for the current location or conditions is not made valid simply because the player has high Fishing Luck.

This is also why Leviathan Bait can guarantee a Tide fish without ignoring habitats.

## Fishing Luck

Fishing Luck biases the selection toward rarer eligible Tide species. It does not directly increase the chance of a rare specimen trait.

The documented V2 weighting is:

```text
W' = W × (1 + Cᵣ × √max(L, 0))
```

Where:

- `W` = Tide's base weight for the eligible species
- `L` = Fishing Luck
- `Cᵣ` = coefficient based on Tide rarity
- `W'` = adjusted selection weight

Rarity coefficients:

| Tide rarity | Coefficient |
| --- | ---: |
| 1 star | 0.00 |
| 2 star | 0.08 |
| 3 star | 0.16 |
| 4 star | 0.24 |
| 5 star | 0.32 |

The square-root curve gives diminishing returns rather than allowing Fishing Luck to scale species weights linearly forever.

## One specimen roll

Once the species is selected, Tideborne constructs one canonical specimen. The natural percentile is sampled once. Trait generators use deterministic split random streams so Body Type, Condition, Pigmentation, and Specimen Quality remain independent without rerolling the whole fish.

See [Specimens & Traits](SPECIMENS_AND_TRAITS.md) for the trait rules.

## Perfect Catch

A Perfect Catch is a skill reward from the fishing process. It can increase trait opportunity and the Body Type event chance, but it does not replace the fish with a new natural percentile.

The standard V2 behavior includes:

- +10 temporary Trait Luck opportunity
- 1.25x Body Type event multiplier
- assistance on Perfect Specimen generation

## Strength and Tempo

Tideborne describes fight difficulty using two normalized dimensions:

- **Strength**: how demanding the fish is to control
- **Tempo**: how quickly or aggressively the fight moves

Body Type contributes to fight feel. The documented V2 body modifiers are:

| Body Type | Strength | Tempo |
| --- | ---: | ---: |
| Normal | 1.00x | 1.00x |
| Giant | 1.08x | 0.95x |
| Dwarf | 0.92x | 1.08x |

This lets Giants feel heavier while Dwarfs can feel lighter but quicker. Gear and special bait can add their own modifiers through the same canonical fight profile rather than bypassing it.

## Leviathan Bait

With Myths of the Sea installed, Leviathan Bait changes the species-selection and fight stages without bypassing normal habitats. In 2.1.0 it:

- guarantees the result is a Tide fish instead of junk, treasure, or crates
- adds +15 fish-selection Fishing Luck
- provides substantial trait-oriented opportunity
- applies approximately 1.15x Strength and 1.15x Tempo to the catch

See [Fishing Gear & Integrations](FISHING_GEAR_AND_INTEGRATIONS.md) for crafting and optional-mod details.

## Why the server owns the pipeline

The same catch can appear in an ItemStack, fish entity, bucket, Satchel, Journal row, team record, Hall display, and network payload. If every screen recalculated its own version, traits and scores could disagree. Tideborne therefore stores and projects canonical state so presentation reads the same specimen rather than generating a new interpretation.
# Stage 33 canonical gear modifier model

Status: implemented on `dev`

## Scope

Stage 33 adds the canonical Fishing System 2.0 representation for server-owned gear modifiers. It intentionally does not migrate or replace any existing runtime gear behavior yet.

The model is `FishingGearModifiers` in the pure V2 package. It has no client, tooltip, screen, text, or other UI dependency.

## Represented modifier families

The model directly represents:

- additive Fishing Luck
- additive Trait Luck
- Strength multiplier
- Tempo multiplier
- category restrictions
- catch-pool restrictions
- per-Body-Type chance multipliers for `NORMAL`, `GIANT`, and `DWARF`
- other named additive canonical fishing modifiers
- other named multiplicative canonical fishing modifiers

The named modifier maps are canonical machine identifiers, not display labels. Runtime consumers can therefore add later Fishing System 2.0 modifier axes without putting UI concerns into the domain model.

## Composition semantics

Composition is fixed and explicit:

| Modifier family | Composition |
|---|---|
| Fishing Luck | add |
| Trait Luck | add |
| Strength | multiply |
| Tempo | multiply |
| Body Type chance | multiply per Body Type |
| named additive modifier | add per key |
| named multiplier modifier | multiply per key |
| active category allow-lists | intersect |
| active catch-pool allow-lists | intersect |
| category deny-lists | union |
| catch-pool deny-lists | union |

An inactive allow-list means unrestricted. An active empty allow-list intentionally means nothing is allowed, so composing disjoint restrictions cannot accidentally become unrestricted. Deny-lists always win over allow-lists.

## Determinism and immutability

`FishingGearModifiers.compose` uses exact `BigDecimal` accumulation for additive and multiplicative stacks before converting the final values back to `double`. This makes composition independent of the input collection iteration order, including floating-point cases where ordinary left-to-right `double` reduction could differ.

Identifier-keyed maps and sets are copied into canonical sorted collections. Body Type maps use enum order. Public collections are unmodifiable snapshots.

Neutral values are:

- Fishing Luck `0.0`
- Trait Luck `0.0`
- Strength `1.0`
- Tempo `1.0`
- unrestricted category and catch-pool filters
- Body Type chance multiplier `1.0` when an axis has no entry
- named additive modifier `0.0` when a key has no entry
- named multiplier modifier `1.0` when a key has no entry

Finite-value validation is enforced. Multipliers must also be nonnegative.

## Tests

`FishingGearModifiersTest` covers:

- neutral composition identity
- Fishing Luck and Trait Luck addition
- Strength and Tempo multiplication
- Body Type chance stacking
- named additive and multiplicative stacking
- order-independent composition with a floating-point-sensitive input set
- category and catch-pool restriction intersection/denial behavior
- disjoint allow-lists remaining restricted instead of becoming unrestricted
- canonical key ordering
- immutable snapshots
- invalid multiplier and modifier input rejection

## Runtime boundary

No existing item, bait, line, rod, hook, `FishingContext`, species-selection, trait-generation, or minigame path consumes this model in Stage 33. Existing behavior remains unchanged until a later queued stage explicitly migrates gear into the canonical context and fight pipeline.

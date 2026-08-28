# Stage 21: Canonical FishScore V2 pure service

Fishing System 2.0 now has one pure canonical FishScore calculation in `FishScoreV2Service`.

## Frozen calculation

Raw FishScore is the sum of three components:

- species points: 1 star = 50, 2 star = 100, 3 star = 175, 4 star = 250, 5 star = 350
- specimen points: `3 * finalPercentile`
- trait points: Scarred +20, Parasite-Ridden +35, Giant +40, Dwarf +40, Albino +70, Iridescent +100, Perfect Specimen +100

The service reads the canonical `finalPercentile` and the already-generated independent specimen axes. It does not reroll or reinterpret any specimen property.

The frozen raw boundaries are:

- minimum raw score: 50
- maximum compatible raw score: 925

The normalized score is calculated linearly as:

```text
normalized = (rawScore - 50) / (925 - 50)
FishScore = round(1 + 2999 * normalized)
```

The final integer is clamped to `1..3000`.

This makes the minimum canonical one-star P0 all-normal specimen score exactly 1 and the maximum five-star P100 specimen with the best compatible trait combination exactly 3000.

## Ownership

`FishScoreV2Service` is the canonical score owner. UI code must consume canonical score output rather than reproducing the formula. Runtime persistence/display wiring is intentionally outside Stage 21.

## Tests

`FishScoreV2ServiceTest` covers:

- exact frozen species points
- exact raw minimum and normalized minimum
- exact raw maximum and normalized maximum
- several exact intermediate linear normalization cases
- every trait point value
- clamping outside the frozen raw range
- rejection of non-finite raw values

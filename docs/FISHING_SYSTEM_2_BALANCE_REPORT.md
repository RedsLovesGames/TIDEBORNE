# Fishing System 2.0 balance simulation report

Stage 49 validates the frozen Fishing System 2.0 probabilities with the offline deterministic `FishingSimulator`. No gameplay constants were changed.

## Method

- deterministic simulation seed: `0x5A17E2026`
- 250,000 catches per reported scenario
- controlled synthetic pool: one equally weighted species per canonical rarity
- each species uses the same neutral profile except rarity: encounter weight `1.0`, Strength `1.0`, Tempo `1.0`, steady behavior, lognormal size distribution median `30 cm`, sigma `0.20`
- rarity-filtered scenarios isolate trait probabilities from species selection
- Momentum scenarios use the simulator-only in-memory per-species Momentum state and the same canonical transition rule as production
- Perfect Catch scenarios use the canonical post-fight `+10 Trait Luck`, Body Type event multiplier, and direct Perfect Specimen reward

This setup validates Fishing System 2.0 mechanics rather than the content mix of the real Tide fish catalog. Actual world distributions also depend on Tide eligibility, encounter weights, biome/time/weather restrictions, and the species available in a cast.

## Baseline and Trait Luck

One-star species, Fishing Luck 0, no Momentum unless noted:

| Scenario | Body Type event | Condition event | Pigmentation event | Perfect Specimen | Mean FishScore |
| --- | ---: | ---: | ---: | ---: | ---: |
| Trait Luck 0 | 4.969% | 5.013% | 1.502% | 1.022% | 530.7 |
| Trait Luck 10 | 9.722% | 9.804% | 2.994% | 1.894% | 545.6 |
| Trait Luck 15 | 12.050% | 12.046% | 3.745% | 2.283% | 552.8 |
| Perfect Catch, base Trait Luck 0 | 12.124% | 9.804% | 2.994% | 2.939% | 551.1 |
| Momentum enabled, base Trait Luck 0 | 6.976% | 6.991% | 2.118% | 1.390% | 536.9 |

The baseline matches the frozen 5%, 5%, and 1.5% event probabilities. At Trait Luck 10 the analytical 5% event result is 9.75%, and at Trait Luck 15 it is 12.035%; the measured rates are within ordinary sampling error. Perfect Catch correctly raises Body Type slightly above the Trait Luck 15-style range because its Body Type multiplier is applied in addition to the temporary `+10 Trait Luck`.

### Subtype splits

At the one-star, zero-modifier baseline:

- Body Type: Giant 2.495% of all catches, Dwarf 2.474%; among Body Type events this is approximately 50.2% Giant / 49.8% Dwarf, as expected from a uniform natural-percentile population.
- Condition: 65.052% Scarred / 34.948% Parasite-Ridden among Condition events, matching the frozen 65/35 split.
- Pigmentation: 69.427% Albino / 30.573% Iridescent among Pigmentation events, matching the frozen 70/30 split.

## Rarity compensation

Trait Luck 0, Fishing Luck 0, one fixed rarity at a time:

| Rarity | Body Type | Condition | Pigmentation | Perfect Specimen | Mean FishScore |
| --- | ---: | ---: | ---: | ---: | ---: |
| 1 star | 4.969% | 5.013% | 1.502% | 1.022% | 530.7 |
| 2 star | 5.748% | 5.762% | 1.728% | 1.046% | 704.0 |
| 3 star | 6.980% | 7.029% | 2.111% | 1.089% | 964.4 |
| 4 star | 8.979% | 9.044% | 2.726% | 1.141% | 1226.5 |
| 5 star | 12.011% | 12.009% | 3.633% | 1.231% | 1577.1 |

These results closely match the frozen rarity multipliers. For the 5% axes, the zero-Trait-Luck analytical targets are 5.0%, 5.75%, 7.0%, 9.0%, and 12.0%. Pigmentation targets are 1.5%, 1.725%, 2.1%, 2.7%, and 3.6%.

Perfect Specimen does not directly use rarity compensation. Its small observed increase with rarity is indirect: higher rarity increases Body Type frequency, which can shift final physical percentile before the Quality curve is evaluated.

## Fishing Luck species distribution

Equal base encounter weight for all five rarities, Trait Luck 0, Momentum disabled:

| Fishing Luck | 1 star | 2 star | 3 star | 4 star | 5 star | Mean FishScore |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 0 | 20.021% | 19.953% | 20.034% | 20.000% | 19.992% | 1000.5 |
| +5 | 14.774% | 17.347% | 19.951% | 22.651% | 25.277% | 1069.3 |
| +10 | 13.292% | 16.632% | 19.996% | 23.365% | 26.716% | 1088.3 |
| +15 | 12.412% | 16.107% | 19.982% | 23.864% | 27.634% | 1100.4 |

Fishing Luck therefore shifts selection smoothly toward higher rarity without touching the canonical natural-percentile distribution. The five-star share rises monotonically from about 20% to 27.6% in this deliberately equal-weight test pool.

## Percentile distribution

For the equal-weight five-rarity Fishing Luck 0 scenario:

- natural percentile mean: `49.961`
- natural percentile decile counts: `[25343, 24921, 25227, 24601, 25055, 24832, 24955, 24884, 25272, 24910]`
- final percentile mean: `49.535`
- final percentile decile counts: `[30698, 24239, 24015, 23257, 23658, 23572, 23868, 24110, 25080, 27503]`

The natural percentile is effectively uniform, as required. The final percentile is intentionally not uniform because Giant and Dwarf Body Types transform physical length and then map that length back through the species CDF. This is expected and does not represent a second percentile roll.

## Momentum impact

One-star species, base Trait Luck 0, no Perfect Catch:

| Momentum | Body Type | Condition | Pigmentation | Perfect Specimen | Mean FishScore |
| --- | ---: | ---: | ---: | ---: | ---: |
| disabled | 4.969% | 5.013% | 1.502% | 1.022% | 530.7 |
| enabled | 6.976% | 6.991% | 2.118% | 1.390% | 536.9 |

With Momentum enabled:

- average captured Momentum: `4.150`
- catches using nonzero Momentum: `83.79%`
- maximum captured Momentum: `15`
- increments: `204,907`
- resets after a prior nonzero value: `35,680`

The effect is meaningful but bounded. Fully normal streaks push temporary Trait Luck upward, while any notable canonical axis resets that species' Momentum.

## Perfect Specimen curve

Direct frozen curve checkpoints remain exact:

| Final percentile / modifiers | Perfect Specimen probability |
| --- | ---: |
| P95, Trait Luck 0 | 2% |
| P97.5, Trait Luck 0 | 8% |
| P99, Trait Luck 0 | 25% |
| P99.9, Trait Luck 0 | 60% |
| P99, Trait Luck 10 | 43.75% |
| P99, Trait Luck 10 + Perfect Catch direct Quality bonus | 68.359375% |

The integrated one-star simulation produced 1.022% Perfect Specimens at Trait Luck 0, 1.894% at Trait Luck 10, 2.283% at Trait Luck 15, and 2.939% with Perfect Catch. These low whole-population rates are expected because Quality remains impossible below final P95.

## FishScore distribution

Representative score summaries:

- one-star baseline: mean `530.7`, min `1`, max `1849`; bands `[119659, 117789, 12143, 409, 0, 0]`
- five-star baseline: mean `1577.1`, min `1029`, max `2942`; bands `[0, 0, 110832, 113108, 24756, 1304]`
- equal-rarity pool at Fishing Luck 0: mean `1000.5`, min `1`, max `2880`; bands `[42703, 86356, 78885, 36277, 5526, 253]`
- equal-rarity pool at Fishing Luck +15: mean `1100.4`, min `1`, max `2922`; bands `[30474, 75834, 88802, 47014, 7525, 351]`

All generated scores remained within the canonical 1-3000 range. Higher rarity and notable traits move the score distribution upward without collapsing it at the cap.

## Conclusion

The deterministic sweeps match the frozen Fishing System 2.0 probability model:

- baseline trait event rates are correct
- subtype splits are correct
- rarity compensation scales as specified
- Fishing Luck affects species selection in the intended direction
- Trait Luck and Momentum increase notable-trait rates without changing the underlying natural percentile sample
- Perfect Catch applies the intended temporary Trait Luck, Body Type, and Quality rewards
- the Perfect Specimen curve remains gated to final P95+
- FishScore remains distributed across the intended 1-3000 range

No obvious balance defect or specification violation was found, so Stage 49 changes no gameplay constants.

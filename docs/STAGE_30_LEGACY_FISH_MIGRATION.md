# Stage 30: legacy fish migration core

Stage 30 adds the pure deterministic migration boundary for pre-V2 Tideborne fish. Runtime persistence wiring and score recalculation remain separate later work.

## Frozen migration rules

- Existing canonical Fishing System 2.0 specimens are returned verbatim and are never regenerated.
- A schema-v2 marker without the canonical specimen payload is rejected rather than treated as legacy data.
- Successful legacy migration writes canonical specimen schema version `2` and generation version `1` with `legacy-migration` provenance.
- A present legacy deterministic seed is preserved exactly. When absent, the seed is derived deterministically from species ID, normalized legacy mutation/body type, legacy percentile, and legacy physical length.
- Legacy `Giant` and `Dwarf` map to canonical Body Type `GIANT` and `DWARF`.
- Legacy `Scarred` maps to Condition `SCARRED`; `Parasite`/`Parasite-Ridden` map to `PARASITE_RIDDEN`.
- Legacy `Albino` and `Iridescent` map to canonical Pigmentation `ALBINO` and `IRIDESCENT`.
- Legacy `Perfect Specimen` maps to canonical Specimen Quality `PERFECT_SPECIMEN`.
- A valid legacy physical length is preserved exactly as canonical `finalLength`. Migration never applies a new Body Type size multiplier to old fish.
- If a valid legacy natural percentile exists, it remains `basePercentile`; `baseLength` is recovered from the species quantile and `finalPercentile` is derived from the preserved final physical length.
- If percentile is absent but physical length is valid, both natural percentile and final percentile are inferred deterministically from the species CDF. Because the pre-V2 data does not contain enough information to invert an unknown historical trait multiplier safely, the preserved physical length also becomes the best available base length in that case.
- If neither percentile nor usable physical size exists, a deterministic percentile is derived from the migration seed and the species quantile supplies size. Species with no physical-size distribution retain zero length.
- Migration does not calculate or recalculate FishScore. Score migration belongs to the later persistence/score migration slice.

## Idempotence

The migration result is a complete canonical `SpecimenData`. Feeding that canonical specimen back into the migration service returns the same object with `migrated == false`, so a second pass cannot change any field or consume any random state.

Unit tests cover every required legacy trait mapping, physical-size preservation, species-distribution percentile inference, preservation of an existing natural percentile, deterministic seed derivation, legacy seed preservation, schema write-once behavior, exact second-pass equality, canonical no-regeneration behavior, rejection of incomplete schema-v2 data, and no-physical-size fallback.

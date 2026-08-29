# Stage 53 - Legacy FishScore removal audit

Stage 53 removes the remaining active pre-V2 FishScore calculation and retires the Stage 41/42 compatibility-mirror read path.

## Canonical calculator authority

`FishScoreV2Service` is the only production FishScore calculator. Finalized Fishing System 2.0 specimens persist its normalized `1..3000` score in canonical specimen data.

The reconstructed `TraitAxesRuntime.score(...)` and `scoreFromParts(...)` signatures remain linkable for binary compatibility, but `LegacyFishScoreCalculatorMixin` cancels both at method entry and returns the scoreless sentinel. Their reconstructed rarity/percentile/length/trait arithmetic is therefore not executable in production. Migrated ItemStack consumers continue to enter through `TeamProgressStore.tideborneFishScore`, which is cancelled at method entry by `TeamProgressCanonicalJournalMixin` and returns the persisted canonical specimen score.

## Stored score migration

`StoredFishScoreStorage` now has one authority field: `canonical_fish_score`.

- `readCanonical` reads only `canonical_fish_score`.
- `writeCanonical` writes only `canonical_fish_score`.
- `migrateLegacyScore` is the only server code allowed to read an old persisted `fish_score` as score state. It copies a positive historical integer forward exactly when canonical storage is absent and never recalculates it.
- Once canonical storage exists, a conflicting or later `fish_score` value cannot replace it.
- Root migration covers contributors, history, and top-fish records.

The serialized `fish_score` key is still emitted where the reconstructed journal/client payload schema requires it. Those values are projections of canonical score state, not an authority field or a calculation source. Newly serialized contributor and history payloads also carry `canonical_fish_score`, so server reads never need to consume their compatibility field.

## Consumer audit

- Angler's Satchel and ItemStack/profile score surfaces use the canonical `TeamProgressStore.tideborneFishScore` interception from Stages 41/42.
- Team contributor leaderboards migrate old stored scores once, then read/order by canonical storage.
- Event history migrates old records once; new records receive canonical score storage at serialization.
- Top-fish ordering reads canonical storage only after root migration.
- History badge metadata already reads and writes through `StoredFishScoreStorage.readCanonical` / `writeCanonical`.
- Record-holder UI/network projection remains specimen-sidecar based and contains no FishScore formula.

## Compatibility boundary

Old saves remain readable because explicit legacy score migration preserves their stored integer exactly. Old client/payload readers may continue to see the historical `fish_score` field, but Tideborne production logic no longer reads that field except inside migration.

# Stage 42 FishScore consumer migration

Stage 42 finishes the major shared FishScore consumer boundary used by Angler's Satchel, item/profile displays, event history, top-fish/profile data, record summaries, and badge-adjacent history reads.

## One score source

`CanonicalSpecimenStorage.read` is now the only ItemStack FishScore source at the shared `TeamProgressStore.tideborneFishScore` consumer boundary. A canonical specimen returns its persisted normalized V2 `fishScore` exactly. A readable legacy specimen is first migrated through the existing canonical ItemStack migration boundary. If the specimen cannot be migrated or has no persisted canonical score, the consumer reports no score instead of evaluating the reconstructed pre-V2 stars/percentile/length formula.

This makes Angler's Satchel record scoring canonical because its `recordScoreValue` delegates to `TeamProgressStore.tideborneFishScore`. Item/profile score surfaces using the same helper receive the same behavior.

## Persisted history, badges, profile/top-fish and summaries

Team-progress persistence continues to use `StoredFishScoreStorage` from Stage 41:

- `canonical_fish_score` is authoritative for contributor, history, and `top_fish` records;
- old positive `fish_score` values are copied forward exactly when canonical storage is absent;
- `fish_score` remains a compatibility mirror for old saves, payloads, record summaries, and badge/history readers;
- when canonical and compatibility fields disagree, canonical storage repairs the mirror before consumers read or order records;
- new controlled compatibility writes are promoted back to canonical storage at the existing capture boundaries.

No history, badge, profile, Satchel, or record-summary consumer calculates FishScore from rarity, percentile, length, or traits in this stage.

## Compatibility

Existing saved score integers remain readable and retain their exact historical value. Existing legacy ItemStacks continue through the deterministic canonical migration service when sufficient specimen data exists. Missing historical scores remain missing rather than being invented.

## Regression coverage

`StoredFishScoreStorageTest` covers exact legacy-score copy-forward, canonical precedence, contributor/history/top-fish root migration, scoreless records, compatibility-mirror repair for history and top-fish data, ordering preservation, and controlled compatibility-write promotion.

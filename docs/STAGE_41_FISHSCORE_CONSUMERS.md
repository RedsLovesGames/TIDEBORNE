# Stage 41 FishScore consumer migration

Stage 41 migrates only the first FishScore consumer group: personal Journal, team Journal, and team-progress leaderboards.

## Canonical Journal score reads

`JournalSpecimenStore.readFishScore` reads the normalized FishScore already stored in the canonical `SpecimenData` snapshot. It does not derive a score from length, percentile, rarity, traits, or any legacy score formula.

The same canonical sidecar format is used for both persistence modes:

- personal Journal roots beside `TidePlayerData`;
- team Journal roots beside the shared `journal` compound.

Existing aggregate Tide journal records do not contain historical FishScore identity. Stage 32 can reconstruct deterministic largest/smallest specimen identity from species plus length, but those migrated snapshots intentionally have no FishScore. Stage 41 keeps those scores absent rather than inventing a value.

## Leaderboard catch score authority

The team-progress catch path previously called `TeamProgressStore.tideborneFishScore`, which could fall through to the reconstructed pre-V2 trait/size score formula for a noncanonical stack.

Stage 41 redirects only the leaderboard capture call site to `SPECIMEN_FISH_SCORE`. A catch without a persisted canonical V2 score is treated as scoreless for this consumer. The shared legacy tooltip helper remains untouched so later consumer groups can be migrated separately.

## Persisted leaderboard score migration

Old team-progress saves already contain explicit integer `fish_score` values for contributors, history entries, and top-fish entries. Those values are recoverable historical data, so `StoredFishScoreStorage` migrates them losslessly:

1. `canonical_fish_score` becomes the authoritative persisted field;
2. an old positive `fish_score` is copied exactly when the canonical field is absent;
3. no score formula is run during this copy-forward;
4. `fish_score` remains as a compatibility mirror for existing client payloads and save readers;
5. if both fields exist and disagree, canonical storage wins and repairs the compatibility mirror.

New writes in the team-progress path are promoted back into canonical persisted storage only at controlled boundaries where the value originated from the current canonical catch or an already-migrated stored record.

## Ordering semantics

No record ordering rule changes in this stage.

- contributor FishScore ranking still sorts by score using the existing `FishScoreComparator`, then applies the existing name tie-break in the snapshot path;
- top-fish insertion still uses the existing strict `candidateScore > existingScore` comparison, so equal scores retain their previous insertion behavior;
- history remains timestamp ordered;
- legacy score migration copies integers exactly, so the relative order of historical leaderboard records is unchanged.

## Tests

Targeted tests cover:

- personal and team Journal score reads returning an intentionally stored canonical score exactly;
- a scoreless canonical journal snapshot remaining scoreless instead of being recalculated;
- exact old stored-score migration with unrelated length/percentile/mutation data unable to affect the result;
- canonical score precedence over a conflicting compatibility mirror;
- contributor/history/top-fish root migration;
- scoreless sentinel records remaining scoreless;
- preservation of descending historical record order after migration;
- promotion of a controlled compatibility write back into canonical storage.

This stage does not migrate score tooltips, Satchel displays, item overlays, or other remaining FishScore consumers.

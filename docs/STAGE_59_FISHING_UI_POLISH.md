# Stage 59 Fishing System 2.0 UI correctness and polish

Date: 2026-08-30

## Scope

Stage 59 is a correctness, consistency, and readability pass over the Fishing System 2.0
surfaces on `dev`. It does not alter `main`, change the canonical FishScore formula, add
random generation, or make client UI authoritative.

## Canonical score projection fix

The contributor FishScore defect came from nested catch lifecycle handling. The outer catch
hook established current canonical catch state, but the inner journal write cleared that
thread-local state before the contributor projection executed. Top Fish could still read the
finalized last-catch tag, while the contributor projection saw no current score and serialized
the missing value as zero.

The projection now reads the highest valid canonical score from the current and finalized
last-catch tags. It never calls a score formula. Missing values use the existing negative
sentinel in storage and render as `N/A`; they are not converted to zero. The persisted best
score update remains one-way and idempotent, so a valid migrated or newer best score cannot be
downgraded by a missing or lower value.

Regression coverage freezes these cases:

- a nested/finalized catch with canonical score 644 updates the contributor projection to 644;
- a missing canonical score remains missing instead of becoming zero;
- repeated legacy migration preserves the same best value;
- Top Fish and contributor views agree on the same finalized canonical catch.

## Shared presentation rules

`FishingUiFormat` and `FishingUiLayout` centralize the rules used by the affected screens:

- positive canonical scores render as integers; absent or invalid scores render as `N/A`;
- percentiles render as `Pxx.x`;
- lengths below one meter render in centimeters and lengths at or above one meter render in
  meters with explicit units;
- enum-like trait names render in title case;
- catch timestamps use one compact local date/time format;
- bounded labels use a single ellipsis and preserve the full value for hover detail;
- numeric cells use a shared right-alignment calculation.

No affected UI class calls `FishScoreV2Service`, `SpecimenGenerator`, or a legacy
score-from-parts helper. UI remains a read-only projection of persisted or synchronized
canonical data.

## Screen changes

### Team Records Summary and History

Summary Recent Highlights and History now use the same event-row renderer. Every row has a
compact primary line, a secondary metadata line, a type-color bar, bounded text, and a full
hover tooltip. History displays five two-line rows per page, which keeps the rows above the
pagination controls. Its species filter has a visible label, species-ID placeholder, Apply
button, and Clear button.

### Leaderboard and Top Fish

Leaderboard columns are Rank, Member, and the selected metric. Numeric values are
right-aligned, canonical-score rows exclude missing values, and the empty state explicitly
says that contributor score data is not available yet. Top Fish uses stable ranks, separate
name/stars/score columns, canonical scores only, bounded labels, and grouped Specimen,
Traits, and Catch Info detail sections.

### Species and team statistics

Species pages no longer display invalid `Fish Score -1 - -1` ranges. They show a recorded
canonical score, a recorded canonical range, or `FishScore: N/A`. Team statistics show one
recorded specimen line for a single observation rather than duplicating largest and smallest
rows.

### Angler's Satchel and tooltips

Satchel specimen details use explicit score/length/percentile labels, grouped traits, fully
spelled personal records with units, and a clear protected indicator. Sorting shows ordered
priorities and direction labels. Upgrade, trophy-lock, and record headings state their
context, and satchel records use stable ranks and right-aligned canonical scores. Fish item
tooltips use the same canonical labels and formatting while continuing to recognize legacy
labels for cleanup.

## Visual sanity pass

The retained fixed-size panels were checked against their render bounds and control
coordinates:

- five History rows at two lines each end above the History controls;
- Summary uses the same bounded row geometry without duplicate mixin rendering;
- leaderboard rank, member, and metric columns do not overlap;
- Top Fish list and grouped detail sections remain within their left and right panels;
- the compressed Satchel contents, sorting, upgrades, and records sections remain within the
  existing screen frame;
- long names and metadata truncate with hover detail instead of crossing panel edges.

The CI environment has no graphical desktop, so this pass does not add a pixel screenshot
baseline. Layout helpers and source-safety tests provide deterministic coverage for the
clipping and projection constraints.

## Validation

Implementation commit: `8a2337dfe3f5b051a87c9221b97f39b86b9e61f2`

Workflow-control commit: `b3d7b6c036d5056d634d548b7c13f3a28dc4f902`

Portable artifact-validator commit: `4d293a034771a63e61edf15510d3e493382983bb`

GitHub Actions run `33303275742` passed:

- repository structure, mixin, entrypoint, and release-metadata validation;
- a clean Java 21 Gradle build and the complete unit suite;
- the complete GameTest suite without optional mods, with Apex Waters only, with Myths of
  the Sea only, and with both optional mods;
- all 249 unit tests;
- all 44 registered GameTests in the no-optional-mod, Apex Waters only, Myths of the Sea
  only, and combined optional-mod matrices;
- the known dedicated-server/client-connect smoke command as a recorded non-blocking check;
- production `tideborne-2.0.0.jar` validation with SHA-256
  `5fffa35cd327cc4a735e8f2abec2e72d61889f6004a9ec698ce290582641a0ce`;
- artifact upload and refresh of the `TIDEBORN-2.0.0` release asset.

The release-validator dependency on `rg`, which is not installed on the GitHub runner, was
replaced with portable `grep` checks. All required class, refmap, and forbidden-symbol
assertions remain intact.

Local static validation also passes `git diff --check`, JSON parsing, mixin/entrypoint
resolution, GameTest registration, and the affected-UI source scan.

## Remaining known issues

No Fishing System 2.0 UI correctness issue is known after this pass. The separately launched
client in the dedicated smoke harness still exits before the runner observes the completed
join message even though the dedicated server reaches ready state. That runner-only harness
issue is documented and remains outside the Stage 59 UI scope.

# P11 Player Language Audit

Status: completed and merged into `dev` through PR #7 on 2026-09-16. Final validated worker source head: `35ac604ae0b6055edd44d913798252845d92404e`. Merge commit: `16b71c2af0cddd489d670647fe4b092338113c0c`.

This audit covers player-visible language only. Public item IDs, fish IDs, recipe IDs, networking IDs, persistence keys, NBT/component IDs, commands, specimen identity, RNG, balance, catch selection, FishScore math, Satchel storage behavior, Journal behavior, records, and external Tide IDs are out of scope for renaming or behavioral changes.

## Player language rule

Player UI should explain what a mechanic does in player terms. Internal architecture names stay internal unless a debug or operator surface genuinely needs them.

Preferred vocabulary:

- Catch Zone
- Fish Movement
- Fishing Luck
- Trait Luck
- Lure Speed
- Shark Protection
- Record
- Largest Record
- Smallest Record
- Team Records
- Catch History
- Species
- Catch
- Bait
- Hook
- Line
- Leader
- Bobber

Avoid in normal UI:

- canonical
- backend
- synchronized state
- component
- NBT
- registry profile
- migration schema
- implementation
- compatibility layer
- ledger
- payload
- authoritative
- raw selection-weight terminology

## Mechanical distinctions verified

### Tide minigame values

`tentacleCatchZoneMultiplier`, `tentacleFishSpeedMultiplier`, `swiftCatchZoneMultiplier`, `swiftFishSpeedMultiplier`, `leviathanBaitMinigameSpeedMultiplier`, and `leviathanBaitCatchZoneMultiplier` affect the Tide fishing minigame. They must be described as Catch Zone and Fish Movement, not world-fish swimming speed.

Current defaults verified from `TideboundConfig.Values`:

- Tentacle Line Catch Zone: 132%
- Tentacle Line Fish Movement: 116%
- Abaia Line Catch Zone: 116%
- Abaia Line Fish Movement: 108%
- Leviathan Bait Fish Movement: 120%
- Leviathan Bait Catch Zone: 80%

### Catch selection modifiers

Seafarer's Hook, Shark Tooth Hook, Kujira Bone Rod, and some bait/bobber effects use relative catch-selection weighting. The UI must not convert those multipliers into direct probability claims. Current wording uses `favored` or `preference` plus the configured relative multiplier where an exact value is useful.

### Leviathan Fishing Luck

`TideborneFishingGearModifiers.LEVIATHAN_BAIT` contributes `fishingLuck(4.0)` and `traitLuck(1.0)` as separate modifiers. The player-facing `Fishing Luck` label is therefore mechanically correct and does not rename Trait Luck or a raw selection weight.

### Shark catch loss

`CATCH_LOSS_PREVENTION_CHANCE` is specifically protection against the shark catch-loss mechanic. Player-facing wording is `Shark Protection`, not durability or generic protection.

### Fishing Luck and Trait Luck

Fishing Luck and Trait Luck remain separate visible mechanics. Do not merge or rename one into the other.

## Implemented findings

### Fishing gear tooltips

Resolved:

- `Server values unavailable` -> fishing stats syncing message
- `Catch zone` -> `Catch Zone`
- `Fish speed` -> `Fish Movement`
- `Night ocean legendary weight` -> legendary fish favored at night in the ocean
- `Large/predatory weight` -> favors large and predatory fish
- `Very-small weight` -> favors very small fish
- `Ocean crate weight` -> ocean crates favored
- `Crate weight` -> crates favored
- `Catch-loss protection` -> `Shark Protection`
- `Lure bonus` -> `Lure Speed`
- Leviathan `fish-only` wording -> always hooks a Tide fish instead of junk, treasure, or crates
- Chum tooltip now shows scent range and duration only; raw particle density was removed because it does not help a player make a gameplay decision
- several ordinary hardcoded strings moved into `en_us.json`

Intentionally retained:

- exact relative multipliers in advanced tooltips
- Fishing Luck and Trait Luck as separate stats

### Fishing HUD

Resolved:

- `Tidebound Fishing` -> `Tideborne Fishing`
- `native movement` removed
- Tentacle and Abaia now show explicit Catch Zone and Fish Movement multipliers
- `catch-loss protection` -> `Shark Protection`
- Seafarer and Shark Tooth descriptions now state the fish types they favor
- Leviathan `fish-only pool; harder fight` -> always hooks a fish, but makes the fight harder
- shark scent status rewritten as `attracting sharks`

The HUD does not claim that selection weights are direct probabilities.

### Field Guide

Resolved:

- title uses Tideborne rather than Tidebound
- waiting message describes server stat sync in player terms
- `native movement` removed
- gear lines distinguish Catch Zone from Fish Movement
- Kujira crate behavior described as a relative selection value, not guaranteed crate chance
- Shark Protection described as protection from a stolen catch
- Chum explanation uses ocean-only, duration, range, and shark attraction language
- compatibility heading changed to integrations

### Shared specimen presentation

Resolved centrally in `CanonicalSpecimenPresentation`:

- `Body Type` display label -> `Body`
- `Pigmentation` display label -> `Color`

The Discovery badge UI and its source-safety tests now use the same `Body` and `Color` player terminology.

Internal class names, enum names, persistence names, and API names remain unchanged.

Percentile formatting remains `Pxx.x` for now. The numeric direction must be verified end-to-end before replacing it with a `Top X%` phrase so the ranking cannot be inverted.

### Top Fish

Resolved:

- `CANONICAL SPECIMEN` -> `FISH DETAILS`
- `Specimen` -> `Fish`
- `FishScore` -> `Fish Score`
- `Percentile` -> `Size Percentile`
- `No specimen selected` -> `No fish selected`
- `Catch Info` -> `Caught`

The screen still uses the existing percentile value because its ranking direction has not been changed or reinterpreted.

### Angler's Satchel

Resolved with display-string-only changes. Internal specimen classes, score accessors, network calls, sorting, storage, and interaction logic were left unchanged.

- ordinary `Specimen` / `specimens` labels -> `Fish` / `fish`
- `FishScore` -> `Score`
- `Percentile` -> `Size percentile`
- `Canonical specimen data is unavailable` -> player-facing fish-details message
- `server-synchronized traits` -> `Select a fish to inspect its traits`
- `synchronized specimen components` -> stored-fish record wording
- `canonical V2 FishScore` -> `Highest-scoring fish in this satchel`
- `multi-rule satchel sorting` -> sorting rules in priority order
- `native Tide largest and smallest records` -> largest and smallest fish records
- `optional server prerequisite unavailable` -> upgrade unavailable on this server
- `Server state unavailable` -> `Upgrade status unavailable`
- `Send the current sorting rules to the server` -> `Apply the current sorting rules`
- `Refresh from server` -> `Refresh satchel`
- `Waiting for server` -> `Updating satchel`
- `Satchel networking is unavailable` -> `Could not reach the server`
- dormant `Shared Ledger` feature label -> `Shared Discoveries`
- Trait Scanner now describes player-visible `Body`, `Color`, `Size percentile`, and `Score` terms

The internal `renderSharedLedger`, `SatchelSpecimenDisplay`, `CanonicalSpecimenPresentation`, request payloads, specimen variables, and feature IDs remain unchanged because they are implementation details rather than UI copy.

### Main Tideborne config

Resolved:

- ordinary descriptions no longer say `authoritative`, `synchronized`, `server-side discovery ledger`, or `canonical stored-specimen records`
- Tentacle and Abaia controls use `Catch Zone` and `Fish Movement`
- Seafarer and Kujira selection controls use `preference` rather than presenting weight as direct probability
- Leviathan Bait uses `Always hook fish`, `Fishing Luck bonus`, `Fish Movement multiplier`, and `Catch Zone multiplier`
- Satchel config descriptions use fish records, Sort Rules, and server-controlled balance language
- journal descriptions use team catch history, record claiming, and shared discoveries
- shark controls use detection range, scent strength, and `Sharks can steal catches`
- raw chum particle `density` is presented as particles per pulse in the advanced tuning screen

Intentionally retained in advanced/operator tuning:

- exact multipliers
- exact percentages
- percentile controls
- spawn intervals and 1-in-N spawn chance
- scent chance coefficients
- diagnostic compatibility controls

These are mathematical/operator controls where the technical value is useful.

### Team Records, Journal, and localization

Resolved in `en_us.json` and Journal presentation:

- `Records & Shared Ledger` -> `Records & Team History`
- `History` tab -> `Catch History`
- `Shared all-time catches` -> `Team catches`
- `Tracked contributors` -> `Contributors`
- `Fish Filter` -> `Species Filter`
- `Journal completion` -> `Species found`
- `post-update contributions` wording removed
- `record events` -> `records set`
- `active records` -> `current records`
- ordinary no-history text simplified
- transient ownership-repair alerts -> `Record Fixed`
- command status no longer says client metadata was resynchronized
- journal merge wording no longer says progress is `represented` in a ledger-like structure
- record badge capitalization normalized
- transient chat separators simplified
- Apex player message now says `integration`, not compatibility
- shark catch-loss message now says a shark stole the catch rather than teaching an uncertain reel-in sequence
- Journal `Best Specimen` -> `Best Fish`
- fish-profile fallback `No canonical specimen recorded` -> `No recorded fish details`
- fish-profile fallback `Recorded FishScore` -> `Recorded Score`

Technical ownership and repair terms remain in operator command flows where they describe actual maintenance actions.

### Command UI

Resolved:

- root `Fishing System 2.0` label -> `Fishing`
- root status help no longer exposes `Fishing System` architecture naming
- normal root button says `OPEN TEAM RECORDS`
- debug hovers drop unnecessary `canonical` adjectives

Intentionally retained in debug/admin UI:

- deterministic catch reproduction
- specimen identity
- percentile editing
- registry diagnostics
- server-owned specimen state
- Pigmentation as the exact internal trait axis on the debug editor

## Second-pass classification

The requested search vocabulary is classified as follows.

### 1. Internal code only

These remain unchanged when they are class names, method names, fields, persistence keys, comments, codec/network names, or implementation terminology:

- canonical
- backend
- component
- NBT
- payload
- registry
- synchronized
- migration
- compatibility
- specimen
- trait
- multiplier
- density
- weight / weighted when used in catch-selection calculations
- percentile when used as stored numeric state

### 2. Debug/admin appropriate

Retained when diagnostic value is real:

- specimen identity
- deterministic reproduction
- percentile
- registry diagnostics
- server-owned state
- migration status
- compatibility/integration status
- exact command IDs and internal axis names where an operator is editing data

Pointless adjectives such as `canonical` were removed from command hover text where they did not add diagnostic information.

### 3. Player-facing but acceptable

Retained where the term is already a player mechanic or the most accurate concise term:

- Trait Luck
- Fishing Luck
- chance when the value is an actual probability
- multiplier in advanced/server configuration when exposing the exact mathematical control is useful
- Species
- percentile when it is explicitly labeled as size percentile

### 4. Player-facing and rewritten

Rewritten during this pass where found on ordinary surfaces:

- native movement
- legendary weight
- large/predatory weight
- very-small weight
- ocean crate weight
- catch-loss protection
- fish-only pool
- shared ledger
- record events
- server values unavailable
- Fishing System 2.0
- unnecessary canonical adjectives
- authoritative/synchronized config prose
- server-synchronized traits
- synchronized specimen components
- multi-rule sorting
- optional server prerequisite language
- Best Specimen
- No canonical specimen recorded
- Recorded FishScore

## Known findings still open

No known high-priority architecture-style language remains on the audited normal player surfaces.

A future localization-only maintenance pass can move additional hardcoded `Text.literal(...)` strings from reconstructed screens into `en_us.json`. Those strings now use acceptable player-facing wording, so this is localization cleanup rather than an unresolved language defect. It should remain separate from behavior-sensitive reconstructed code unless there is a concrete localization requirement.

## Newly discovered issues during implementation

- The earlier wording direction `Tentacle Line ... native movement` was mechanically false for current defaults because Tentacle Line changes Fish Movement to 116%. The implementation now shows the real configured value.
- Abaia Line also changes both Catch Zone and Fish Movement. It is now shown as two minigame stats rather than an inferred tradeoff sentence.
- Leviathan Bait's selection modifier is a real `Fishing Luck` value in `FishingGearModifiers`, so that label is correct and remains distinct from Trait Luck.
- `Server values unavailable` did not tell players whether the feature was broken or still loading. It now explains that fishing stats are unavailable until server sync finishes.
- Chum particle density was present in the advanced item tooltip even though it is presentation detail rather than a fishing decision. That line was removed from the item tooltip and remains available only as an advanced config control.
- The Satchel mixed reconstructed implementation terminology directly into ordinary UI even though the underlying mechanics were already stable. Those strings were changed without changing its data or interaction paths.
- `No verified churn yet` from the earlier audit appears to have been stale/reconstructed wording and was not present in the current worker version of `TideboundTooltips.java` during implementation.
- Final source-safety tests still expected `Body Type`, `Pigmentation`, `Best Specimen`, and the canonical-specimen fallback after the UI had been cleaned. Those expectations were updated to match the finished player wording.

## Final validation

Validated worker source head: `35ac604ae0b6055edd44d913798252845d92404e`.

Passed before merge:

- repository structure and release metadata validation
- production Gradle build and unit tests
- production release artifact validation and JAR upload
- Java 21 production compile
- PMD changed-line gate
- CPD changed-line gate
- Semgrep changed-line gate
- Qodana JVM changed-code gate
- aggregate Java / AI quality gate
- P10.5 runtime matrix `required-only`
- P10.5 runtime matrix `myths-only`
- P10.5 runtime matrix `apex-only`
- P10.5 runtime matrix `apex-and-myths`
- dedicated server plus real client join in all four runtime variants

GameTests were intentionally not run or repaired during this closeout, per project direction.

A targeted `PMD.UnusedPrivateMethod` suppression remains on the Mixin `@Inject` callback `tideTeamJournal$restoreProfileBlendState`. PMD cannot see framework injection usage; this is a static-analysis annotation only and does not change runtime behavior.

## Scope guard

This pass did not alter fishing balance, selection math, RNG, specimen generation, FishScore, records, Satchel storage, network IDs, migration formats, or minigame behavior as part of the language cleanup. `build.gradle` was left untouched.

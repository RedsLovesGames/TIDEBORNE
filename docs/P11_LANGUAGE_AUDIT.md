# P11 Player Language Audit

Status: implementation in progress on `agent/p10-5-runtime-fixes`.

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

Seafarer's Hook, Shark Tooth Hook, Kujira Bone Rod, and some bait/bobber effects use relative catch-selection weighting. The UI must not convert those multipliers into direct probability claims. Current wording uses `favors` plus the configured relative multiplier where an exact value is useful.

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
- Chum's single semicolon-heavy density line split into scent range/duration and cloud density lines
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
- Kujira crate behavior described as relative selection value, not guaranteed crate chance
- Shark Protection described as protection from a stolen catch
- Chum explanation uses ocean-only, duration, range, and shark attraction language
- compatibility heading changed to integrations

### Shared specimen presentation

Resolved centrally in `CanonicalSpecimenPresentation`:

- `Body Type` display label -> `Body`
- `Pigmentation` display label -> `Color`

Internal class names, enum names, persistence names, and API names remain unchanged.

Percentile formatting remains `Pxx.x` for now. The numeric direction must be verified end-to-end before replacing it with a `Top X%` phrase so the ranking cannot be inverted.

### Team Records and localization

Resolved in `en_us.json`:

- `Records & Shared Ledger` -> `Records & Team History`
- `History` tab -> `Catch History`
- `Shared all-time catches` -> `Team catches`
- `Tracked contributors` -> `Contributors`
- `Fish Filter` -> `Species Filter`
- `post-update contributions` wording removed
- `record events` -> `records set`
- ordinary no-history text simplified
- command status no longer says client metadata was resynchronized
- journal merge wording no longer says progress is `represented` in a ledger-like structure
- record badge capitalization normalized
- transient chat separators simplified

Technical ownership-repair terms remain where they describe actual maintenance/admin operations.

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
- specimen in limited collection/detail contexts where it reads naturally

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

## Known findings still open

The following surfaces still need a dedicated follow-up if the current branch remains stable after validation:

- `TopFishScreen.java`: `CANONICAL SPECIMEN`, `Specimen`, `FishScore`, `Percentile`, and `No specimen selected` are still hardcoded in a large reconstructed UI class.
- `AnglersSatchelScreen.java`: large reconstructed UI class still contains hardcoded Satchel labels and needs a careful localization-only pass without storage/selection behavior changes.
- `TideborneConfigScreen.java`: advanced server tuning still exposes several raw `weight`, `density`, and `multiplier` labels. Those should be split between normal player wording and explicitly advanced mathematical controls.
- remaining normal `Text.literal(...)` strings in reconstructed screens should move to translation keys when doing so does not create risky churn.

These are presentation findings, not gameplay defects.

## Newly discovered issues during implementation

- The earlier wording direction `Tentacle Line ... native movement` was mechanically false for current defaults because Tentacle Line changes Fish Movement to 116%. The implementation now shows the real configured value.
- Abaia Line also changes both Catch Zone and Fish Movement. It is now shown as two minigame stats rather than an inferred tradeoff sentence.
- `Server values unavailable` did not tell players whether the feature was broken or still loading. It now explains that fishing stats are unavailable until server sync finishes.
- `No verified churn yet` from the earlier audit appears to have been stale/reconstructed wording and was not present in the current worker version of `TideboundTooltips.java` during implementation.

## Validation policy

For this pass:

- validate `en_us.json`
- verify translation keys used by modified Java exist
- run repository validation and unit tests through the branch CI where available
- run clean production build/release validation where the repository workflow provides it
- do not run or fix GameTests, per project direction
- inspect final worker diff for generated files, formatting churn, line-ending noise, and behavior changes
- leave `build.gradle` untouched unless a validation failure proves it is required

## Scope guard

This pass must not alter fishing balance, selection math, RNG, specimen generation, FishScore, records, Satchel storage, network IDs, migration formats, or minigame behavior. If wording uncovers a gameplay defect, record it separately rather than fixing mechanics inside the language pass.

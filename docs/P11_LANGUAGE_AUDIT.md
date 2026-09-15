# P11 Player Language and Onboarding Audit

Status: audit in progress. This document defines the language rules for the upcoming player-facing rewrite. It does not rename public IDs, command literals, persistence keys, NBT fields, packet fields, or internal architecture.

## Goal

Make Tideborne easy to understand for players with low English proficiency and for players using English as a second language.

Player-facing text should answer three questions quickly:

1. What is this?
2. What does it do?
3. What should I do next?

Do not expose implementation terminology when a simpler gameplay term exists.

## Core writing rules

- Use one idea per sentence.
- Prefer short, common words.
- Prefer direct verbs: catch, hook, store, sort, protect, move, grow.
- Buttons and labels should usually be 1-3 words.
- Tooltips should usually be 1-2 short sentences or compact stat lines.
- Quest text may use 2-4 short sentences.
- Avoid sentence fragments that depend on English word order. Use translation placeholders instead.
- Avoid raw multipliers when a percentage change is easier to understand.
- Keep flavor text separate from gameplay instructions.
- Keep technical/debug terms in debug or admin surfaces only.
- Do not rename IDs or data keys as part of the language pass.

## Critical gameplay distinction

Tideborne currently mixes three different meanings of "fish" in player-facing language. They must stay separate.

### 1. Hooking / catch selection

This controls what is hooked before the Tide minigame begins.

Preferred terms:
- Fishing Luck
- Rare Fish Chance
- Hook Chance
- Fish Only
- Crate Chance

Avoid:
- weight
- pool
- selection weight
- legendary weight

### 2. Tide fishing minigame

These values affect the fish marker and catch bar inside Tide's fishing minigame. They do not make world fish entities swim faster or slower.

Preferred terms:
- Catch Bar Size
- Minigame Fish Speed
- Fishing Minigame

Hard rule: if a value only affects the Tide minigame, use "minigame" or another unmistakably minigame-specific term such as "Catch Bar".

Avoid:
- Fish Speed, when the context could mean a swimming entity
- Catch Zone
- native movement
- harder fight

### 3. World fish and sharks

These are actual entities and ecosystem behavior in the world.

Preferred terms:
- Shark Detection Range
- Fish Scent
- Shark Attraction
- Shark Attack / Catch Theft
- Chum Range
- Chum Duration

Do not use minigame terms for these systems.

## Standard player vocabulary

| Internal / current wording | Player-facing wording |
| --- | --- |
| FishScore | Fish Score |
| canonical specimen | Fish / Specimen |
| canonical | remove in normal UI |
| percentile | Size Rank or Top X% |
| pigmentation | Color |
| body type | Body |
| catch zone | Catch Bar Size |
| fish speed | Minigame Fish Speed |
| catch-loss protection | Shark Protection |
| record event | New Record |
| active record | Current Record |
| shared ledger | Team Records / Team Progress |
| synchronized / authoritative | server controlled, only when the distinction matters |
| legendary weight | Rare Fish Chance |
| crate weight | Crate Chance |
| fish-only pool | Only hooks fish |
| trait luck | Trait Luck only if retained as a visible mechanic; otherwise explain the outcome |

## Centralized presentation opportunity

File: `presentation/CanonicalSpecimenPresentation.java`

A large part of the specimen language is already centralized. P11 should use that instead of patching every screen independently.

Current central labels:
- `Body Type` / `Body`
- `Condition` / `Cond`
- `Pigmentation` / `Pig`
- `Quality` / `Qual`
- percentile formatted as `P18.5`

P11 direction:
- full `Body Type` label -> `Body`
- `Pigmentation` -> `Color`
- short `Pig` -> `Color` or `Clr` only where width forces an abbreviation
- avoid `Cond` / `Qual` unless the screen cannot fit full words
- percentile display -> human-readable size rank such as `Top 18.5%` where mathematically correct for the existing percentile direction

Before changing percentile formatting, verify whether a larger internal percentile means a larger fish. The text must not invert the ranking semantics.

This class is a high-leverage implementation point because Satchel, Journal, Top Fish, inspection surfaces, and other specimen views consume it.

## High-priority findings

### Tide fishing HUD

File: `presentation/client/TideboundFishingHud.java`

Current text is compact but exposes implementation language and blurs the line between minigame behavior and world entities.

Current -> proposed direction:

- `Tidebound Fishing` -> `Fishing Effects`
- `Tentacle: 120% zone; native movement` -> `Tentacle Line: Bigger Catch Bar` or `Catch Bar: +20%`
- `Abaia: 120% zone` -> include both minigame effects explicitly, e.g. `Abaia Line: Catch Bar +20%, Fish Speed +X%`
- `Steel Leader: 75% catch-loss protection` -> `Steel Leader: 75% Shark Protection`
- `Seafarer: night ocean legendary focus` -> `Seafarer: Better rare ocean fish chance at night`
- `Shark Tooth: large/predatory focus` -> `Shark Tooth: Better chance for large predator fish`
- `Leviathan Bait: fish-only pool; harder fight` -> `Leviathan Bait: Fish Only` plus explicit minigame stat lines if needed
- `Large catch: produces shark scent` -> `Large catch: Sharks can smell it`

The HUD should not teach full mechanics. FTB Quests and normal tooltips should do that. HUD text should be quick status information.

### Fishing gear tooltips

File: `presentation/client/TideboundTooltips.java`

Problems:
- `Catch zone`
- `Fish speed`
- `Night ocean legendary weight`
- `Large/predatory weight`
- `Very-small weight`
- `Ocean crate weight`
- `fish-selection luck`
- `Catch-loss protection`
- raw `100%` multiplier formatting can make a neutral value look like a bonus
- Chum exposes `density`, which is primarily presentation/config detail rather than a player decision

Rewrite direction:

- `Catch zone` -> `Catch Bar Size`
- `Fish speed` -> `Minigame Fish Speed`
- `Night ocean legendary weight` -> `Rare Ocean Fish Chance at Night`
- `Large/predatory weight` -> `Large Predator Fish Chance`
- `Very-small weight` -> `Very Small Fish Chance`
- `Ocean crate weight` -> `Ocean Crate Chance`
- `Catch-loss protection` -> `Shark Protection`
- Leviathan bait should use separate lines such as `Only hooks fish`, `Fishing Luck: +4`, `Catch Bar: +25%`, `Minigame Fish Speed: +20%` as appropriate to the actual configured values.
- Keep habitat restrictions simple: `Fish still need their normal biome and conditions.`

Advanced tooltip mode can show exact values, but the labels should still be plain language.

### Top Fish / specimen screens

File: `journal/client/TopFishScreen.java`

Current -> proposed:

- `CANONICAL SPECIMEN` -> `FISH DETAILS`
- `No specimen selected` -> `Select a fish`
- `Specimen` -> `Fish`
- `FishScore` -> `Fish Score`
- `Percentile` -> `Size Rank`
- `Pigmentation` -> `Color`
- `Catch Info` -> `Caught`

Preferred detail layout:

- Fish Score: 192
- Stars: ★
- Size Rank: Top 18.5%
- Length: 31.9 cm
- Body: Normal
- Condition: Normal
- Color: Normal
- Quality: Normal
- Caught by: Player
- Caught: date

### Team Records / Team Journal

Files:
- `journal/client/TeamRecordsScreen.java`
- `assets/tideborne/lang/en_us.json`

Findings:

- `Species ID` is developer-oriented for a normal filter. Prefer `Fish Name` if the filter supports names; otherwise leave ID entry in debug/admin UI.
- `Tracked contributors` -> `Team Members` or `Players Tracked` depending behavior.
- `Shared all-time catches` -> `Team Catches`.
- `Journal completion` -> `Fish Found` or `Species Found`.
- `record events` -> `records set` / `new records`.
- `active records` -> `current records`.
- `post-update contributions` is migration/history language and should not be shown to ordinary players.
- `Ownership Repair` and `Record Ownership Repaired` are maintenance concepts. Keep them in admin history/debug views, or present a simpler `Record Fixed` label if players must see them.
- `proof-based ownership claims`, `exact team-record fish`, `represented in the party journal`, and `client metadata was resynchronized` are admin/debug wording and should not appear in normal user flows.

The Team Records screen is currently debug-hidden by default, but its language should still be cleaned because it remains accessible as a troubleshooting feature.

### Alerts, chat, and short-lived messages

Source: `assets/tideborne/lang/en_us.json`

Short-lived text has to be even easier to scan than menus because it disappears quickly.

Good or nearly good existing strings:
- `Catch Lost!`
- `New Largest Record!`
- `New Smallest Record!`
- `Caught by: %s`
- `Length: %s`

Needs simplification:
- `First Team Discovery!` -> `New Team Fish!` or `First Team Catch!` depending exact semantics
- `Record Ownership Repaired` -> `Record Fixed` for normal visibility
- `Improvement` -> `New record by` or a compact direct comparison if space permits
- `A shark ate the fish before you could reel it in.` is clear, but verify mechanics: if the catch-loss event happens after the minigame rather than before reeling, use `A shark stole your catch.` to avoid teaching a false sequence.
- `Your leader held through a sudden bite!` -> `Your Steel Leader saved the catch!` if Steel Leader is always the cause

Chat record announcements currently combine several values with dash separators. Prefer a consistent, short structure and avoid requiring the player to infer what each unlabeled value means.

### Satchel

File: `satchel/client/AnglersSatchelScreen.java`

High-risk vocabulary already found:

- canonical percentile
- server-synchronized traits
- specimen components
- multi-rule
- optional server prerequisite
- shared ledger

Rewrite rules:

- Use `fish`, `stored fish`, or `specimen` instead of `canonical specimen`.
- Use `Size Rank` instead of `canonical percentile`.
- Use normal trait labels: `Body`, `Condition`, `Color`, `Quality`.
- Use `Sort Rules` instead of `multi-rule`.
- Explain unmet requirements as an action: `Upgrade the Satchel first.` / `This upgrade is controlled by the server.`
- Never mention components, synchronization, ledgers, or canonical storage in normal Satchel UI.

Also move remaining player-facing `Text.literal(...)` strings to translation keys where practical.

### Main Tideborne config

File: `client/TideborneConfigScreen.java`

Normal player/client settings should be simple. Advanced server settings may remain technical, but still need consistent labels.

High-priority rewrites:

- `Authoritative fishing behavior and catch selection live on the server.` -> `The server controls these fishing settings.`
- `Tentacle Line catch-zone multiplier` -> `Tentacle Line Catch Bar Size`
- `Tentacle Line fish-speed multiplier` -> `Tentacle Line Minigame Fish Speed`
- `Abaia Line catch-zone multiplier` -> `Abaia Line Catch Bar Size`
- `Abaia Line fish-speed multiplier` -> `Abaia Line Minigame Fish Speed`
- `Seafarer night legendary-weight multiplier` -> `Seafarer Rare Ocean Fish Chance at Night`
- `Kujira ocean-crate weight multiplier` -> `Kujira Ocean Crate Chance`
- `Force fish-only catches` -> `Only Hook Fish`
- `Fish-selection luck bonus` -> `Fishing Luck Bonus`
- `Minigame speed multiplier` -> `Minigame Fish Speed`
- `Catch-zone multiplier` -> `Catch Bar Size`
- `Record Keeper and specimen records are stored per Satchel and synchronized by the server.` -> `Each Satchel keeps its own fish records.`
- `Shared journal history and discovery tracking rules.` -> `Controls team discoveries and record history.`
- `proof-based ownership claims` -> remove from normal description
- `server-side discovery ledger` -> `team discoveries`
- `Enable abstract shark catch loss` -> `Sharks Can Steal Catches`
- `Large-fish scent multiplier` -> keep only in advanced balance, phrased as `Large Fish Scent Strength`
- `Strong shark-food scent multiplier` -> `Shark Food Scent Strength`
- `Scent chance per strength` -> advanced-only; explain the actual probability model in admin docs rather than normal UI
- `Specimen Distribution` can remain an Advanced category, but `percentile` should be explained as size rank/natural size roll if exposed.

### Commands

Files include:
- `command/TideborneCommandUi.java`
- `command/TideborneCommands.java`
- `command/TideTraitsCommands.java`
- inspect/recovery/reproduce commands

Normal command UI currently mixes player actions and developer architecture.

Examples:

- `Fishing System 2.0` -> player-facing command root should simply say `Fishing` or `Tideborne Fishing`.
- `View Tideborne server and Fishing System status` -> `View Tideborne server status`.
- Debug terms such as `canonical`, `deterministic`, `registry`, and `server-owned state` are acceptable inside `/tideborne debug`, but should not leak into ordinary command messages.
- `OPEN TEAM JOURNAL` should be removed or clearly debug-only if the Team Records screen remains debug-only.
- Debug `Pigmentation` can remain technically accurate, but `Color` is easier even there unless the exact data-axis name matters to developers.

Command literals and syntax must NOT be renamed during the language pass unless there is a separate compatibility plan. Only displayed labels/help text should change by default.

### `en_us.json`

High-priority normal-player strings found:

- `Records & Shared Ledger`
- `Traits & Mutations`
- `Migration & Compatibility`
- `server-configured XP`
- `journal size statistics`
- `exact team-record fish`
- `record owners`
- `tracked contributor`
- `record events`
- `active records`
- `fish-selection luck`
- `Harder catch: fish speed ... catch zone ...`
- `without bypassing habitats`
- `Fishing Integrations`

Many command/admin strings may remain technical, but they should be clearly separated from normal player translation keys.

## Field Guide replacement

The standalone Tideborne/Tidebound Field Guide should be removed after equivalent onboarding is available in FTB Quests.

Do not replace one large wall of text with another. Teach mechanics by action.

Suggested quest structure:

### Getting Started
- Catch a fish
- Open the Tide fishing minigame
- Keep the fish in the Catch Bar

### Fishing Minigame
- Learn Catch Bar Size
- Learn Minigame Fish Speed
- Try gear that changes each stat

### Your Catch
- Size / Size Rank
- Body
- Condition
- Color
- Quality
- Fish Score

### Angler's Satchel
- Get/open a Satchel
- Store a fish
- Sort fish
- Protect important fish
- Upgrade capacity/features

### Fishing Gear
- Line
- Hook
- Leader
- Bobber
- Bait
- Leviathan Bait

### Ocean Ecosystem
- Shark risk
- Steel Leader protection
- Chum
- Fish scent / shark attraction

### Challenges / achievements
- Catch a Giant fish
- Catch a Dwarf fish
- Catch unusual Color/Condition/Quality traits
- Catch a Legendary fish
- Reach Fish Score milestones
- Catch a Perfect specimen
- Protect a catch from a shark
- Use Chum successfully
- Complete collection goals

## Localization requirements

The P11 implementation should also make future translations easier.

- Move normal player-facing literals into language JSON.
- Do not build sentences by concatenating translated fragments.
- Use complete translation keys with placeholders.
- Keep values separate from grammar when possible.
- Avoid idioms in instructional text.
- Flavor text may remain stylistic because it is optional and non-instructional.
- Do not encode color, pluralization assumptions, or English word order into reusable fragments.

## Implementation order

1. Finish inventory of player-facing text surfaces.
2. Lock the vocabulary table above.
3. Rewrite fishing HUD and equipment tooltips first because they can currently misdescribe minigame stats as world-fish behavior.
4. Rewrite specimen/stat labels through `CanonicalSpecimenPresentation` where possible.
5. Rewrite Satchel labels, errors, and help text.
6. Rewrite Journal/Team Records text.
7. Simplify normal config descriptions; keep detailed balance language in Advanced/Admin areas.
8. Simplify normal command output while preserving command syntax.
9. Add FTB Quests onboarding/achievement content.
10. Remove the standalone Field Guide and its keybind/entry points once quest coverage exists.
11. Move remaining normal UI literals into translation keys.
12. Run a final terminology search for forbidden normal-UI terms: `canonical`, `authoritative`, `ledger`, `native movement`, `weight`, `catch zone`, ambiguous `fish speed`, `component`, `synchronized`, `record event`, and `percentile`.

## Non-goals

P11 language changes must not alter:

- public item/entity/recipe IDs
- NBT/component keys
- command literals without a compatibility plan
- networking payload IDs or fields
- specimen identity
- server authority
- fishing RNG/balance
- Satchel persistence
- Journal/team/history/Top Fish data
- FTB Teams ownership semantics

The language layer should become simpler without changing the underlying game rules.

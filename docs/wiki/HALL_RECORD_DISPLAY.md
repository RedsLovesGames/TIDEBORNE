# Hall Record Display

[← Team Journal & Records](TEAM_JOURNAL_AND_RECORDS.md) | [Wiki Home](README.md) | [Fishing Gear & Integrations →](FISHING_GEAR_AND_INTEGRATIONS.md)

The **Hall Record Display** is Tideborne's in-world record and specimen display block. It lets a world turn fishing history into something physical: a trophy room, server fishing hall, museum, or personal collection space.

## What it can display

Tideborne 2.1.0 includes personal and team history modes. The display can request canonical catch-history information and render stored specimen data instead of inventing a new fish when the display is opened.

History supports the canonical sort modes:

- **Best**
- **Worst**
- **Newest**
- **Oldest**

The Hall interface also supports paged history and species-oriented selection so larger histories do not need to be sent or shown all at once.

## Personal mode

Personal mode works from the player's canonical catch history. It is useful for a private trophy room where the focus is the player's own notable catches rather than the current team record holder.

## Team mode

Team mode uses team-oriented history and record data. On multiplayer servers this makes it possible to build a shared hall around the party's catches and record events.

## Specimen identity

A Hall entry is not just a fish name and a number. Tideborne's history path stores and projects specimen information so presentation can retain characteristics such as size and traits when that data is available. This is important for a trophy display because a historical Iridescent Giant should remain that specimen when revisited.

## Crafting availability in 2.1.0

The Tideborne 2.1.0 JAR registers `tideborne:hall_record_display`, but it does **not** include a standard JSON crafting recipe for the block. In an unmodified 2.1.0 setup, access therefore depends on creative/operator access or a recipe supplied by the server or modpack.

This wiki does not invent a recipe that the release does not ship.

## Good server uses

A Hall Record Display works especially well for:

- a spawn fishing museum
- a team record room
- a tournament hall
- a rare-trait showcase
- a personal fishing cabin
- a memorial to old records that have since been beaten

For the underlying history and team events, see [Team Journal & Records](TEAM_JOURNAL_AND_RECORDS.md). For how the displayed specimen is scored, see [FishScore](FISHSCORE.md).
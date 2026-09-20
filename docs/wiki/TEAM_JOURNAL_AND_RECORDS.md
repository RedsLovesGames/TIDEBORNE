# Team Journal & Records

[← Angler's Satchel](ANGLERS_SATCHEL.md) | [Wiki Home](README.md) | [Hall Record Display →](HALL_RECORD_DISPLAY.md)

Tideborne's Team Journal turns an **FTB Teams party** into a shared fishing progression group. It keeps team discoveries, catch totals, size records, contributors, history, and leaderboards while preserving the identity of important canonical specimens.

## What is shared

Team tracking can include:

- discovered species
- total catches
- unique species
- largest and smallest records by species
- record owner information
- record events
- contributor statistics
- active records
- best FishScore statistics
- Team Top Fish / Top 15 specimen data

A server can disable or configure parts of the team system.

## Main interface

Use:

```text
/ttj
```

or:

```text
/ttj open
```

The 2.1.0 Team Records screen contains four main tabs:

- **Summary**
- **Leaderboard**
- **History**
- **Settings**

`/tideborne journal` also routes players into the team-journal system.

## Record events

The team history recognizes these event types:

### First Discovery

The first tracked team discovery of a fish species.

### New Largest

A member catches a specimen larger than the team's current largest record for that species.

### New Smallest

A member catches a specimen smaller than the team's current smallest record for that species.

### Ownership Repair

An administrative or recovery action corrects record ownership without pretending a new live catch occurred.

## Leaderboards

The built-in leaderboard metrics are:

- **Catches**
- **Unique Species**
- **Records Set**
- **Active Records**
- **Best FishScore**

Use:

```text
/ttj leaderboard [metric] [page]
```

## History

View general team record history:

```text
/ttj history [page]
```

Filter history to one fish ID:

```text
/ttj history fish <fish_id> [page]
```

History is meant to preserve the team's fishing story, not just the current record table. A former record can remain historically meaningful after another player beats it.

## Member statistics

Inspect one member by name or UUID:

```text
/ttj member <name-or-uuid>
```

This lets the team view member-specific contribution information without requiring the player to currently hold every record they once set.

## Importing older personal progress

The one-time merge command can import missing personal journal progress into the team context:

```text
/ttj merge
```

Use this for migration or onboarding where personal discoveries existed before the shared team state.

## Record ownership tools

Tideborne retains explicit ownership tools because old data or migrations may know the record but not the correct member.

Claim the held fish as a record:

```text
/ttj claim <largest|smallest>
```

Assign the held record to an online member:

```text
/ttj assign <largest|smallest> <online-member>
```

Repair many records at once:

```text
/ttj claimall [online-member]
```

Inspect ownership for the held fish:

```text
/ttj status
```

Some ownership and repair commands require elevated team or operator permission.

## Configuration reload

Operators can reload Team Journal configuration through:

```text
/ttj config reload
```

For the unified Tideborne configuration and primary commands, see [Configuration & Commands](CONFIGURATION_AND_COMMANDS.md).

## Records vs FishScore

A team size record and a high FishScore are different accomplishments. Team record ownership is not added to the FishScore formula. The leaderboard exposes both record-oriented metrics and Best FishScore so different kinds of fishing achievements remain visible.

## Former members

The client settings include presentation controls for former members, badges, record tooltips, alerts, default tab, default leaderboard metric, and related record presentation. Exact visibility can therefore vary by client and server settings.
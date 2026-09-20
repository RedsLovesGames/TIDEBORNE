# Configuration & Commands

[← Recipes](RECIPES.md) | [Wiki Home](README.md) | [Updating & Compatibility →](UPDATING_AND_COMPATIBILITY.md)

Tideborne 2.1.0 uses a unified configuration entry point while retaining historical serialized namespaces where compatibility requires them.

## Main configuration

The canonical user-facing configuration file is:

```text
config/tideborne.json
```

The 2.1.0 UI groups settings into these areas:

- **Traits & Mutations**
- **Angler's Satchel**
- **Journal & Teams**
- **Client & Rendering**
- **Fishing Gameplay**
- **Records & Shared Ledger**
- **Migration & Compatibility**

If Mod Menu is installed, Tideborne can expose its configuration screen through the normal mod-menu workflow.

## What can be configured

The exact JSON keys are version-sensitive, but the unified backend covers settings such as:

- trait and specimen behavior
- Satchel conversion, upgrade costs, capacity multipliers, sorting, Auto-Stow, and protection behavior
- Journal/team feature switches and presentation
- fishing-gameplay modifiers
- record and Shared Ledger behavior
- optional compatibility switches
- migration behavior
- client rendering and display preferences

Server-owned settings remain authoritative in multiplayer even if a client has different local preferences.

## Main Tideborne command

```text
/tideborne
```

This opens the main Tideborne command menu/help surface.

### Status

```text
/tideborne status
```

Use this to inspect Tideborne's unified runtime/backend status.

### Open the Team Journal

```text
/tideborne journal
```

Routes into Tideborne's team-journal interface.

### Reload configuration

```text
/tideborne reload
```

Reloads the Tideborne configuration. This is intended for operators or otherwise permission-authorized users.

### Debug tools

```text
/tideborne debug
```

The 2.1.0 command tree includes operator/developer tools for fishing state, specimens, the fish registry, and the gear registry. Debug commands can inspect or manipulate canonical state and should not be treated as ordinary survival progression commands.

## Team Journal commands

Common player-facing Team Journal commands:

```text
/ttj
/ttj open
/ttj leaderboard [metric] [page]
/ttj history [page]
/ttj history fish <fish_id> [page]
/ttj member <name-or-uuid>
/ttj merge
/ttj status
```

Record ownership tools:

```text
/ttj claim <largest|smallest>
/ttj assign <largest|smallest> <online-member>
/ttj claimall [online-member]
```

Operator configuration reload:

```text
/ttj config reload
```

See [Team Journal & Records](TEAM_JOURNAL_AND_RECORDS.md) for what each command does.

## Compatibility commands

The preserved compatibility subsystem also includes integration status/reload tooling under its historical command namespace. Those commands are mainly useful to server operators diagnosing Myths of the Sea or Apex Waters integration.

Changing certain optional-mod master switches can require a restart because optional classes must remain safely isolated when the mod is absent.

## Raw XP vs levels

Satchel costs shown in the configuration and wiki are **raw vanilla experience points**, not Minecraft experience levels. A cost of 100 XP does not mean 100 levels.

## Recommended server workflow

Before editing a production server configuration:

1. Stop or back up the server if the change affects persistence or migration.
2. Edit `config/tideborne.json`.
3. Use `/tideborne reload` only for settings designed to reload safely.
4. Restart when changing optional-integration master switches or when a setting explicitly requires restart behavior.
5. Confirm with `/tideborne status` and test one catch before returning the server to normal play.
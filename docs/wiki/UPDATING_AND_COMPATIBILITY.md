# Updating & Compatibility

[← Configuration & Commands](CONFIGURATION_AND_COMMANDS.md) | [Wiki Home](README.md) | [FAQ →](FAQ.md)

Modern Tideborne consolidates systems that historically existed as separate Tide Traits, Tide Team Journal, and Tidebound Compatibility modules. The 2.1.0 JAR declares compatibility aliases for all three historical mod IDs so existing worlds can keep stable serialized identifiers without requiring the old standalone JARs.

## Before updating

Always make a world backup before a major Tideborne update, especially when the world contains valuable specimens, team records, Satchels, or long-running Journal data.

For Tideborne 2.1.0, confirm that the server and clients use:

- Minecraft 1.21.1
- Java 21+
- Tide 2.1.1
- the required Fabric dependencies listed in [Getting Started](GETTING_STARTED.md)

## Moving from older modular builds

Remove older standalone JARs for:

- Tide Traits
- Tide Team Journal / Multiplayer Extras
- Tidebound Compatibility
- older Tideborne versions

Then install only the current Tideborne JAR plus its real dependencies.

Do **not** run duplicate historical modules beside Tideborne 2.1.0. The modern JAR already provides the old mod IDs where required for compatibility.

## Why old namespaces still appear

You may still see IDs such as:

```text
tide_traits:...
tide_team_journal:...
tidebound_compatibility:...
```

This is intentional. Registry IDs, component IDs, network IDs, NBT keys, recipes, and other serialized identifiers can become part of a saved world. Renaming them just to match the current Java package would risk breaking old items and saves.

A good example is the current **Iron Leader**, which intentionally retains the registry ID:

```text
tidebound_compatibility:steel_leader
```

The visible name changed. The compatibility-sensitive ID did not.

## Specimen migration

Fishing System 2.0 includes deterministic one-way migration paths for recoverable older specimens and saved-data representations. Legacy compatibility code is allowed to read old state, but current canonical specimens should not be rerolled or overwritten by an old compatibility path.

This matters because a migrated fish should remain the same historical catch as closely as the stored data allows.

## Journal and record migration

Older personal or team progress can be imported or repaired through Tideborne's Journal migration and ownership tools. Useful commands include:

```text
/ttj merge
/ttj status
/ttj claim <largest|smallest>
/ttj assign <largest|smallest> <online-member>
/ttj claimall [online-member]
```

Record recovery is designed not to replay normal live-catch side effects as if an old record had just been caught again.

## Configuration migration

Tideborne exposes a unified `config/tideborne.json` configuration while retaining migration support for historical settings. If an old installation has multiple subsystem configuration files, let Tideborne's migration path import supported values rather than manually renaming every old key.

Use the main Tideborne status and migration tooling to confirm the resulting state.

## Safe update checklist

1. Back up the world and configuration folder.
2. Stop the server or game.
3. Remove old Tideborne and standalone legacy module JARs.
4. Confirm Tide is exactly 2.1.1 for Tideborne 2.1.0.
5. Install Tideborne 2.1.0 and the required dependencies.
6. Keep optional Myths/Apex mods only if you want those integrations.
7. Start the world or server.
8. Check `/tideborne status`.
9. Inspect an older valuable fish, an Angler's Satchel, and the Team Journal before normal play resumes.
10. Keep the backup until you have confirmed records and stored specimens look correct.

## Dedicated servers

Install Tideborne on both sides for normal multiplayer use. The server owns canonical catch state and progression, while the client supplies the screens and presentation required for the full experience.

## Reporting a migration problem

When reporting an issue, include:

- Tideborne version before the update
- Tideborne version after the update
- Tide version
- Minecraft/Fabric versions
- whether the world is singleplayer or dedicated server
- which optional integrations are installed
- whether the problem affects the fish item, Satchel, personal history, team data, or display
- a copy of the log and the world backup state if available

Do not repeatedly load and save the only copy of a world while diagnosing a suspected migration problem.
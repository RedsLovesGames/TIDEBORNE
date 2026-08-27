# Tideborne development instructions

This file is the first stop for humans and coding agents.

## Project goal

Maintain Tideborne as a readable, testable Fabric 1.21.1 addon for Tide 2. The current branch reconstructs the authoritative 1.3.57 behavior before any new fishing-system redesign is implemented.

## Source of truth

During reconstruction, authority is ordered as follows:

1. The supplied Tideborne 1.3.57 release JAR.
2. The supplied Tide 2.1.1 Fabric JAR for Tide APIs and runtime behavior.
3. Existing persisted NBT/config/network formats recovered from bytecode and resources.
4. Decompiled source only as a reconstruction aid. Decompiled source is not automatically correct or readable.

Do not invent behavior to fill a decompiler gap. Mark uncertainty and recover it from bytecode, resources, or tests.

## Non-negotiable reconstruction rule

Do not implement the planned Fishing System 2.0 while reconstructing or refactoring 1.3.57. Preserve behavior first. Structural refactors are allowed only when validated against the baseline.

## Build commands

```bash
./gradlew build
./gradlew test
./gradlew printBuildBaseline
```

For exact local Tide API validation, place the supplied JAR at:

```text
dev/libs/tide-fabric-1.21.1-2.1.1.jar
```

Then run:

```bash
./gradlew verifyExactDependencies build
```

Optional Apex Waters compatibility source requires:

```text
dev/libs/apex-waters-fabric-1.21.1-1.1.1.jar
```

## Package ownership

Keep these domains separated:

- `com.redslovesgames.tideborne`: composition root, unified config, migration, commands, cross-module facade.
- `com.redslovesgames.tidetraits`: specimen traits, size, discovery, rendering, Angler's Satchel.
- `com.redslovesgames.tideteamjournal`: shared/team journal, records, scoreboards, record events.
- `com.redslovesgames.tideboundcompatibility`: Tide equipment and optional-mod integration, including Apex Waters.

Do not create new circular dependencies between those domains.

## Refactor direction

Prefer this dependency direction:

```text
entrypoints / mixins / networking / screens
                 |
                 v
application services / domain services
                 |
                 v
domain models / pure calculations
                 |
                 v
persistence and external compatibility adapters
```

Minecraft classes should not leak into pure calculation code unless they are genuinely required.

## Persistence safety

Treat all of these as compatibility-sensitive public APIs:

- NBT keys
- saved-state file names
- config keys
- network payload IDs and wire fields
- component IDs
- item/entity IDs
- Fabric entrypoint class names
- mixin config names
- command names and permissions

Any change requires either backward compatibility or an explicit migration.

## Mixin policy

Mixins must be thin adapters. A mixin should capture/redirect/inject and then delegate to a named service. Do not place large gameplay algorithms inside mixin classes.

Every mixin should document:

- target class and method
- why a normal Fabric event/API is insufficient
- expected failure mode if Tide changes
- whether the mixin is required or optional

## Client/server policy

Server owns authoritative gameplay state. Client code may render, cache, preview, and request actions, but must not be the source of truth for score, traits, records, inventory, XP, or team progression.

## Compatibility policy

Optional integrations live behind a compatibility boundary. Loading Tideborne without the optional mod must never resolve that mod's classes eagerly.

## Naming policy

Replace decompiler names and generic utility names as they are understood. Prefer names that state domain intent, for example:

- `SpecimenTraitRoller`
- `FishPercentileService`
- `SatchelUpgradeService`
- `TeamRecordService`

Avoid new `Utils`, `Helper`, `Manager2`, `Misc`, or catch-all classes.

## Function size and responsibilities

As a default target:

- keep public methods focused on one operation
- extract calculations from screen/mixin/network code
- keep config parsing separate from gameplay logic
- keep serialization separate from domain decisions

Large legacy methods may remain temporarily during reconstruction, but each should be listed in `docs/CURRENT_STATE.md` as a refactor target.

## Validation before merging

At minimum:

1. `./gradlew build`
2. unit tests pass
3. Fabric GameTests pass when applicable
4. client starts with Tide 2.1.1
5. dedicated server starts with Tide 2.1.1
6. no optional-mod classloading crash when Apex/Myths are absent
7. saved data from 1.3.57 loads without loss
8. network payload registration succeeds on both sides
9. no mixin application errors

See `docs/VALIDATION.md` for the full gate.

## Working-state discipline

After every meaningful development pass update `docs/CURRENT_STATE.md` with:

- exact baseline version
- branch/commit
- what is verified
- what is still inferred
- build status
- next prioritized tasks

This is intentionally redundant with issue tracking so a future agent can resume with a short prompt.

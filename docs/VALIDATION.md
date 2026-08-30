# Validation gates

## Current 2.0.0 CI gate

`.github/workflows/build.yml` is the single active validation and release workflow for
the maintained `dev` branch. It runs the following required gates in order:

1. exact checksum verification for Tide, Apex Waters, Myths of the Sea, CERBON API,
   and GeckoLib;
2. repository metadata, entrypoint, mixin, and GameTest-registration validation;
3. clean Java 21 build and the complete JUnit suite;
4. Fabric GameTests with no optional mods, Apex only, Myths only, and both mods;
5. production JAR validation and artifact upload;
6. a release-only publish job with `contents: write`, after validation succeeds on a
   `dev` push.

The dedicated-server and client-connect smoke harness continues to collect diagnostics,
but its known runner-only client-connect failure is non-blocking. It must not prevent the
required build, GameTest, artifact-validation, artifact-upload, or release-publish gates.

The completed reconstruction workflows and the one-off release workflow were retired after
their historical work completed. The reconstruction scripts remain in the repository as
recovery tooling and are not part of continuous validation.

## Historical reconstruction gates

Tideborne reconstruction is not complete just because Java compiles. The 1.3.57 baseline mixes persistence, networking, UI, mixins, Tide internals, and optional compatibility, so validation is layered.

## Gate 1: static project checks

Required:

```bash
./gradlew clean build
```

The build must use Java 21 and Minecraft 1.21.1.

Also verify:

- no unresolved `class_*`, `method_*`, or `field_*` identifiers remain in maintained source
- no decompiler error comments remain unexplained
- no duplicate top-level classes
- all `fabric.mod.json` entrypoints resolve
- all mixin classes listed in mixin JSON exist
- all required resources referenced by code exist

## Gate 2: exact dependency compile

Place the authoritative Tide 2.1.1 Fabric JAR at:

```text
dev/libs/tide-fabric-1.21.1-2.1.1.jar
```

Then run:

```bash
./gradlew verifyExactDependencies build
```

The exact dependency artifact must match the recorded SHA-256 in `docs/RECONSTRUCTION.md`.

## Gate 3: client smoke test

Run:

```bash
./gradlew runClient
```

Verify:

- title screen reaches a usable state
- no mixin application errors
- no missing entrypoint errors
- Tideborne config opens
- a world can load
- Tide journal opens
- Angler's Satchel opens
- specimen tooltips render
- mutation/trait textures render

## Gate 4: dedicated server smoke test

Run:

```bash
./gradlew runServer
```

Verify:

- server reaches ready state
- no client-only class is loaded server-side
- Tideborne payloads register
- saved-state services initialize
- commands register
- no optional-mod classes are resolved when optional mods are absent

## Gate 5: GameTests and characterization tests

Minimum characterization coverage before structural refactoring:

### Specimen domain

- percentile lookup is deterministic for the same fish data
- Giant qualification and multiplier behavior
- Dwarf qualification and multiplier behavior
- Perfect Specimen qualification
- Condition selection ordering/probabilities
- Perfect Catch trait-luck behavior
- physical length transformation
- specimen NBT round trip

### Satchel

- capacity per level/upgrade
- XP thresholds
- insert/extract rules
- protected fish behavior
- sort behavior
- upgrade migration
- NBT round trip

### Team journal and records

- personal-to-team merge behavior
- team record replacement rules
- FishScore comparison
- record holder persistence
- event history/badge creation
- migration from legacy keys

### Tide compatibility

- Leviathan Bait fish-only selection path
- Leviathan catch-area modifier
- Leviathan speed modifier
- line/hook modifier stacking
- Steel Leader loss protection
- chum state lifecycle

## Gate 6: persistence fixtures

Create fixtures from actual 1.3.57 output for:

- player specimen data
- Angler's Satchel NBT
- team progress state
- record holders
- event history
- config files

A refactor is rejected if it cannot load these fixtures without unintentional data loss.

## Gate 7: network compatibility

Inventory every custom payload ID and field order. For each payload:

- encode/decode round trip
- server rejects malformed/unauthorized actions
- client does not authoritatively change server-owned state
- registration exists on the correct environment

## Gate 8: mixin contract checks

Every mixin must have a short contract comment describing target and purpose.

During runtime smoke tests verify:

- zero failed required injections
- zero unexpected target warnings
- optional Apex mixin config skips safely without Apex Waters
- Tide 2.1.1 target signatures still match

## Refactor merge rule

A refactor should change structure without changing observable 1.3.57 behavior unless its pull request explicitly declares and tests a behavior fix.

The future Fishing System 2.0 is intentionally exempt from behavioral equivalence, but it should branch only after these baseline gates are in place.

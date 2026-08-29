# Stages 57-58: Final dedicated-server and release validation

Date: 2026-08-29

## Release target

- branch: `dev`
- Minecraft: 1.21.1
- Java: 21
- Tide: 2.1.1
- Tideborne: 2.0.0
- production artifact: `build/libs/tideborne-2.0.0.jar`
- GitHub release tag and title: `TIDEBORN-2.0.0`

The release workflow publishes the production artifact only after every gate in this
document succeeds. The release tag therefore identifies the exact validated `dev` commit.

## Dedicated-server and multiplayer authority gate

The final gate combines runtime smoke coverage with deterministic server-side and network
boundary tests:

- a real Fabric dedicated server must reach its ready state and stop cleanly;
- a real Fabric client must start separately and join that dedicated server;
- normal fishing must select a Tide fish through the canonical Fishing System 2.0 bridge;
- the selected specimen must match the server-owned transient catch state and carry server
  authority provenance;
- edits to legacy/client-facing item mirrors must not overwrite canonical specimen state;
- client-to-server payload types must not accept canonical `SpecimenData`, Trait Momentum,
  or authored fish `ItemStack` state;
- Trait Momentum remains attached to `ServerPlayerEntity` persistence and cannot be changed
  through specimen/item state;
- team Journal requests are projections from server-owned Journal state, and record-holder,
  event, leaderboard, discovery, settings, and Satchel responses are produced server-side;
- existing canonical networking, Journal, record, migration, transfer, and multi-recipient
  projection tests must pass in all optional-mod runtime matrices;
- dedicated-server startup must not load client-only entrypoints or fail with client-class,
  mixin, or initializer errors.

The live client-connect smoke uses one graphical client. Cross-client consistency is covered
by the server-side team/discovery broadcast and canonical projection tests rather than by
automating two graphical clients.

## Defects found and fixed

Final inspection found five GameTest classes that existed but were not registered in
`fabric.mod.json`. They are now registered, so canonical pigmentation, item storage,
entity transfer, legacy bucket migration, and legacy Journal persistence coverage executes
in every GameTest matrix leg.

The release metadata was also still reporting 1.3.57. Gradle resource expansion, runtime
status strings, the debug function, and user-facing build documentation now consistently
report 2.0.0.

The final authority coverage adds a normal-fishing GameTest and a unit test freezing the
client-to-server payload boundary. Repository and release-artifact validators were added so
missing GameTest registrations, missing mixin/entrypoint sources, unresolved intermediary
identifiers, client-only common-entrypoint imports, wrong version metadata, or a dev/source
artifact fail the release gate.

## Complete release gate

`.github/workflows/build.yml` performs the following against the release commit:

1. fetch and verify the exact required and optional dependency artifacts;
2. run reconstruction and repository validation scripts;
3. run `./gradlew clean build --stacktrace`, including the complete JUnit suite;
4. run the complete Fabric GameTest suite with no optional mods, Apex only, Myths only,
   and Apex plus Myths;
5. start a dedicated server and connect a separately launched client;
6. verify that `build/libs/tideborne-2.0.0.jar` contains production classes and expanded
   2.0.0 metadata and is not a source or development artifact;
7. upload build artifacts and publish `TIDEBORN-2.0.0` from the exact successful `dev` SHA.

## Runtime authority conclusion

Canonical Fishing System 2.0 selection, specimen generation, finalization, persistence,
score, transfer, Journal, and record paths are authoritative. Remaining legacy code is
restricted to one-time migration, representation mirrors, or guarded fallback for old or
noncanonical data. No known duplicate legacy fishing calculation path remains active for a
canonical V2 catch.

There are no known Fishing System 2.0 release blockers. The supported 2.0.0 scope remains
Minecraft 1.21.1, Java 21, Tide 2.1.1, Apex Waters 1.1.1, and Myths of the Sea 1.3.0.

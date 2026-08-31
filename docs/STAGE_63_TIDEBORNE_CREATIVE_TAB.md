# Stage 63 - Tideborne Creative Tab

Updated: 2026-08-31

## Goal

Give Tideborne-owned items one dedicated Creative Mode tab without renaming or re-registering any existing item IDs.

## Creative tab

- registry ID: `tideborne:tideborne`
- display name: `Tideborne`
- icon: `tide_traits:anglers_satchel`
- registration is common-side and is initialized only after the Satchel and Tideborne compatibility items have been registered.
- the runtime entries and validation tests consume the same immutable visibility/order contract so the tested matrix cannot drift from the actual creative presentation.

## Item ownership and order

The tab is intentionally curated by gameplay role rather than raw registry order:

1. Angler's Satchel
2. Kujira Bone Fishing Rod, when Myths of the Sea integration is active
3. Tentacle Line and Swift Line, when Myths of the Sea integration is active
4. Steel Leader, when Apex Waters integration is active
5. Seafarer's Hook, when Myths of the Sea integration is active
6. Shark Tooth Hook, when Apex Waters integration is active
7. Leviathan Bait, when Myths of the Sea integration is active
8. Chum Bucket and Shark Tooth, when Apex Waters integration is active

The optional-mod visibility rules are deliberately preserved from the pre-Stage-63 vanilla creative-group injections. Items belonging to an inactive optional integration remain registered but are not advertised in the Tideborne creative tab.

The exact expected matrices are:

- no optional integrations: Satchel only;
- Myths only: Satchel, Kujira rod, Tentacle Line, Swift Line, Seafarer's Hook, Leviathan Bait;
- Apex only: Satchel, Steel Leader, Shark Tooth Hook, Chum Bucket, Shark Tooth;
- Myths + Apex: Satchel, Kujira rod, Tentacle Line, Swift Line, Steel Leader, Seafarer's Hook, Shark Tooth Hook, Leviathan Bait, Chum Bucket, Shark Tooth.

## Duplicate removal

Stage 63 removes Tideborne-owned presentation from vanilla creative groups:

- the Angler's Satchel is no longer injected into vanilla `TOOLS` by the traits subsystem;
- Tideborne compatibility rods, lines, leaders, hooks, and utility items are no longer injected into vanilla `TOOLS`;
- Leviathan Bait and Shark Tooth are no longer injected into vanilla `INGREDIENTS`.

Native Tide items and Tide's own creative presentation are not modified.

## Compatibility guarantees

- no item registry ID changes;
- no recipe changes;
- no saved-item migration is required;
- no Fishing System 2.0 gear profile IDs change;
- creative-tab registration does not reference client-only classes and remains dedicated-server safe.

## Validation

Final validated Stage 63 implementation head:

- `24cc3a42e30f9dc8a51bf9abef469585e0d2b48b` - `test: lock creative tab compatibility matrix`

GitHub Actions run `33360256194` is green on that head. It passed:

- exact external dependency retrieval and repository/release-metadata validation;
- Java 21 Gradle build and unit tests;
- Fabric GameTests with no optional compatibility mods;
- Fabric GameTests with Apex Waters only;
- Fabric GameTests with Myths of the Sea only;
- Fabric GameTests with Apex Waters and Myths of the Sea together;
- dedicated-server/client-connect smoke validation;
- production `tideborne-2.0.0.jar` validation;
- final validation-count checks;
- built-JAR artifact upload;
- `TIDEBORN-2.0.0` release publication/refresh.

The registered Stage 63 GameTests now verify:

- `tideborne:tideborne` is registered at runtime;
- the icon is the actual registered Angler's Satchel item;
- all four Myths/Apex visibility combinations have the exact expected item count, identity, and stable order;
- the live creative-item list follows the actual optional-integration activation flags.

The refreshed `TIDEBORN-2.0.0` release targets
`24cc3a42e30f9dc8a51bf9abef469585e0d2b48b`. Its published `tideborne-2.0.0.jar` has SHA-256
`ff9ef2a8f8aa975336816ae56240302c31dcc777606bb1a88d23c947b799b937`.

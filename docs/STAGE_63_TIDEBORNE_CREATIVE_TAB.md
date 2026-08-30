# Stage 63 - Tideborne Creative Tab

Updated: 2026-08-30

## Goal

Give Tideborne-owned items one dedicated Creative Mode tab without renaming or re-registering any existing item IDs.

## Creative tab

- registry ID: `tideborne:tideborne`
- display name: `Tideborne`
- icon: `tide_traits:anglers_satchel`
- registration is common-side and is initialized only after the Satchel and Tideborne compatibility items have been registered.

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

A Fabric GameTest verifies that the `tideborne:tideborne` item group is registered at runtime and uses the actual Angler's Satchel item as its icon. The normal repository workflow additionally validates Java compilation, unit tests, all optional-mod GameTest matrices, dedicated-server/client-connect smoke behavior, and the production JAR.

# P7.5 Compatibility Alias Audit

Branch: `agent/p7-resources`

Baseline: `b2ea30d927c4bae15f2e9ccbdc48b61331ead9f4`

P7 separates current resource ownership from persisted or externally addressable identity. Current presentation and Tide-owned flat data live under Tideborne ownership. Historical namespaces remain only where Minecraft, Fabric, saved data, network peers, datapacks, recipes, tags, registry identity, or an explicitly published command path can observe the old identifier.

## `tide_traits`

Retained runtime resource surface:

- `assets/tide_traits/lang/en_us.json`: automatic `item.tide_traits.anglers_satchel` naming plus translations for the eight historical public exclusion tags.
- `assets/tide_traits/models/item/anglers_satchel*.json`: four model aliases required by the persisted item ID `tide_traits:anglers_satchel`. Their physical textures are Tideborne-owned.
- `data/tide_traits/recipe/anglers_satchel.json`: retained as recipe ID `tide_traits:anglers_satchel`; recipe IDs are externally addressable.
- `data/tide_traits/tags/item/*_excluded.json`: eight public datapack extension tags retained at their historical IDs.
- `data/tide_traits/function/test_tuna_mutations.mcfunction`: retained as a one-line compatibility alias because the packaged historical note `TIDE_TRAITS_LITERAL_OVERLAY_V3.txt` explicitly advertised `/function tide_traits:test_tuna_mutations`. The actual diagnostic implementation now lives at `data/tideborne/function/test_tuna_mutations.mcfunction`.
- `fabric.mod.json` provides `tide_traits`: retained so dependencies written against the historical internal-mod ID continue to resolve Tideborne.

Historical non-resource identities also remain in Java where compatibility requires them, including the Satchel item/component family, specimen/discovery payload or persistence identifiers, and the `tide_traits:mutation_textures` reload-listener ID. P8 owns centralization of those identifiers; P7 does not rename them.

## `tide_team_journal`

No `assets/tide_team_journal` or `data/tide_team_journal` resource tree remains. Current journal presentation lives under Tideborne assets and Tideborne translation keys.

The historical `tide_team_journal` Fabric `provides` alias remains for dependency compatibility. Journal network and persistence IDs using this namespace are compatibility identities and are intentionally deferred to P8.

## `tidebound_compatibility`

Retained runtime resource surface:

- `assets/tidebound_compatibility/lang/en_us.json`: automatic historical item names/descriptions plus translations for retained public tag IDs.
- `assets/tidebound_compatibility/models/item/*.json`: historical model aliases required by persisted `tidebound_compatibility:*` item IDs. Physical textures are Tideborne-owned.
- `data/tidebound_compatibility/recipe/*.json`: eleven historical recipe IDs retained unchanged because recipes are externally addressable.
- `data/tidebound_compatibility/tags/item/*.json`: eight public/historical integration tags retained unchanged for datapacks and optional integrations.
- `fabric.mod.json` provides `tidebound_compatibility`: retained for dependency checks against the historical internal-mod ID.

Current Leviathan bait and rod-accessory flat data do not live here. P7.2 moved the bundled records to `data/tideborne/bait` and `data/tideborne/rod_accessories`. Tide 2.1.1 scans those directories across namespaces, so third-party packs using the historical namespace remain loadable without duplicate bundled records.

Historical item, entity, payload, config-file, and other persisted/runtime IDs using `tidebound_compatibility:*` remain compatibility identities. P8 owns their centralization and any future migration strategy.

## Non-historical foreign namespaces intentionally retained

- `assets/tide/**`: upstream Tide renderer extension textures. Tide owns the lookup path.
- `data/tide/**`: upstream Tide tag extension points.
- `data/myths_of_the_sea/**`: optional Myths of the Sea integration resources.
- `data/c/**`: common interoperability tags.
- `data/minecraft/**`: vanilla-owned extension points.

These are not compatibility aliases and must not be folded into `tideborne` for cosmetic uniformity.

## P7.5 invariant

Historical asset namespaces are alias-only. Historical data namespaces contain only externally observable compatibility surfaces: recipes, tags, and the published diagnostic-function forwarding alias. No current presentation texture, current UI translation family, Tide flat-data record, dynamic mutation texture, mixin config, or general refmap remains historically owned.

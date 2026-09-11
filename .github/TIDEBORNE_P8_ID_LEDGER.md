# P8 Historical ID Ledger

This ledger is the classification source of truth for P8. Preserve exact externally observable ID values. P8 centralizes ownership; it does not erase compatibility history.

Status key: `KNOWN_COMPAT`, `ENUMERATED`, `CENTRALIZED`, `VERIFIED`, `NOT_P8`.

| Namespace/family | Exact values / known surface | Classification | Status | P8 owner/action |
|---|---|---|---|---|
| `tide_traits` Satchel registry IDs | `tide_traits:anglers_satchel`, `tide_traits:satchel_state` | PERSISTED_COMPAT | CENTRALIZED | `LegacyRegistryIds`; preserve values |
| `tide_traits` data-component family | `mutation`, `mutation_seed`, `size_percentile`, `protected`, `body_type`, `steel_leader_attached`, `leader_tier`, `specimen_species_id`, `specimen_schema_version`, `specimen_generation_version`, `specimen_deterministic_seed`, `specimen_base_percentile`, `specimen_base_length`, `specimen_final_length`, `specimen_final_percentile`, `specimen_percentile_definition`, `specimen_body_type`, `specimen_condition`, `specimen_pigmentation`, `specimen_quality`, `specimen_perfect_catch`, `specimen_raw_fish_score`, `specimen_fish_score` | PERSISTED_COMPAT | CENTRALIZED | `LegacyRegistryIds.tideTraitsComponent`; preserve every serialized ID |
| `tide_traits` network IDs | `discovery_sync`, `shared_discovery_request`, `shared_discovery_sync`, `anglers_satchel_request`, `anglers_satchel_view` | WIRE_COMPAT | CENTRALIZED | `LegacyNetworkIds`; preserve wire values |
| `tide_traits` mutation/specimen lookup IDs | mutation IDs constructed as `tide_traits:<mutation path>`, including historical `tide_traits:normal` lookup | MIGRATION_LOOKUP / PERSISTED_COMPAT | CENTRALIZED | `LegacyServiceIds`; do not rename old serialized mutation IDs |
| `tide_traits` shared-discovery NBT key | nested `tide_traits` under historical team-journal root | PERSISTED_COMPAT | CENTRALIZED | `LegacyPersistenceIds.SHARED_DISCOVERY_TRAITS` |
| `tide_traits:mutation_textures` | reload-listener external identity | PUBLIC_EXTENSION | CENTRALIZED | `LegacyServiceIds.MUTATION_TEXTURES_RELOAD_LISTENER` |
| `tide_traits` recipe/tag/function aliases retained by P7 | `anglers_satchel` recipe, `*_excluded` public item tags, `test_tuna_mutations` forwarding function | PUBLIC_EXTENSION | KNOWN_COMPAT | P7 resources stay in place; Java tag construction routes through `LegacyServiceIds` |
| `tide_traits.json` | historical server/specimen config filename | MIGRATION_LOOKUP | CENTRALIZED | `LegacyPersistenceIds.TRAITS_CONFIG` |
| Fabric `provides: tide_traits` | historical dependency identity | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; regression-test |
| `tide_team_journal` network IDs | `record_holders`, `team_data`, `record_event`, `bobber_settings`, `open_team_records`, `team_data_request` | WIRE_COMPAT | CENTRALIZED | `LegacyNetworkIds`; preserve wire values |
| `tide_team_journal` persistence keys | `tide_team_journal`, `tide_team_journal_canonical_specimens`, `tide_team_journal_record_holders` | PERSISTED_COMPAT | CENTRALIZED | `LegacyPersistenceIds` |
| `tide_team_journal` config filenames | `tide_team_journal-server.json`, `tide_team_journal-client.json` | MIGRATION_LOOKUP | CENTRALIZED | `LegacyPersistenceIds` |
| Fabric `provides: tide_team_journal` | historical dependency identity | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; regression-test |
| `tidebound_compatibility` item registry IDs | `tentacle_line`, `seafarers_hook`, `swift_line`, `kujira_bone_fishing_rod`, `leviathan_bait`, `chum_bucket`, `copper_leader`, `steel_leader`, `gold_leader`, `diamond_leader`, `shark_tooth`, `shark_tooth_hook` | PERSISTED_COMPAT | CENTRALIZED | `LegacyRegistryIds`; preserve values |
| `tidebound_compatibility` entity ID | `chum_projectile` | PERSISTED_COMPAT | CENTRALIZED | `LegacyRegistryIds.CHUM_PROJECTILE` |
| `tidebound_compatibility` fishing-gear profile IDs | same historical gear registry IDs consumed by `FishingGearRegistry` | PERSISTED_COMPAT | CENTRALIZED | namespace routes through `LegacyNamespaces`; registered IDs remain frozen by `LegacyRegistryIds` and regression tests |
| `tidebound_compatibility` payload IDs | `settings`, `settings_result`, `settings_update`, `shark_catch_loss` | WIRE_COMPAT | CENTRALIZED | `LegacyNetworkIds` |
| `tidebound_compatibility` config filenames | `tidebound_compatibility.json`, `tidebound_compatibility-client.json` | MIGRATION_LOOKUP | CENTRALIZED | `LegacyPersistenceIds` |
| `tidebound_compatibility` historical item translation keys | `item.tidebound_compatibility.*` descriptions tied to persisted item IDs | PERSISTED_COMPAT / PUBLIC_EXTENSION | KNOWN_COMPAT | remain at item construction sites because Minecraft's automatic historical item translation surface depends on those persisted IDs |
| `tidebound_compatibility` recipe/tag aliases retained by P7 | eleven historical recipes and eight public/integration tags | PUBLIC_EXTENSION | KNOWN_COMPAT | do not move P7 resources |
| Fabric `provides: tidebound_compatibility` | historical dependency identity | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; regression-test |
| `tide:*` IDs | upstream Tide extension points | UPSTREAM_FOREIGN | NOT_P8 | leave feature/integration-owned |
| `myths_of_the_sea:*` IDs | optional foreign integration | UPSTREAM_FOREIGN | NOT_P8 | leave integration-owned |
| `minecraft:*` / `c:*` | vanilla/common interoperability | UPSTREAM_FOREIGN | NOT_P8 | leave unchanged |

## Centralized owners

- `LegacyNamespaces`: the three frozen historical namespace strings and Identifier factories.
- `LegacyRegistryIds`: persisted Satchel, component, Tidebound item, and entity registry identities.
- `LegacyNetworkIds`: all audited historical TideTraits, team-journal, and Tidebound payload/channel identifiers.
- `LegacyPersistenceIds`: historical NBT/save projection keys and legacy config filenames.
- `LegacyServiceIds`: mutation lookup IDs, public historical TideTraits item-tag construction, and the mutation-texture reload-listener identity.

Normal feature code references these owners instead of constructing raw historical namespace identifiers. The owners intentionally live below `migration.legacy.ids` so compatibility history is explicit rather than appearing canonical.

## Exact preserved values

The exact values above are frozen compatibility inputs for P8. `HistoricalIdOwnershipTest` regression-tests representative registry, wire, persistence, service, namespace, Satchel-state, and every historical TideTraits component path. Existing fishing tests retain exact Tidebound gear IDs.

## Remaining intentional historical literals at closure

- `src/main/java/com/redslovesgames/tideborne/migration/legacy/ids/**`: authoritative frozen compatibility definitions by design.
- `src/main/java/com/redslovesgames/tideborne/fishing/gametest/**`: exact-value GameTest fixtures proving old registry/mod identities still resolve. These are test code despite living in the GameTest production source set.
- `src/main/java/com/redslovesgames/tideborne/registry/TideboundItems.java`: historical `item.tidebound_compatibility.*.desc` translation keys required by the persisted historical item registry family and retained P7 language aliases.
- `src/main/resources/fabric.mod.json`: the three historical `provides` aliases retained for dependency compatibility.
- P7 compatibility alias resources under `assets/tide_traits`, `assets/tidebound_compatibility`, `data/tide_traits`, and `data/tidebound_compatibility`: externally addressable compatibility surfaces owned by P7's explicit alias boundary.
- Source/architecture/regression tests and P8/P7 documentation/scripts: literals are assertions, audit tokens, compatibility documentation, or migration tooling, not normal runtime ownership.

All other normal production Java was audited after centralization and contains no raw quoted `tide_traits`, `tide_team_journal`, or `tidebound_compatibility` namespace owner literal.

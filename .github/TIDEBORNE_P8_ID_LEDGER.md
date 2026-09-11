# P8 Historical ID Ledger

This ledger is the classification source of truth for P8. Preserve exact externally observable ID values. P8 centralizes ownership; it does not erase compatibility history.

Status key: `KNOWN_COMPAT`, `ENUMERATED`, `CENTRALIZED`, `VERIFIED`, `NOT_P8`.

| Namespace/family | Exact values / known surface | Classification | Status | P8 action |
|---|---|---|---|---|
| `tide_traits` Satchel registry ID | `tide_traits:anglers_satchel` | PERSISTED_COMPAT | ENUMERATED | centralize registry identity, preserve value |
| `tide_traits` data-component family | `mutation`, `mutation_seed`, `size_percentile`, `protected`, `body_type`, `steel_leader_attached`, `leader_tier`, `specimen_species_id`, `specimen_schema_version`, `specimen_generation_version`, `specimen_deterministic_seed`, `specimen_base_percentile`, `specimen_base_length`, `specimen_final_length`, `specimen_final_percentile`, `specimen_percentile_definition`, `specimen_body_type`, `specimen_condition`, `specimen_pigmentation`, `specimen_quality`, `specimen_perfect_catch`, `specimen_raw_fish_score`, `specimen_fish_score` | PERSISTED_COMPAT | ENUMERATED | centralize namespace/registry ownership, preserve every serialized ID |
| `tide_traits` network IDs | `discovery_sync`, `shared_discovery_request`, `shared_discovery_sync`, `anglers_satchel_request`, `anglers_satchel_view` | WIRE_COMPAT | ENUMERATED | centralize payload identifiers, preserve wire values |
| `tide_traits` mutation/specimen lookup IDs | mutation IDs constructed as `tide_traits:<mutation path>`, including historical `tide_traits:normal` lookup | MIGRATION_LOOKUP / PERSISTED_COMPAT | ENUMERATED | route through explicit legacy identity owner; do not rename old serialized mutation IDs |
| `tide_traits` shared-discovery NBT key | nested `tide_traits` under historical team-journal root | PERSISTED_COMPAT | ENUMERATED | centralize key ownership |
| `tide_traits:mutation_textures` | reload-listener external identity | PUBLIC_EXTENSION | ENUMERATED | preserve exact listener ID; centralize owner |
| `tide_traits` recipe/tag/function aliases retained by P7 | `anglers_satchel` recipe, `*_excluded` public item tags, `test_tuna_mutations` forwarding function | PUBLIC_EXTENSION | KNOWN_COMPAT | do not move P7 resources; centralize Java tag construction where used |
| `tide_traits.json` | historical server/specimen config filename | MIGRATION_LOOKUP | ENUMERATED | centralize legacy filename used by config import/backup |
| Fabric `provides: tide_traits` | historical dependency identity | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; regression-test |
| `tide_team_journal` network IDs | `record_holders`, `team_data`, `record_event`, `bobber_settings`, `open_team_records`, `team_data_request` | WIRE_COMPAT | ENUMERATED | centralize payload identifiers, preserve wire values |
| `tide_team_journal` persistence keys | `tide_team_journal`, `tide_team_journal_canonical_specimens`, `tide_team_journal_record_holders` | PERSISTED_COMPAT | ENUMERATED | centralize NBT/network projection key ownership |
| `tide_team_journal` config filenames | `tide_team_journal-server.json`, `tide_team_journal-client.json` | MIGRATION_LOOKUP | ENUMERATED | centralize legacy filenames used by config import/backup |
| Fabric `provides: tide_team_journal` | historical dependency identity | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; regression-test |
| `tidebound_compatibility` item registry IDs | `tentacle_line`, `seafarers_hook`, `swift_line`, `kujira_bone_fishing_rod`, `leviathan_bait`, `chum_bucket`, `copper_leader`, `steel_leader`, `gold_leader`, `diamond_leader`, `shark_tooth`, `shark_tooth_hook` | PERSISTED_COMPAT | ENUMERATED | centralize registry identifiers, preserve values |
| `tidebound_compatibility` entity ID | `chum_projectile` | PERSISTED_COMPAT | ENUMERATED | centralize registry identifier, preserve value |
| `tidebound_compatibility` fishing-gear profile IDs | same historical gear registry IDs consumed by `FishingGearRegistry` | PERSISTED_COMPAT | ENUMERATED | reference centralized registry IDs instead of repeating namespace literals |
| `tidebound_compatibility` payload IDs | `settings`, `settings_result`, `settings_update`, `shark_catch_loss` | WIRE_COMPAT | ENUMERATED | centralize payload identifiers, preserve wire values |
| `tidebound_compatibility` config filenames | `tidebound_compatibility.json`, `tidebound_compatibility-client.json` | MIGRATION_LOOKUP | ENUMERATED | centralize legacy filenames used by config import/backup |
| `tidebound_compatibility` historical item translation keys | `item.tidebound_compatibility.*` descriptions tied to persisted item IDs | PERSISTED_COMPAT / PUBLIC_EXTENSION | ENUMERATED | leave translation compatibility semantics intact; avoid treating display aliases as canonical current ownership |
| `tidebound_compatibility` recipe/tag aliases retained by P7 | eleven historical recipes and eight public/integration tags | PUBLIC_EXTENSION | KNOWN_COMPAT | do not move P7 resources; centralize Java references only if present |
| Fabric `provides: tidebound_compatibility` | historical dependency identity | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; regression-test |
| `tide:*` IDs | upstream Tide extension points | UPSTREAM_FOREIGN | NOT_P8 | leave feature/integration-owned |
| `myths_of_the_sea:*` IDs | optional foreign integration | UPSTREAM_FOREIGN | NOT_P8 | leave integration-owned |
| `minecraft:*` / `c:*` | vanilla/common interoperability | UPSTREAM_FOREIGN | NOT_P8 | leave unchanged |

## P8.0 literal inventory notes

The exact branch audit found historical namespace literals in normal production code only in the following ownership categories:

- registry/component construction and gear profile declarations;
- network payload identifiers;
- Satchel/discovery/specimen migration and lookup identifiers;
- Journal/team NBT and network-projection keys;
- historical config import/backup filenames;
- the historical mutation-texture reload-listener ID;
- legacy namespace `MOD_ID`/logger/helper constants left behind after P2/P6 re-ownership;
- Java references to P7-retained public tag identities and historical item translation keys.

GameTest literals that assert exact historical registry values are regression fixtures, not ownership leaks. Architecture/resource tests and `fabric.mod.json` deliberately contain historical literals to prove compatibility aliases remain present.

## Centralized owners

_To be filled phase-by-phase._

## Exact preserved values

The exact values above are frozen compatibility inputs for P8. No phase may change them without an explicit compatibility bridge and regression proof.

## Remaining intentional historical literals at closure

_To be filled during P8.5 with the exact reason each location cannot or should not reference a centralized Java owner, for example Fabric metadata, P7 alias resources, compatibility regression fixtures, or historical translation keys required by persisted registry identities._

# P8 Historical ID Ledger

This ledger is the classification source of truth for P8. Preserve exact externally observable ID values. P8 centralizes ownership; it does not erase compatibility history.

Status key: `KNOWN_COMPAT`, `NEEDS_EXACT_ENUMERATION`, `CENTRALIZED`, `VERIFIED`, `NOT_P8`.

| Namespace/family | Known observer / reason | Required classification | Status | P8 action |
|---|---|---|---|---|
| `tide_traits:anglers_satchel` item ID | persisted ItemStacks / registry identity | PERSISTED_COMPAT | KNOWN_COMPAT | centralize definition/reference ownership, preserve value |
| `tide_traits` Satchel component family | serialized component data | PERSISTED_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact IDs, centralize, regression-test |
| `tide_traits` specimen/discovery payload IDs | network peers / compatibility | WIRE_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact IDs, centralize, preserve wire values |
| `tide_traits` specimen/discovery persistence IDs | old items/worlds | PERSISTED_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact IDs/keys, centralize |
| `tide_traits:mutation_textures` | reload-listener external identity | PUBLIC_EXTENSION | KNOWN_COMPAT | preserve exact ID; give explicit legacy owner if Java-defined |
| `tide_traits` recipe/tag/function aliases retained by P7 | datapacks/commands | PUBLIC_EXTENSION | KNOWN_COMPAT | do not move P7 resources; centralize Java references only if present |
| Fabric `provides: tide_traits` | dependency compatibility | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; document/test |
| `tide_team_journal` network IDs | existing clients/servers | WIRE_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact payload/channel IDs, centralize |
| `tide_team_journal` persistence/team IDs | old worlds/team history | PERSISTED_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact keys/IDs, centralize |
| Fabric `provides: tide_team_journal` | dependency compatibility | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; document/test |
| `tidebound_compatibility` item registry IDs | persisted ItemStacks / models | PERSISTED_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact IDs, centralize, preserve values |
| `tidebound_compatibility` entity IDs | persisted entities/worlds | PERSISTED_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact IDs, centralize |
| `tidebound_compatibility` payload IDs | network compatibility | WIRE_COMPAT | NEEDS_EXACT_ENUMERATION | enumerate exact IDs, centralize |
| `tidebound_compatibility` config compatibility names | existing config/users | PERSISTED_COMPAT or MIGRATION_LOOKUP | NEEDS_EXACT_ENUMERATION | classify exact filenames/keys before any refactor |
| `tidebound_compatibility` recipe/tag aliases retained by P7 | datapacks/integrations | PUBLIC_EXTENSION | KNOWN_COMPAT | do not move P7 resources; centralize Java references only if present |
| Fabric `provides: tidebound_compatibility` | dependency compatibility | DEPENDENCY_ALIAS | KNOWN_COMPAT | preserve in metadata; document/test |
| `tide:*` IDs | upstream Tide extension points | UPSTREAM_FOREIGN | NOT_P8 | leave feature/integration-owned |
| `myths_of_the_sea:*` IDs | optional foreign integration | UPSTREAM_FOREIGN | NOT_P8 | leave integration-owned |
| `minecraft:*` / `c:*` | vanilla/common interoperability | UPSTREAM_FOREIGN | NOT_P8 | leave unchanged |

## Worker rule

Before changing an ID reference, add its exact literal/family here with observer and classification. At P8.5, no `NEEDS_EXACT_ENUMERATION` or `UNCLASSIFIED` production family may remain.

When a family is centralized, record the owning Java class/file and the exact preserved values below.

## Centralized owners

_To be filled phase-by-phase._

## Exact preserved values

_To be filled phase-by-phase. Prefer compact grouped lists; do not paste unrelated resource inventories._

## Remaining intentional historical literals at closure

_To be filled during P8.5 with the exact reason each location cannot/should not reference a centralized Java owner (for example Fabric metadata or resource namespace paths)._

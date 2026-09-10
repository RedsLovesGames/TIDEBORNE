# P7.0 Resource Classification

Branch: `agent/p7-resources`

Dependency baseline: `b2ea30d927c4bae15f2e9ccbdc48b61331ead9f4`

This document records the P7.0 ownership classification before any runtime resource relocation. Resource ownership and persisted identity are treated separately throughout P7.

## Classification meanings

- `CURRENT_MOVE`: active Tideborne presentation or runtime resource whose current ownership should move to the `tideborne` namespace.
- `COMPAT_ALIAS`: historical namespace or path that remains necessary because worlds, registry identities, recipes, tags, datapacks, item models, translations, or external references can depend on it.
- `UPSTREAM_EXTENSION`: resource intentionally installed in an upstream mod namespace because that mod resolves or consumes the resource there.
- `OPTIONAL_INTEGRATION`: resource intentionally attached to an optional foreign mod or classloading boundary.
- `TEST_ONLY`: development or diagnostic resource that is not part of normal production ownership.
- `NON_RUNTIME_DOC`: packaging, license, migration, or historical documentation that P7 does not relocate merely for namespace uniformity.

## Dependency gate

The P7 dependency gate is satisfied on this branch.

- P3 config unification is present under `com.redslovesgames.tideborne.config`, including `TideborneConfig`.
- P5 canonical specimen ownership is present under `fishing.specimen`, including canonical `SpecimenData` and canonical storage, while mutation-era representations are isolated under `fishing.specimen.legacy`.
- P6 gameplay ownership is present under `fishing.gear`, `fishing.tide`, and `ecosystem`.

No P7 production move is permitted to undo those ownership boundaries.

## Client assets

| Surface | Classification | P7 action | Compatibility constraint |
| --- | --- | --- | --- |
| `assets/tide/textures/entity/fishing_hook/*` | `UPSTREAM_EXTENSION` | Retain in the Tide namespace unless the Tide renderer lookup is deliberately changed and validated. | Tide owns the renderer lookup path. These hook textures are extension resources, not a historical Tideborne presentation namespace. |
| `assets/tide_team_journal/lang/en_us.json` | `CURRENT_MOVE` | Move current journal, command, tooltip, screen, metric, event, toast, scoreboard, chat, and config presentation keys toward Tideborne-owned translation keys in P7.1. | Command behavior, payload IDs, persistence, and saved journal data remain unchanged. |
| `assets/tide_traits/lang/en_us.json` current `message.*` and `tooltip.*` entries | `CURRENT_MOVE` | Move active UI translation ownership to `message.tideborne.*` and `tooltip.tideborne.*` in P7.1 and update direct callers. | Do not rename persisted IDs while changing display keys. |
| `assets/tide_traits/lang/en_us.json` `item.tide_traits.anglers_satchel` | `COMPAT_ALIAS` | Retain the historical item translation alias while canonical presentation resources are consolidated. | The Satchel item registry ID remains historical and must keep a valid default translation path. |
| `assets/tide_traits/lang/en_us.json` `tag.item.tide_traits.*` | `COMPAT_ALIAS` | Retain until the tag compatibility plan is implemented in P7.2. | Existing datapacks can reference historical tag IDs. |
| `assets/tide_traits/models/item/anglers_satchel*.json` | `COMPAT_ALIAS` | Keep minimal historical model aliases. Their referenced textures may move to Tideborne ownership in P7.1. | Item model resolution follows the persisted historical item namespace. |
| `assets/tide_traits/textures/entity/traits/**` | `CURRENT_MOVE` | Move to `assets/tideborne/textures/...` and update direct resource lookups in the later focused identifier phase. | Rendering behavior and specimen trait behavior must not change. |
| `assets/tide_traits/textures/gui/**` | `CURRENT_MOVE` | Move current journal and trait presentation textures to Tideborne ownership. | Historical specimen/save identities remain untouched. |
| Other Satchel presentation textures under `assets/tide_traits/textures/**` | `CURRENT_MOVE` | Move physical textures to Tideborne ownership while retaining historical model aliases when required. | Satchel item ID, components, NBT, persistence, and behavior remain unchanged. |
| `assets/tidebound_compatibility/lang/en_us.json` current `tooltip.*`, `message.*`, `toast.*`, `key.*`, `guide.*`, and presentation `command.*` entries | `CURRENT_MOVE` | Move current presentation keys to Tideborne-owned families in P7.1 and update direct callers. | Commands and networking semantics remain unchanged. |
| `assets/tidebound_compatibility/lang/en_us.json` `item.tidebound_compatibility.*` | `COMPAT_ALIAS` | Retain the historical item translations needed by persisted item IDs. | Item registry IDs must not change. |
| `assets/tidebound_compatibility/lang/en_us.json` `tag.item.tidebound_compatibility.*` | `COMPAT_ALIAS` | Retain pending P7.2 tag compatibility work. | Public tag identities can be consumed by datapacks and integrations. |
| `assets/tidebound_compatibility/models/item/*.json` | `COMPAT_ALIAS` | Keep minimal model aliases at historical item paths. Models may point to Tideborne-owned textures after P7.1. | Item model resolution follows historical registry namespaces. |
| `assets/tidebound_compatibility/textures/item/**` | `CURRENT_MOVE` | Move physical textures to `assets/tideborne/textures/item/**` and repoint retained historical models. | Do not change the item IDs themselves. |
| `assets/tideborne/**` | canonical target | Keep and merge current Tideborne-owned presentation resources here. | Avoid duplicate translation keys and preserve all existing canonical paths. |

### P7.1 asset rule

A historical item namespace can remain as a thin compatibility model and translation alias even after the actual texture becomes Tideborne-owned. P7.1 must not delete a historical model merely because its texture is moved.

## Data resources

| Surface | Classification | P7 action | Compatibility constraint |
| --- | --- | --- | --- |
| `data/tide_traits/function/test_tuna_mutations.mcfunction` | `TEST_ONLY` | Treat separately from active runtime ownership in P7.2. Move to an appropriate test fixture or retire only after proving it is not a supported user-facing command surface. | Do not treat this diagnostic function as evidence that all `tide_traits` data should remain production-owned. |
| `data/tide_traits/recipe/anglers_satchel.json` | `COMPAT_ALIAS` | Keep the historical recipe identity unless a validated alias strategy is introduced. | Recipe IDs are externally referenceable and P7 must preserve compatibility. |
| `data/tide_traits/tags/item/*_excluded.json` | `COMPAT_ALIAS` | Preserve historical tag IDs. A canonical Tideborne tag family may be added only with explicit dual-namespace support. | Existing datapacks can populate these extension tags. |
| `data/tidebound_compatibility/bait/leviathan_bait.json` | `CURRENT_MOVE` with compatibility loader alias | Make Tideborne the current ownership path in P7.2, while allowing historical namespace resources to remain loadable where the current reload path is an external datapack extension point. | Leviathan bait behavior, balance, and optional Tide integration must remain unchanged. |
| `data/tidebound_compatibility/rod_accessories/*.json` | `CURRENT_MOVE` with compatibility loader alias | Move current data ownership to Tideborne in P7.2 and support the historical lookup path as a compatibility source if external datapacks can provide it. | Gear modifiers, leaders, hooks, and line behavior must remain unchanged. |
| `data/tidebound_compatibility/recipe/*.json` | `COMPAT_ALIAS` | Preserve historical recipe identities. | Do not silently replace externally persisted or referenced recipe IDs. |
| `data/tidebound_compatibility/tags/item/*.json` | `COMPAT_ALIAS` | Preserve historical tag IDs and their integration semantics. | These tags include fish, predator, food, Leviathan, and optional integration extension points. |
| `data/tide/tags/**` | `UPSTREAM_EXTENSION` | Keep under the Tide namespace. | These resources extend Tide-owned tag surfaces. |
| `data/myths_of_the_sea/tags/**` | `OPTIONAL_INTEGRATION` | Keep under the foreign namespace. | This is deliberate optional Myths integration and must remain safe when Myths is absent. |
| `data/c/**` | common interoperability | Keep. | The `c` namespace is intentionally shared, not a historical Tideborne identity. |
| `data/minecraft/**` | vanilla extension | Keep. | Minecraft-owned extension points require this namespace. |
| `data/tideborne/**` | canonical target | Keep and use for new current Tideborne-owned data where compatibility allows. | Do not create duplicate recipes or tags that change runtime behavior. |

## Mixin, refmap, and Fabric metadata

| Surface | Classification | P7 action | Compatibility constraint |
| --- | --- | --- | --- |
| `tide_traits.mixins.json` | `CURRENT_MOVE` | Consolidate under Tideborne-owned mixin metadata in P7.4 when safe. | Common mixin environment and target behavior must remain identical. |
| `tide_traits.client.mixins.json` | `CURRENT_MOVE` | Consolidate under Tideborne client mixin metadata in P7.4. | Client-only classloading boundary must remain intact. |
| `tide_team_journal.mixins.json` | `CURRENT_MOVE` | Consolidate under Tideborne-owned mixin metadata in P7.4. | Journal behavior and optional classloading safety must not change. |
| `tidebound_compatibility.mixins.json` | `CURRENT_MOVE` | Consolidate current Tideborne mixins in P7.4. | Gameplay re-ownership from P6 must not be reversed. |
| `tidebound_compatibility.apex.mixins.json` | `OPTIONAL_INTEGRATION` | Canonicalize ownership only if the Apex-specific boundary can remain explicit and optional-safe. | Apex classes must never load when Apex is absent. |
| `tide_team_journal.refmap.json`, `tidebound_compatibility.refmap.json` | `CURRENT_MOVE` | Audit Gradle/resource processing and rename only as part of the P7.4 metadata change. | Generated/runtime refmap linkage must remain valid. |
| `fabric.mod.json` historical entries in `provides` | `COMPAT_ALIAS` | Preserve the provided historical mod IDs unless a later compatibility task explicitly replaces them. | Existing dependency checks can rely on `tide_traits`, `tide_team_journal`, or `tidebound_compatibility`. |
| `fabric.mod.json` historical mixin filenames | `CURRENT_MOVE` references | Update only together with the P7.4 mixin metadata consolidation. | No partial filename change that leaves Fabric pointing at a missing config. |

## Non-runtime historical files

Root files such as historical licenses, migration notes, readmes, and implementation notes are `NON_RUNTIME_DOC`. P7 does not delete or rename them merely to reduce historical namespace text counts.

## P7.1 execution contract

P7.1 should only perform the client asset and translation ownership step defined by this classification:

1. Merge Tideborne-owned UI translations into `assets/tideborne/lang` and update their direct translation-key callers.
2. Move current Tideborne presentation textures from historical Tideborne-internal namespaces into `assets/tideborne`.
3. Retain historical item model and item translation aliases wherever registry identity requires them, repointing those models to Tideborne-owned textures when safe.
4. Keep the Tide hook renderer extension textures in `assets/tide` unless the upstream lookup contract is deliberately changed and validated.
5. Do not touch recipe IDs, tags, save IDs, component IDs, payload IDs, network semantics, or optional data-pack ownership in P7.1.
6. Validate that every moved texture/model/lang lookup resolves and that historical item models still render before completing P7.1.

P7.0 makes no runtime resource move. It establishes the compatibility map that later phases must follow.

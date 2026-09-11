# P8 Low-Token Historical-ID Context

Purpose: give P8 workers the finalized P7 compatibility boundary so they do not repeat the resource migration audit.

P8 baseline: `94052f905b7c3bda33e9f1b0b3988ebf93dd327f`

## What P7 already proved

P7 completed resource ownership and validation. Current Tideborne presentation/data ownership was moved where safe, while externally observable historical aliases were retained intentionally.

### `tide_traits`

Known retained resource compatibility surface:
- `assets/tide_traits/lang/en_us.json` for historical item/tag translations.
- `assets/tide_traits/models/item/anglers_satchel*.json` as model aliases for persisted `tide_traits:anglers_satchel`.
- `data/tide_traits/recipe/anglers_satchel.json` as the historical recipe ID.
- `data/tide_traits/tags/item/*_excluded.json` as public datapack extension tags.
- `data/tide_traits/function/test_tuna_mutations.mcfunction` as the published command-function forwarding alias.
- `fabric.mod.json` `provides` alias `tide_traits`.

P7 explicitly deferred Java/runtime compatibility identities in this namespace to P8, including Satchel item/component families, specimen/discovery payload or persistence identifiers, and `tide_traits:mutation_textures` reload-listener identity.

### `tide_team_journal`

P7 left no `assets/tide_team_journal/` or `data/tide_team_journal/` tree.

Known remaining compatibility families:
- Fabric `provides` alias `tide_team_journal`.
- Journal network IDs.
- Journal/team persistence IDs.

These are P8 ownership-centralization targets; their values are not rename targets.

### `tidebound_compatibility`

Known retained resource compatibility surface:
- `assets/tidebound_compatibility/lang/en_us.json`.
- `assets/tidebound_compatibility/models/item/*.json` as aliases for persisted historical item IDs.
- `data/tidebound_compatibility/recipe/*.json` for historical externally addressable recipe IDs.
- `data/tidebound_compatibility/tags/item/*.json` for public/integration tags.
- Fabric `provides` alias `tidebound_compatibility`.

Known non-resource historical families deferred to P8 include item/entity IDs, payload IDs, config compatibility names, and other persisted/runtime identifiers.

## Do not touch as historical Tideborne debt

These namespaces can be legitimate foreign/upstream extension points:
- `assets/tide/**`
- `data/tide/**`
- `data/myths_of_the_sea/**`
- `data/c/**`
- `data/minecraft/**`

Do not fold them into Tideborne merely for namespace uniformity.

## Efficient P8 search strategy

Start with the compact report. Then use narrow local searches rather than opening broad directories:

```bash
git grep -n -E 'tide_traits|tide_team_journal|tidebound_compatibility' -- src/main/java src/test/java src/main/resources/fabric.mod.json scripts .github
```

For exact historical identifier construction, prioritize Java lines containing:
- `Identifier.of`
- `new Identifier`
- `RegistryKey`
- `PayloadTypeRegistry`
- `CustomPayload.Id`
- `ComponentType`
- persistent-state keys
- NBT string constants
- config filenames
- reload-listener IDs

Do not scan every Java file. Read only files surfaced by the literal inventory or by references from a known ID owner.

## Classification vocabulary

Use these labels in the ledger:
- `CANONICAL_CURRENT` — current Tideborne-owned ID; not a P8 legacy target.
- `PERSISTED_COMPAT` — exact historical ID must remain for saved/serialized data.
- `WIRE_COMPAT` — exact historical network/payload ID must remain.
- `PUBLIC_EXTENSION` — externally addressable recipe/tag/function or integration surface.
- `DEPENDENCY_ALIAS` — Fabric/mod dependency compatibility identity.
- `MIGRATION_LOOKUP` — read/repair/migration-only legacy identity.
- `UPSTREAM_FOREIGN` — owned by Tide/Minecraft/Myths/common interoperability.
- `NON_RUNTIME_DOC` — documentation/history only.
- `UNCLASSIFIED` — temporary; must be zero at P8 completion.

## Phase-local read budget

P8.0: state + this context + ledger + exact literal hits only.
P8.1: registry/component owner classes + direct tests only.
P8.2: networking/persistence owner classes + direct tests only.
P8.3: Fabric/config/reload/public compatibility owner classes + direct tests only.
P8.4: architecture tests + remaining literal hits.
P8.5: final ledger + P7 compatibility alias document + exact remaining hits.
P8.6: validation scripts/workflows only as needed.

Avoid rereading all docs, git history, P7 implementation commits, textures/models, or unrelated gameplay code.

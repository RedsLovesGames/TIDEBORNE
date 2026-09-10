# P7 Low-Token Resource Context

Purpose: seed P7 workers with the live high-value inventory so they do not spend tool calls rediscovering obvious resource roots. This is a snapshot, not a replacement for `scripts/p7_agent.py report` after edits.

Snapshot branch: `agent/p7-resources`

P7 dependency baseline: `b2ea30d927c4bae15f2e9ccbdc48b61331ead9f4`

Optimization seed: `053f14f17404d9ae41aeefa095116a6690d19bc2`

## Live top-level asset namespaces at the seed

`src/main/resources/assets/` contains exactly these five top-level namespaces:

- `tide/`
- `tide_team_journal/`
- `tide_traits/`
- `tideborne/`
- `tidebound_compatibility/`

P7's normal current ownership target is `assets/tideborne/`. Do not assume the other four can simply be deleted: classify each file/reference first.

## Live top-level data namespaces at the seed

`src/main/resources/data/` contains exactly these seven top-level namespaces:

- `c/` — common/tag interoperability namespace; not a Tideborne historical namespace.
- `minecraft/` — vanilla extension namespace; keep when Minecraft requires it.
- `myths_of_the_sea/` — optional integration/foreign namespace; audit, do not blindly move.
- `tide/` — upstream Tide/extension namespace; audit ownership carefully.
- `tide_traits/` — historical Tideborne namespace candidate.
- `tideborne/` — canonical current Tideborne namespace.
- `tidebound_compatibility/` — historical/compatibility namespace candidate.

There is no top-level `data/tide_team_journal/` at this snapshot. Do not waste a discovery pass assuming it exists unless later commits add it.

## Live root mixin/refmap hotspots at the seed

Mixin configs:

- `src/main/resources/tide_team_journal.mixins.json`
- `src/main/resources/tide_traits.client.mixins.json`
- `src/main/resources/tide_traits.mixins.json`
- `src/main/resources/tidebound_compatibility.apex.mixins.json`
- `src/main/resources/tidebound_compatibility.mixins.json`

Historical refmaps visible at resource root:

- `src/main/resources/tide_team_journal.refmap.json`
- `src/main/resources/tidebound_compatibility.refmap.json`

Also audit `src/main/resources/fabric.mod.json`, Gradle resource processing, and artifact validation whenever a mixin config/refmap name changes.

## Root resource files that look historical but are not automatically P7 move/delete targets

Examples currently present include:

- `LICENSE_tide-traits`
- `TIDEBORNE_1.2_MIGRATION.txt`
- `TIDEBORNE_README.txt`
- `TIDE_TRAITS_JOURNAL_V4_NOTES.txt`
- `TIDE_TRAITS_LITERAL_ITEM_OVERLAY_FIX.txt`
- `TIDE_TRAITS_LITERAL_ITEM_OVERLAY_FIX_V2.txt`
- `TIDE_TRAITS_LITERAL_OVERLAY_V3.txt`

Treat packaging notes/licenses/migration documentation separately from runtime resource ownership. Do not spend P7 scope deleting historical documentation unless it directly breaks or misrepresents the production artifact.

## Fast path by phase

### P7.0 inventory/classification

Start with `python3 scripts/p7_agent.py report`. Then inspect only historical namespace subtrees and their direct Java/Fabric references. Create a classification of each family: CURRENT_MOVE, COMPAT_ALIAS, UPSTREAM_EXTENSION, OPTIONAL_INTEGRATION, or NON_RUNTIME_DOC.

### P7.1 client assets/translations

Prioritize `assets/tide_traits`, `assets/tide_team_journal`, `assets/tidebound_compatibility`, and existing `assets/tideborne`. Search Java for the exact moved texture/model/lang identifiers only after the path plan is known.

Translation targets are the Tideborne-owned families:

- `config.tideborne.*`
- `tooltip.tideborne.*`
- `message.tideborne.*`
- `screen.tideborne.*`
- `toast.tideborne.*`
- `key.tideborne.*`

Do not change persisted registry/network/save IDs merely because a display translation key changes.

### P7.2 data resources

Prioritize historical `data/tide_traits` and `data/tidebound_compatibility`, then audit whether anything under `data/tide` is actually a Tide extension point and whether `data/myths_of_the_sea` is intentionally foreign-owned. Preserve `data/c` and `data/minecraft` semantics when those namespaces are required by loaders/tags.

### P7.3 Java identifiers

Search narrowly for identifiers discovered in P7.1/P7.2. High-value classes are those constructing `Identifier` values for textures, reload listeners, generated resources, recipe/tag paths, or translation keys. Do not globally replace `tide_traits`, `tide_team_journal`, or `tidebound_compatibility`: P8 owns historical persisted-ID centralization.

### P7.4 mixin/refmap/metadata

Read the five mixin configs listed above plus `fabric.mod.json`, the relevant Gradle resource/refmap configuration, and validation scripts. Preserve client/common and optional Apex/Myths classloading boundaries.

### P7.5 compatibility audit

Use the report's remaining historical literal counts as a search seed, not as a deletion target. For every retained historical runtime namespace, record the exact compatibility reason in the P7 completion report/state notes.

### P7.6 validation

Run the task's current gate: repository validator, Java 21 clean build/unit/architecture tests, resource/mixin/refmap checks, release artifact validation. GameTests and Minecraft boot tests are not current blockers.

## Avoid expensive rereads

Do not reread all of `docs/`, all Java files, or the entire git history. Use exact identifier/path searches based on this map and the current phase. Read the coordination doc from `main` once per new worker, then rely on the branch task/state/context files for P7 execution.

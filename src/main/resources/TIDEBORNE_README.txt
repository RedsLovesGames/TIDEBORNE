TIDEBORNE 1.3.0 — NATIVE INTEGRATION

Public surface
- Mod ID: tideborne
- Config: config/tideborne.json
- Command root: /tideborne
- Backend: com.redslovesgames.tideborne.backend.TideborneBackend
- Rendering facade: com.redslovesgames.tideborne.client.render.TideborneRenderService
- Migration marker: config/tideborne-migration.json

Commands
- /tideborne traits ...
- /tideborne team ...
- /tideborne journal ...
- /tideborne fishing ...
- /tideborne status
- /tideborne reload
- /tideborne migrate status
- /tideborne debug backend

Command UX
- `/tideborne` and `/tideborne help` show the grouped command map.
- Use `/tideborne journal` for player-facing journal actions; `/tideborne team`
  remains the equivalent administrator-friendly name.
- Every public and legacy command root redirects directly to its concrete command
  tree, so Tab completion works consistently for subcommands and arguments.
- Legacy roots (`/tidetraits`, `/tideteamjournal`, `/ttj`, and
  `/tideboundcompat`) remain available for existing scripts.

Satchel layout
- Footer controls use a protected bottom margin so page content, controls, and
  the status strip do not overlap.
- Specimen action buttons are kept inside the scanner panel and their click
  targets match their rendered position.
- The Trait Scanner prefixes the selected fish name with one to five rarity
  stars (Common through Legendary) and trims long names within the panel.

Legacy command implementations are moved under internal roots and the old public roots are compatibility aliases.

Backend
The canonical Tideborne backend now fronts personal/shared discoveries, personal/team Tide journal access, record status and claims, Shared Ledger data, personal-to-team merge, and sync.

Migration
Version 3 creates config/tideborne-migration.json. Legacy serialized namespaces remain preserved: tide_traits:, tide_team_journal:, tidebound_compatibility:. This protects existing items, components, registry IDs, saved journals, packets and worlds.

Rendering
TideborneRenderService is the canonical facade over the proven mutation renderer. Stable mixins stay in place to avoid regressing working entity, item and Fish Display rendering.

Install
Use only this JAR. Remove older Tideborne builds and the three former standalone JARs.

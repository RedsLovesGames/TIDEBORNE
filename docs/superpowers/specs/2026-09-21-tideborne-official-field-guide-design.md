# Tideborne Official Field Guide Design

Date: 2026-09-21
Status: Design specification
Target branch: `wiki/official-field-guide`, based on `dev`

## 1. Product intent

Tideborne needs one official public wiki and companion site that is easy for a new player to understand, fast for an experienced player to search, and maintainable from the Tideborne repository without duplicating gameplay truth across several hand-maintained websites.

The site should feel like an oceanographic field guide, angler's trophy journal, and Minecraft utility interface. It must not feel like a generic documentation template or a generic AI-generated marketing site.

Primary user tasks:

1. Find a fish and understand everything Tideborne knows about it.
2. Understand how specimens, traits, FishScore, gear, Satchel, Journal, records, and compatibility work.
3. Calculate or inspect FishScore behavior without guessing at the mechanics.
4. Build and compare a fishing setup from real Tideborne gear.
5. Install, update, configure, and troubleshoot Tideborne using current versioned documentation.

Success means a player can move from an unknown question to the relevant fish, mechanic, gear item, recipe, command, or wiki page within a few interactions, while every factual mechanic is traceable to a versioned source.

## 2. Source-of-truth boundaries

The site must preserve the existing ownership model rather than create another competing authority.

### Authoritative prose

`docs/wiki/` remains the player-facing prose source. It currently documents Tideborne 2.1.0, Minecraft 1.21.1, Fabric, and Tide 2.1.1 and is verified against the shipped Tideborne 2.1.0 JAR.

The website build consumes this content. It does not create a second separately edited copy of the same articles.

### Authoritative gameplay mechanics

Tideborne Java/source ownership remains authoritative for canonical behavior. The website may render generated reference data and calculators, but it must not invent or silently diverge from canonical rules.

Important examples include:

- specimen identity and trait enums
- FishScore V2 constants and mappings
- gear identity and effects
- Satchel upgrade defaults
- command/config identifiers
- compatibility IDs
- recipes and registered resource IDs

### Authoritative release/resource data

The release JAR and repository resources are the source for:

- mod version and dependency requirements
- recipes
- item/resource identifiers
- icons/textures that are safe to publish
- integration metadata

### Fish catalog data and renders

The existing Tideborne/fish data and render assets in `RedsLovesGames/random-info-pages` are migration inputs. They should be imported into the official Tideborne repository once, normalized, and then maintained here. The final official site should not depend at runtime on a second repository being online or unchanged.

## 3. Existing repository integration

The repository already has:

- `docs/wiki/` for current player documentation
- `site/` for the current static official wiki shell
- `.github/workflows/pages.yml` for GitHub Pages deployment

Therefore the new site will migrate the existing `site/` surface instead of creating a parallel `wiki/` product directory.

### Resulting high-level structure

```text
TIDEBORNE/
├── docs/
│   └── wiki/                         # authoritative prose
├── site/                             # Astro/Starlight web project
│   ├── astro.config.mjs
│   ├── package.json
│   ├── tsconfig.json
│   ├── public/
│   │   ├── tideborne-icon.png
│   │   ├── fish/
│   │   ├── items/
│   │   └── screenshots/
│   ├── scripts/
│   │   ├── sync-docs.mjs
│   │   ├── build-fish-data.mjs
│   │   ├── build-reference-data.mjs
│   │   └── validate-site-data.mjs
│   └── src/
│       ├── assets/
│       ├── components/
│       │   ├── ui/
│       │   ├── fish/
│       │   ├── score/
│       │   ├── gear/
│       │   └── navigation/
│       ├── content/docs/            # generated from docs/wiki at prebuild
│       ├── data/
│       │   ├── editorial/
│       │   └── generated/
│       ├── pages/
│       │   ├── index.astro
│       │   ├── fish/
│       │   └── tools/
│       └── styles/
│           ├── tokens.css
│           ├── base.css
│           └── tideborne.css
└── .github/workflows/pages.yml
```

Generated content under `site/src/content/docs` is build output from `docs/wiki` and should not become a separately maintained documentation source.

## 4. Technical architecture

### Framework

Use Astro with Starlight as documentation infrastructure and React islands for interactive features.

Why:

- static output is ideal for GitHub Pages
- Markdown/MDX remains first-class
- Starlight supplies a proven documentation foundation, search integration, navigation semantics, SEO, and accessibility behavior
- React islands allow use of the bundled shadcn-compatible component sources without turning the entire site into a client-rendered application
- ordinary documentation pages can remain nearly JavaScript-free

Starlight is infrastructure, not the visible brand. Its shell should be customized enough that the site looks Tideborne-specific.

### Styling

Use Tailwind for imported/adapted React components plus a small semantic CSS token layer for Tideborne's visual identity.

Do not copy upstream library themes wholesale. Every reused component must be normalized to Tideborne tokens.

### Search

Use Starlight/Pagefind for global static search across documentation and statically generated fish pages.

Add a custom `Ctrl/Cmd + K` presentation layer using shadcn Command/Dialog so global search can group results by type:

- Fish
- Wiki page
- Gear
- Recipe
- Command/config reference

The Fish Explorer uses its own local search/filter state because it is a browsing/database interaction rather than global site navigation.

## 5. UI component bundle strategy

The uploaded local bundle contains 33,000+ files and source snapshots for shadcn/ui, Kibo UI, ReUI, Magic UI, Motion Primitives, Animate UI, Origin UI, and other libraries.

The bundle's documented `catalog/components.json` file is absent, so component selection must use the bundled source tree directly or a generated local inventory.

### Foundation

Use shadcn/ui New York style primitives as the default interactive foundation.

Verified useful source locations in the bundle include:

- `sources/react/shadcn-ui/apps/v4/registry/new-york-v4/ui/command.tsx`
- `sources/react/shadcn-ui/apps/v4/registry/new-york-v4/ui/sheet.tsx`
- the adjacent New York v4 primitives for dialog, popover, button, input, tabs, slider, tooltip, skeleton, checkbox, badge, separator, and related controls

These provide the accessibility/interaction base for search, mobile filters, dialogs, tabs, sliders, and loading states.

### Rich selection controls

Use Kibo UI selectively where its richer composition materially improves the product.

Verified candidates:

- `sources/react/kibo-ui/packages/combobox/index.tsx`
- `sources/react/kibo-ui/packages/choicebox/index.tsx`

The Kibo combobox is a good fit for searchable gear selection and some filter controls because it is built on shadcn Command/Popover and only adds a small amount of behavior.

The Choicebox is a good fit for visually selecting specimen trait states or gear options when a plain radio group would be too weak.

### Motion

Use Motion Primitives as the default motion family and keep motion limited to feedback, state transitions, and a few high-value moments.

Verified candidate:

- `sources/react/motion-primitives/components/core/animated-number.tsx`

Use this for the live FishScore result rather than adding a heavier animation system.

Other Motion Primitives may be used for variant/render transitions only if they preserve reduced-motion behavior and do not materially increase bundle cost.

### Libraries intentionally not used by default

- ReUI's advanced filter system is not needed for the initial Fish Explorer because its filter package brings TanStack Table, virtualization, and date dependencies that the product does not need for simple fish filtering.
- Aceternity UI is not a default dependency because the bundle records a non-MIT Aceternity license. It may only be considered after a specific component's public-use license is confirmed.
- RewampUI is excluded by default because the bundle records its license as unknown/inspect source.
- React Bits is excluded by default because the bundle records MIT plus Commons Clause licensing.
- WebGL/canvas effects are not part of the baseline site.

## 6. Tideborne design system

### Personality

Precise, exploratory, aquatic, collectible, technical, restrained.

The site should feel authored for Tideborne rather than decorated after the fact.

### Core colors

Initial palette:

```text
Deep ocean      #07141D
Navy            #10263A
Tide blue       #28698C
Sea teal        #2C7C78
Light water     #86C6C5
Parchment       #D7B68F
Warm paper      #E6CBA9
Coral           #D76358
FishScore gold  #E4B85C
Primary text    #EDF5F4
Muted text      #9CB3B3
```

These become semantic tokens rather than direct component-specific colors.

### Spacing

Use the constrained scale:

`2, 4, 8, 12, 16, 24, 32, 48, 64, 96, 128`

### Radius

Use a small consistent family:

- small: 6px
- medium: 10px
- large: 16px
- full: only for semantically pill/circular controls

### Elevation

Use four practical levels:

- flat
- raised control
- card/popover
- modal/overlay

Avoid borders around every surface. Group primarily with spacing, background changes, hierarchy, and controlled elevation.

### Typography

Body/UI typography prioritizes legibility. A more distinctive display face may be used for hero/section identity only if it does not impair scanning or loading performance.

## 7. Information architecture

Top-level navigation should remain short:

- Start
- Fishing
- Fish
- Gear
- Satchel
- Journal
- Reference
- Search

Documentation navigation expands beneath those categories rather than putting every wiki article in the top bar.

### Start

- Overview
- Getting Started
- Installation
- Dependencies
- Your First Catch
- Tide 2 Basics
- Updating Tideborne
- FAQ

### Fishing

- Catch pipeline
- Species selection
- Fishing Luck
- Fight Strength
- Fight Tempo
- Perfect Catch

### Specimens

- What is a specimen?
- Size and percentiles
- Body Type
- Condition
- Pigmentation
- Quality / Perfect Specimen
- Trait Luck
- Trait Momentum
- Persistent specimen identity

### FishScore

- Overview
- Exact formula
- Rarity points
- Trait points
- Percentile contribution
- FishScore Calculator

### Gear

- Gear overview
- Rods
- Lines
- Hooks
- Leaders
- Bait
- Recipes
- Integrations
- Gear Builder

### Collection and records

- Angler's Satchel
- Satchel upgrades
- Journal
- Teams
- Records
- Hall Record Display

### Reference

- Configuration
- Commands
- Recipes
- Compatibility
- Updating/migration
- Exact mechanics
- Version information

## 8. Primary feature: Fish Explorer

The Fish Explorer is the first major interactive feature and defines the site's application design language.

### Desktop layout

Use a two-region layout:

- left filter column
- main fish result region

The filter column may become sticky on wide screens but must not trap keyboard focus or exceed viewport height without its own sensible scroll behavior.

### Mobile layout

Do not shrink the desktop sidebar.

Mobile should show:

- page heading
- search input
- compact filter summary button
- result count
- fish cards

Filters open in a shadcn Sheet/Drawer with an explicit Apply/Clear experience where needed.

### Filter dimensions

At minimum:

- text search
- source mod
- rarity
- habitat
- journal category when data exists

Only add additional filters when the underlying data is reliable.

### Result cards

Each result card should prioritize:

1. render
2. fish name
3. rarity
4. habitat/source metadata

Cards must not become miniature specification tables.

Support grid/list views only if the list view materially improves dense scanning after the grid is complete.

### States

Implement:

- loading skeleton
- no results
- missing render fallback
- partial metadata
- selected/hover/focus
- mobile
- reduced motion

## 9. Fish detail pages

Generate one static route per fish, for example:

`/fish/tide/dragon-fish/`

Each page contains:

- large source-backed render
- canonical display name
- namespace/source
- rarity
- habitat
- size/range information when verified
- specimen variant controls when verified render variants exist
- FishScore potential/reference
- catch guidance based on verified data
- relevant gear/bait when the relationship is actually supported
- Journal/record information
- technical data/provenance
- version badge

### Variant controls

Possible states include:

- Normal
- Giant
- Dwarf
- Albino
- Iridescent
- Scarred
- Parasite-Ridden

Do not show a selectable variant when no verified representation/data exists.

Variant transitions should be short and functional. Reduced-motion users receive immediate swaps.

## 10. FishScore Calculator

The calculator should be a real Tideborne tool, not a decorative mockup.

Inputs:

- species/rarity context
- final percentile
- Body Type
- Condition
- Pigmentation
- Specimen Quality

Output:

- raw contribution breakdown
- normalized FishScore
- visible 1-3000 scale
- exact selected trait contribution values

The score value may use Motion Primitives' animated number, but the animation must be disabled or simplified for reduced-motion users.

### Canonical-data rule

The calculator must consume generated/reference data rather than maintaining an unrelated set of hard-coded point tables inside the React component.

The data-generation mechanism must be testable against canonical Tideborne Java behavior before the calculator is considered complete.

## 11. Gear database and Gear Builder

Gear should be represented as structured item data, not one giant article.

Each item may expose:

- icon
- item name
- slot/category
- source/integration
- relevant modifiers/effects
- recipe
- compatibility conditions
- version availability

### Builder interaction

The Gear Builder provides slots such as:

- rod
- line
- hook
- leader
- bait

Use Kibo Combobox for searchable slot selection if it remains the smallest useful control after adaptation.

The summary panel displays only effects that can be derived from canonical data. It must not imply simulated catch odds or outcomes that Tideborne cannot accurately calculate from the available inputs.

## 12. Homepage

The homepage is built after the primary tools so it can showcase real functionality rather than generic marketing blocks.

### Hero

Primary content:

- Tideborne
- Every Catch Has a Story.
- short value statement
- primary CTA: Explore Fish
- secondary CTA: Get Started
- current version badges

Use one strong specimen/render presentation and a restrained oceanic background treatment. No particle field, WebGL scene, or stack of unrelated hero effects.

### Immediate utility

The next section should expose real fish search rather than a generic three-card feature grid.

Then explain the core loop visually:

Catch -> Generate Specimen -> Fight -> Score -> Keep

Later homepage sections may feature:

- specimen traits
- FishScore calculator preview
- gear builder preview
- Satchel/records
- latest release/reference links

## 13. Documentation rendering

A prebuild script synchronizes `docs/wiki/*.md` into Starlight's content collection.

The synchronizer should:

1. preserve Markdown body content
2. derive title from the first H1 when frontmatter is absent
3. add generated frontmatter needed by Starlight
4. preserve source links back to the authoritative `docs/wiki` file
5. fail clearly if a document cannot be parsed

Do not manually copy article text into separate MDX files.

## 14. Generated site data

Use a clear separation:

```text
site/src/data/editorial/
site/src/data/generated/
```

Editorial data is site-specific presentation metadata.

Generated data should eventually include:

```text
release.json
dependencies.json
recipes.json
items.json
traits.json
fishscore.json
gear.json
integrations.json
fish/*.json
```

### Generation principle

Do not make the website decompile Java bytecode on every page build.

Prefer a repository build/export step that turns canonical Tideborne data into a stable JSON reference artifact, then let Astro consume that artifact.

The export implementation must avoid shipping a second gameplay authority inside the mod runtime. A dedicated build-time/export source set or similarly isolated mechanism is preferred if direct canonical APIs can safely be called.

Where canonical runtime classes cannot be loaded safely outside Minecraft, use explicit generated snapshots with automated consistency tests against canonical unit-level behavior rather than fragile source-regex parsing.

## 15. Accessibility

Required baseline:

- semantic HTML
- keyboard-operable search and filters
- visible focus state
- accessible names for icon-only controls
- proper labels for form controls
- sufficient contrast
- status never communicated by color alone
- sensible heading hierarchy
- meaningful image alt text where the fish identity is not already adjacent text
- reduced-motion support
- useful touch target sizes

Rarity stars need accessible text such as `5-star rarity` rather than relying on symbols alone.

## 16. Performance

Target a fast static site.

Rules:

- documentation pages should hydrate little or no React
- Fish Explorer is one bounded client island
- FishScore Calculator is one bounded client island
- Gear Builder is one bounded client island
- lazy-load off-screen fish renders
- use responsive images where source assets support it
- statically generate fish detail routes
- prefer CSS for simple hover/visual treatment
- animate transform/opacity when possible
- no baseline canvas/WebGL dependency
- dynamically import optional heavy interactions

## 17. Deployment

Update `.github/workflows/pages.yml` to build the Astro project rather than copying the current static `site/` files.

Expected flow:

1. checkout branch/release source
2. set up Node
3. install locked site dependencies
4. run documentation sync
5. run site data validation/generation
6. run `astro check`
7. run tests
8. run production Astro build
9. upload `site/dist`
10. deploy with GitHub Pages actions

The Astro config must set the correct GitHub Pages base path for the `TIDEBORNE` project site.

The current static site's icon and useful content are migrated, not silently discarded.

## 18. Versioning

Every page should make the documentation target discoverable without repeating a giant banner.

Initial target:

- Tideborne 2.1.0
- Minecraft 1.21.1
- Fabric
- Tide 2.1.1

Design data structures so later archives can support routes such as:

- `/v/2.1.0/`
- `/v/2.0.1/`
- `/v/1.3.57/`

Do not implement the full historical version selector until the current 2.1.0 site is complete.

## 19. Migration of existing public surfaces

After the new official site is verified:

- Tideborne GitHub Pages becomes the canonical current wiki.
- CurseForge Wiki links should point to the official site.
- `random-info-pages/tideborne` becomes a legacy/prototype source or redirects to the official site.
- the old Tide-2-Addons pages remain clearly labeled legacy 1.3.57 material or redirect current users.
- useful fish data/renders from older surfaces are imported into the official repository.
- useful Tide 2 mechanics visualizations may be migrated into `/reference/` or `/tools/` only when still accurate.

No redirect changes should occur before the replacement passes production QA.

## 20. Testing and quality gate

### Unit/data tests

Cover:

- fish search normalization
- filter combinations
- fish route generation
- generated data schema validation
- FishScore data/calculation behavior
- gear selection aggregation where applicable

### Component/interaction tests

Cover:

- global search keyboard flow
- mobile filter Sheet
- Fish Explorer no-results state
- variant selector
- FishScore input/output updates
- Gear Builder slot selection

### End-to-end checks

At minimum verify:

- desktop Fish Explorer
- narrow mobile Fish Explorer
- keyboard-only global search
- one fish detail route
- FishScore calculator
- one documentation page
- no broken internal links in production output

### Required commands before completion

The exact package scripts may differ, but the final project must provide equivalents of:

```text
npm run check
npm run lint
npm run test
npm run build
```

Do not declare the site complete while a verified responsive, accessibility, build, or data-consistency issue remains.

## 21. Implementation order

1. Convert `site/` into the Astro/Starlight project without changing `docs/wiki` authority.
2. Establish Tideborne design tokens and the customized Starlight shell.
3. Implement documentation sync and render existing 2.1.0 articles.
4. Import and normalize the existing fish catalog data/renders.
5. Build Fish Explorer.
6. Build fish detail static routes and variant handling.
7. Add global grouped search presentation.
8. Build canonical-reference generation/validation needed for FishScore.
9. Build FishScore Calculator.
10. Build gear database and recipes.
11. Build Gear Builder.
12. Build homepage around the completed features.
13. Add restrained motion/polish and reduced-motion behavior.
14. Replace the current GitHub Pages assembly workflow with the Astro build.
15. Run full responsive/accessibility/data/build QA.
16. Only after successful production verification, update/redirect older public surfaces.

## 22. Non-goals for the first production milestone

Do not block the initial official wiki on:

- user accounts
- comments
- server-side database
- live multiplayer data
- a CMS
- WebGL water simulation
- every historical Tideborne version
- every possible comparison visualization
- a complete redesign of Tideborne's in-game UI

The first milestone is a fast, authoritative, highly searchable 2.1.0 wiki with an excellent Fish Explorer, static fish pages, reliable documentation, and the foundation for FishScore and gear tooling.

## 23. Acceptance summary

The design is successful when:

- `docs/wiki` remains authoritative prose
- the official Pages site is built from `site/` with Astro/Starlight
- Fish Explorer is the primary application feature
- fish pages are static and source-backed
- reused bundle components are minimal, accessible, and normalized to Tideborne tokens
- public-use licensing is clear for copied component code
- global docs/search and fish-specific filtering are separate, appropriate tools
- interactive tools consume generated/canonical reference data rather than silently diverging from the mod
- mobile is intentionally reflowed rather than shrunken desktop
- ordinary docs remain lightweight
- GitHub Pages deployment is reproducible and validated
- no changes are made to `main`

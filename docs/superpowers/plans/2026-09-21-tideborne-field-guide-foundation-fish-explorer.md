# Tideborne Field Guide Foundation + Fish Explorer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Tideborne's current hand-built static wiki shell with a production Astro/Starlight site that consumes `docs/wiki/`, preserves the existing GitHub Pages surface, and ships a working data-backed Fish Explorer plus generated fish detail pages.

**Architecture:** `docs/wiki/` remains the single human-edited prose source. `site/` becomes an Astro/Starlight project with React islands only where interaction is required. Fish metadata and renders are migrated from the existing public Tideborne fish data into a typed local dataset and then statically rendered. This first milestone deliberately stops before FishScore calculation, Gear Builder, command-palette search, and final homepage polish so it can ship and be reviewed independently.

**Tech Stack:** Astro 7.3.x, Starlight 0.42.x, `@astrojs/react` 6.0.x, React 19.2.x, Tailwind CSS 4.3.x, TypeScript, shadcn-derived primitives from the uploaded local UI bundle, Vitest 5.x, Testing Library 16.x, GitHub Pages using `withastro/action@v6`.

**Spec:** `docs/superpowers/specs/2026-09-21-tideborne-official-field-guide-design.md`

## Global Constraints

- Work only on `wiki/official-field-guide`, based on `dev`. Never modify, merge, rebase, or retarget `main`.
- `docs/wiki/` remains authoritative prose. Generated copies under `site/src/content/docs/` are disposable build inputs, not a second editing surface.
- Documentation target remains Tideborne 2.1.0, Minecraft 1.21.1, Fabric, Tide 2.1.1 unless the authoritative docs are deliberately updated.
- Website code may display generated/versioned mechanics but must not become a second gameplay authority.
- Copy only the smallest UI component source needed from `UI_LIBRARY_COMPLETE.zip` and normalize it to Tideborne tokens. This follows the bundle rule to use source components selectively rather than importing upstream projects wholesale.
- Foundation component family is shadcn/ui. Kibo UI and Motion Primitives are reserved for later milestones unless a tested need appears.
- No Aceternity, RewampUI, React Bits, WebGL, canvas, or particle dependency in this milestone.
- Mobile layouts reflow intentionally. Do not shrink the desktop filter sidebar into a narrow column.
- Semantic controls, keyboard operation, visible focus, sufficient contrast, non-color status cues, and reduced-motion behavior are required.
- Ordinary documentation pages should remain nearly JavaScript-free.
- Production output must work when hosted under the GitHub Pages base path `/TIDEBORNE`.

## Review Focus

- **Repository base path:** every internal asset and custom route must work from `/TIDEBORNE/`, not just from a local root server. Task 1 and Task 7 own this.
- **Partial fish records:** missing render, habitat, rarity, or optional metadata must not crash cards or detail pages. Tasks 4 and 6 own this.
- **Zero-result filter combinations:** the Explorer must show an explicit empty state and reset action. Task 5 owns this.
- **Keyboard/mobile filters:** search, checkboxes, filter-sheet open/close, and reset must work without a pointer. Task 5 owns this.
- **Generated-doc drift:** deleted or renamed source Markdown must not leave stale generated pages. Task 2 owns this.

---

## Locked File Structure

```text
site/
├── astro.config.mjs
├── package.json
├── package-lock.json
├── tsconfig.json
├── public/
│   ├── tideborne-icon.png
│   └── fish/renders/
├── scripts/
│   ├── sync-docs.mjs
│   ├── sync-docs.test.mjs
│   ├── normalize-fish-data.mjs
│   └── normalize-fish-data.test.mjs
└── src/
    ├── content.config.ts
    ├── content/docs/                  # generated from docs/wiki
    ├── styles/
    │   ├── tokens.css
    │   └── global.css
    ├── lib/
    │   ├── cn.ts
    │   └── urls.ts
    ├── components/
    │   ├── ui/
    │   │   ├── button.tsx
    │   │   ├── checkbox.tsx
    │   │   ├── input.tsx
    │   │   ├── sheet.tsx
    │   │   ├── skeleton.tsx
    │   │   └── badge.tsx
    │   └── fish/
    │       ├── FishExplorer.tsx
    │       ├── FishFilters.tsx
    │       ├── FishCard.tsx
    │       ├── FishEmptyState.tsx
    │       ├── FishExplorer.test.tsx
    │       ├── FishRender.astro
    │       ├── FishIdentity.astro
    │       ├── FishMetadata.astro
    │       └── FishVariantStrip.astro
    ├── data/fish/
    │   ├── schema.ts
    │   ├── fish-data.json
    │   ├── filterFish.ts
    │   └── filterFish.test.ts
    └── pages/
        └── fish/
            ├── index.astro
            └── [source]/[slug].astro
```

The synchronized `docs/wiki/README.md` remains Starlight's root documentation page for this milestone, so there is **no competing `site/src/pages/index.astro` yet**. Final custom homepage work is a later milestone.

---

### Task 1: Convert `site/` to Astro/Starlight and preserve the public Pages identity

**Files:**
- Create: `site/package.json`
- Create: `site/astro.config.mjs`
- Create: `site/tsconfig.json`
- Create: `site/src/content.config.ts`
- Create: `site/src/styles/tokens.css`
- Create: `site/src/styles/global.css`
- Create: `site/src/lib/urls.ts`
- Move/copy: `site/assets/tideborne-icon.png` -> `site/public/tideborne-icon.png`
- Delete after replacement works: `site/index.html`, `site/assets/app.js`, `site/assets/styles.css`
- Modify: `.gitignore`

**Interfaces:**
- Produces `npm run dev`, `npm run build`, `npm run check`, and `npm test` from `site/`.
- Produces static output in `site/dist/` for `https://redslovesgames.github.io/TIDEBORNE/`.
- Produces `sitePath(path: string): string` for base-safe custom links.

- [ ] **Step 1: Create the package manifest with only milestone dependencies**

Core versions verified on the planning date:

```text
astro                     ^7.3.3
@astrojs/starlight        ^0.42.2
@astrojs/react            ^6.0.6
@astrojs/check            ^0.9.10
react                     ^19.2.3
react-dom                 ^19.2.3
tailwindcss               ^4.3.3
@tailwindcss/vite         ^4.3.3
typescript                ^7.0.2
vitest                    ^5.0.1
@testing-library/react    ^16.3.3
```

Also install `@testing-library/dom`, `jsdom`, `radix-ui`, `lucide-react`, `class-variance-authority`, `clsx`, and `tailwind-merge`. Do not install Kibo, Motion, ReUI, or visual-effect packages yet.

Use these scripts:

```json
{
  "scripts": {
    "dev": "npm run sync:docs && astro dev",
    "sync:docs": "node scripts/sync-docs.mjs",
    "build": "npm run sync:docs && astro check && astro build",
    "check": "astro check",
    "test": "vitest run"
  }
}
```

- [ ] **Step 2: Configure Astro/Starlight for the repository Pages path**

`site/astro.config.mjs` uses:

```js
import { defineConfig } from 'astro/config';
import react from '@astrojs/react';
import starlight from '@astrojs/starlight';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  site: 'https://redslovesgames.github.io',
  base: '/TIDEBORNE',
  output: 'static',
  integrations: [
    starlight({
      title: 'Tideborne',
      description: 'Official Tideborne field guide and wiki.',
      favicon: '/TIDEBORNE/tideborne-icon.png',
      customCss: ['./src/styles/tokens.css', './src/styles/global.css'],
    }),
    react(),
  ],
  vite: {
    plugins: [tailwindcss()],
  },
});
```

Do not manually configure a sidebar in this milestone. Starlight's default behavior automatically builds one from the synchronized documentation tree.

- [ ] **Step 3: Configure the Starlight content collection**

`site/src/content.config.ts`:

```ts
import { defineCollection } from 'astro:content';
import { docsLoader } from '@astrojs/starlight/loaders';
import { docsSchema } from '@astrojs/starlight/schema';

export const collections = {
  docs: defineCollection({ loader: docsLoader(), schema: docsSchema() }),
};
```

- [ ] **Step 4: Add a base-safe link helper before custom routes are built**

`site/src/lib/urls.ts`:

```ts
export function sitePath(path: string): string {
  const base = import.meta.env.BASE_URL.replace(/\/$/, '');
  const normalized = path.startsWith('/') ? path : `/${path}`;
  return `${base}${normalized}`;
}
```

Use this helper for custom Fish Explorer links instead of hard-coded root-relative URLs.

- [ ] **Step 5: Establish Tideborne tokens**

Create semantic tokens for background, surface, raised surface, primary/secondary/muted text, subtle/strong border, accent, FishScore gold, success, warning, danger, 6/10/16px radii, and four elevation levels. Map the approved deep-ocean/navy/tide-blue/sea-teal/light-water/parchment/coral/gold palette into those semantic roles.

Use the spacing scale `2, 4, 8, 12, 16, 24, 32, 48, 64, 96, 128`.

- [ ] **Step 6: Install and verify the foundation**

From `site/`:

```bash
npm install
npm run check
```

Expected: dependency install succeeds. Configuration/type errors are fixed before Task 2.

- [ ] **Step 7: Commit**

```bash
git add site .gitignore
git commit -m "feat(wiki): migrate site foundation to Astro Starlight"
```

### Task 2: Synchronize `docs/wiki/` deterministically into Starlight

**Files:**
- Create: `site/scripts/sync-docs.mjs`
- Create: `site/scripts/sync-docs.test.mjs`
- Generated: `site/src/content/docs/*.md`

**Interfaces:**
- Consumes repository `docs/wiki/*.md`.
- Produces clean generated Starlight documents.
- Exports `syncDocs({ sourceDir, outputDir }): Promise<void>`.

- [ ] **Step 1: Write the failing clean-sync test**

Create temporary source/output directories with `README.md`, `FISHSCORE.md`, and a stale output file. Assert:

```js
expect(readdirSync(outputDir).sort()).toEqual(['fishscore.md', 'index.md']);
expect(readFileSync(join(outputDir, 'index.md'), 'utf8')).toContain('title: Tideborne Wiki');
expect(existsSync(join(outputDir, 'STALE.md'))).toBe(false);
```

Then delete `FISHSCORE.md`, run sync again, and assert only `index.md` remains.

- [ ] **Step 2: Run the focused test and verify it fails**

```bash
cd site
npm test -- scripts/sync-docs.test.mjs
```

Expected: FAIL because `syncDocs` does not exist.

- [ ] **Step 3: Implement clean synchronization**

Rules:

- remove and recreate the generated docs directory every run;
- map `README.md` -> `index.md`;
- map other filenames to lowercase kebab-case slugs;
- preserve source Markdown body verbatim;
- if source frontmatter is absent, derive `title` from the first H1, falling back to a readable filename title;
- never write into `docs/wiki/`.

- [ ] **Step 4: Run test, check, and build**

```bash
npm test -- scripts/sync-docs.test.mjs
npm run check
npm run build
```

Expected: PASS and the current Tideborne player docs emit as Starlight pages.

- [ ] **Step 5: Commit**

```bash
git add site/scripts site/src/content
 git commit -m "feat(wiki): sync authoritative player docs into Starlight"
```

### Task 3: Adapt the minimum shadcn primitives from the uploaded UI bundle

**Files:**
- Create: `site/src/lib/cn.ts`
- Create: `site/src/components/ui/button.tsx`
- Create: `site/src/components/ui/input.tsx`
- Create: `site/src/components/ui/checkbox.tsx`
- Create: `site/src/components/ui/sheet.tsx`
- Create: `site/src/components/ui/skeleton.tsx`
- Create: `site/src/components/ui/badge.tsx`
- Modify: `site/src/styles/global.css`

**Interfaces:**
- Produces Tideborne-normalized interactive primitives used by the Fish Explorer.

- [ ] **Step 1: Copy only required New York v4 primitives**

Use source files from:

```text
sources/react/shadcn-ui/apps/v4/registry/new-york-v4/ui/
```

Do not import the upstream site, registry demos, or unrelated primitives.

- [ ] **Step 2: Normalize `cn` and aliases**

`site/src/lib/cn.ts`:

```ts
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
```

Replace upstream registry paths and the bundle's `cn` import with local `@/` imports.

- [ ] **Step 3: Normalize visuals to Tideborne tokens**

Preserve Radix accessibility and state behavior. Replace upstream colors, radii, and elevation with the Tideborne token system. Do not add gradients, glass, particles, or unrelated animation.

- [ ] **Step 4: Typecheck**

```bash
npm run check
```

Expected: PASS with no unresolved aliases or missing component dependencies.

- [ ] **Step 5: Commit**

```bash
git add site/src/components/ui site/src/lib/cn.ts site/src/styles
git commit -m "feat(wiki): add Tideborne UI primitives"
```

### Task 4: Normalize the existing fish catalog into a typed local dataset

**Files:**
- Create: `site/src/data/fish/schema.ts`
- Create: `site/scripts/normalize-fish-data.mjs`
- Create: `site/scripts/normalize-fish-data.test.mjs`
- Create: `site/src/data/fish/fish-data.json`
- Create: `site/public/fish/renders/*`

**Interfaces:**
- Produces this display-only type:

```ts
export type FishEntry = {
  id: string;
  source: string;
  slug: string;
  name: string;
  rarity: 1 | 2 | 3 | 4 | 5 | null;
  habitats: string[];
  journalCategory: string | null;
  render: string | null;
  variants: Partial<Record<
    'normal' | 'giant' | 'dwarf' | 'albino' | 'iridescent' | 'scarred' | 'parasite_ridden',
    string
  >>;
  metadata: Record<string, string | number | boolean | null>;
};
```

- [ ] **Step 1: Import the current public Tideborne fish manifest/data as migration input**

Use the existing `RedsLovesGames/random-info-pages/tideborne/assets/fish` data and renders only as a migration source. The production site must not fetch that repository at runtime.

- [ ] **Step 2: Write failing normalization tests**

Fixtures cover:

1. complete record with render and habitat;
2. record with no render;
3. record missing optional habitat/category;
4. duplicate `source + id`;
5. record referencing a render file that does not exist.

Expected behavior:

- complete and partial records normalize;
- duplicate canonical keys throw an error naming the duplicate;
- non-null missing render references throw an error naming the missing file.

- [ ] **Step 3: Implement the normalizer**

Preserve canonical IDs and source attribution. Derive only website slugs/display-normalization. Do not infer missing rarity, habitat, category, catch rules, or traits from model knowledge.

- [ ] **Step 4: Copy only referenced renders**

Every non-null `render` and `variants[...]` path in `fish-data.json` must resolve under `site/public/fish/renders/`.

- [ ] **Step 5: Run dataset integrity tests**

```bash
npm test -- scripts/normalize-fish-data.test.mjs
```

Also assert:

```text
unique(source + id)
unique(source + slug)
rarity is null or integer 1..5
all referenced renders exist
variant keys are from the approved enum
```

- [ ] **Step 6: Commit**

```bash
git add site/scripts/normalize-fish-data* site/src/data/fish site/public/fish
git commit -m "feat(wiki): import normalized Tideborne fish catalog"
```

### Task 5: Build the responsive Fish Explorer

**Files:**
- Create: `site/src/data/fish/filterFish.ts`
- Create: `site/src/data/fish/filterFish.test.ts`
- Create: `site/src/pages/fish/index.astro`
- Create: `site/src/components/fish/FishExplorer.tsx`
- Create: `site/src/components/fish/FishFilters.tsx`
- Create: `site/src/components/fish/FishCard.tsx`
- Create: `site/src/components/fish/FishEmptyState.tsx`
- Create: `site/src/components/fish/FishExplorer.test.tsx`

**Interfaces:**

```ts
export type FishFilters = {
  query: string;
  sources: string[];
  rarities: number[];
  habitats: string[];
  journalCategories: string[];
};

export function filterFish(fish: FishEntry[], filters: FishFilters): FishEntry[];
```

- [ ] **Step 1: Write pure filter tests**

Cover case-insensitive name search, canonical ID search, source, rarity, habitat, journal category, combined filters, zero results, and records with missing optional fields.

Semantics are locked as:

- OR within a filter group;
- AND between filter groups;
- text query ANDs with all selected groups.

- [ ] **Step 2: Run and verify failure**

```bash
npm test -- src/data/fish/filterFish.test.ts
```

Expected: FAIL because `filterFish` is missing.

- [ ] **Step 3: Implement the pure filter function and rerun tests**

Expected: PASS.

- [ ] **Step 4: Write Fish Explorer interaction tests before the component**

Assert:

- typing a query updates visible result count;
- selecting/deselecting rarity changes results;
- a zero-match state renders `No fish match those filters.`;
- `Clear filters` restores the complete dataset;
- every filter has an accessible label;
- the mobile `Filters` trigger is keyboard operable;
- Escape closes the mobile Sheet.

- [ ] **Step 5: Implement the custom route with Starlight's official custom-page wrapper**

`site/src/pages/fish/index.astro` uses:

```astro
---
import StarlightPage from '@astrojs/starlight/components/StarlightPage.astro';
import FishExplorer from '../../components/fish/FishExplorer';
import fish from '../../data/fish/fish-data.json';
---

<StarlightPage frontmatter={{ title: 'Fish Wiki', description: 'Browse every fish Tideborne understands.' }}>
  <FishExplorer fish={fish} client:load />
</StarlightPage>
```

- [ ] **Step 6: Implement intentional desktop/mobile layouts**

Desktop:

- search above results;
- sticky left filter region;
- result count and card grid in main region.

Mobile:

- search;
- `Filters` button;
- active-filter count;
- result count;
- cards;
- filter controls inside shadcn `Sheet`.

Cards prioritize render, name, rarity, habitat, and source. Technical fields stay off cards.

All fish-detail links use `sitePath()` or `import.meta.env.BASE_URL`; no hard-coded `/fish/...` root links.

- [ ] **Step 7: Implement honest states**

No-results gets the explicit empty state plus reset. Missing renders use an accessible static placeholder. Do not fake network-loading skeletons because the catalog is locally bundled.

- [ ] **Step 8: Run all milestone tests/check/build**

```bash
npm test
npm run check
npm run build
```

Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add site/src/pages/fish site/src/components/fish site/src/data/fish
git commit -m "feat(wiki): add searchable responsive Fish Explorer"
```

### Task 6: Generate one static detail route per fish

**Files:**
- Create: `site/src/pages/fish/[source]/[slug].astro`
- Create: `site/src/components/fish/FishRender.astro`
- Create: `site/src/components/fish/FishIdentity.astro`
- Create: `site/src/components/fish/FishMetadata.astro`
- Create: `site/src/components/fish/FishVariantStrip.astro`
- Add: `site/src/data/fish/fishPages.test.ts`

**Interfaces:**
- Consumes normalized `FishEntry[]`.
- Produces `/fish/<source>/<slug>/` per unique entry.

- [ ] **Step 1: Write route-generation integrity tests**

Assert every fish maps to exactly one unique path and no `source + slug` pair collides.

- [ ] **Step 2: Implement `getStaticPaths()` from local fish data**

Pass the full `FishEntry` as static props. Do not fetch catalog data client-side.

- [ ] **Step 3: Implement `FishRender` fallback behavior**

If `render` is null, render a visible placeholder with `Render unavailable`. The page's metadata remains usable.

- [ ] **Step 4: Implement detail hierarchy**

Order:

1. back link to Fish Wiki;
2. name + rarity + source;
3. large render/fallback;
4. habitats and verified metadata;
5. only verified available variant renders;
6. technical canonical ID/source information;
7. visible `Verified for Tideborne 2.1.0` context.

Do not fabricate FishScore maxima, catch advice, gear recommendations, or trait availability from absent data.

- [ ] **Step 5: Test partial records and production build**

Fixtures with null render, empty habitats, null rarity, and null category must all build.

```bash
npm test
npm run build
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add site/src/pages/fish site/src/components/fish site/src/data/fish/fishPages.test.ts
git commit -m "feat(wiki): generate fish detail pages"
```

### Task 7: Move GitHub Pages deployment to Astro's current official action

**Files:**
- Modify: `.github/workflows/pages.yml`

**Interfaces:**
- Consumes `site/package-lock.json` and `site/package.json`.
- Produces a static GitHub Pages artifact from `site/`.

- [ ] **Step 1: Replace manual `_site` assembly with the official Astro action**

Use:

```yaml
- uses: actions/checkout@v6
- uses: withastro/action@v6
  with:
    path: ./site
```

Deploy with:

```yaml
- uses: actions/deploy-pages@v5
```

Keep permissions:

```yaml
contents: read
pages: write
id-token: write
```

Keep the `github-pages` environment.

- [ ] **Step 2: Preserve narrow triggers**

Pushes to `dev` trigger only when these paths change:

```text
docs/wiki/**
site/**
.github/workflows/pages.yml
```

Also retain `workflow_dispatch`.

- [ ] **Step 3: Run production base-path checks**

```bash
cd site
npm ci
npm test
npm run check
npm run build
test -f dist/index.html
test -f dist/fish/index.html
! grep -R 'href="/fish/' dist --include='*.html'
! grep -R 'src="/fish/' dist --include='*.html'
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/pages.yml site/package-lock.json
git commit -m "ci(wiki): deploy Astro field guide to GitHub Pages"
```

### Task 8: Milestone QA and repository state update

**Files:**
- Modify `docs/CURRENT_STATE.md` only if the verified website state actually changed.
- Modify `docs/TODO.md` only to mark completed wiki work and preserve later milestones.

- [ ] **Step 1: Run complete gates**

```bash
cd site
npm ci
npm test
npm run check
npm run build
```

Expected: all PASS.

- [ ] **Step 2: Responsive QA**

Check approximately 1440px, 1024px, 768px, and 390px:

- search is immediately visible;
- desktop filters remain usable without viewport overflow;
- mobile filters use the Sheet;
- result cards do not overflow;
- long names wrap safely;
- missing-render cards keep layout integrity;
- docs remain readable.

- [ ] **Step 3: Keyboard and reduced-motion QA**

Verify Tab/Shift+Tab, Space/Enter on checkboxes/buttons, Escape on Sheet, visible focus, and no information loss under `prefers-reduced-motion: reduce`.

- [ ] **Step 4: Content-integrity QA**

Confirm:

- `docs/wiki/` was not rewritten;
- generated docs contain no stale file from deleted source Markdown;
- every fish card detail link resolves to emitted HTML;
- every non-null render path resolves to a local file;
- every fish page states the 2.1.0 verification boundary.

- [ ] **Step 5: Update repository state docs surgically**

Record verified outcomes only. Do not add a historical stage narrative.

- [ ] **Step 6: Commit**

```bash
git add docs/CURRENT_STATE.md docs/TODO.md
git commit -m "docs: record field guide fish explorer milestone"
```

- [ ] **Step 7: Whole-branch review**

Review `dev...wiki/official-field-guide` for accidental Java/gameplay changes, duplicated documentation authority, unnecessary dependencies, broken base-path URLs, accessibility regressions, and copied component code outside approved license boundaries.

## Follow-on implementation plans

After this milestone is working and reviewed, create separate plans for:

1. **Canonical FishScore reference + calculator**: generated FishScore constants, contribution breakdown, exact 1-3000 output, Motion Primitives animated number with reduced-motion fallback.
2. **Gear catalog + Gear Builder**: structured gear/recipe data, Kibo Combobox slot selection, canonical modifier/effect summaries.
3. **Homepage + grouped global search + visual polish**: feature-driven Tideborne homepage, Ctrl/Cmd+K grouped search, restrained high-value motion.
4. **Legacy-site migration and release cutover**: old-site redirects/links, CurseForge wiki target, final Pages verification.

Every follow-on plan keeps the same source-of-truth rule: the website renders canonical/versioned Tideborne data and never becomes a second gameplay authority.

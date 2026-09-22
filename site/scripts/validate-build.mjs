import { access, readFile, readdir } from 'node:fs/promises';
import { constants } from 'node:fs';
import { dirname, extname, join, relative, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));

const normalizeBasePath = (basePath) => {
  const withLeading = basePath.startsWith('/') ? basePath : `/${basePath}`;
  return withLeading.endsWith('/') ? withLeading : `${withLeading}/`;
};

const normalizeRoute = (route) => {
  if (!route || route === '/') return '/';
  const withLeading = route.startsWith('/') ? route : `/${route}`;
  return withLeading.endsWith('/') || extname(withLeading)
    ? withLeading
    : `${withLeading}/`;
};

async function exists(path) {
  try {
    await access(path, constants.F_OK);
    return true;
  } catch {
    return false;
  }
}

async function htmlFiles(root) {
  const found = [];

  async function walk(directory) {
    for (const entry of await readdir(directory, { withFileTypes: true })) {
      const path = join(directory, entry.name);
      if (entry.isDirectory()) await walk(path);
      else if (entry.isFile() && entry.name.endsWith('.html')) found.push(path);
    }
  }

  if (await exists(root)) await walk(root);
  return found;
}

function routeForHtml(distDir, file) {
  const local = relative(distDir, file).split(sep).join('/');
  if (local === 'index.html') return '/';
  if (local.endsWith('/index.html')) return `/${local.slice(0, -'index.html'.length)}`;
  return `/${local}`;
}

function hrefsFromHtml(html) {
  const hrefs = [];
  const pattern = /\bhref\s*=\s*(?:"([^"]*)"|'([^']*)')/gi;
  for (const match of html.matchAll(pattern)) hrefs.push(match[1] ?? match[2] ?? '');
  return hrefs;
}

function shouldIgnoreHref(href) {
  const value = href.trim();
  return !value
    || value.startsWith('#')
    || value.startsWith('//')
    || /^[a-z][a-z0-9+.-]*:/i.test(value);
}

function stripQueryAndHash(href) {
  const hash = href.indexOf('#');
  const query = href.indexOf('?');
  const cut = [hash, query].filter((index) => index >= 0).sort((a, b) => a - b)[0];
  return cut === undefined ? href : href.slice(0, cut);
}

function resolveInternalRoute({ href, currentRoute, basePath }) {
  const clean = stripQueryAndHash(href.trim());
  if (!clean) return currentRoute;

  if (clean.startsWith('/')) {
    if (!clean.startsWith(basePath)) return null;
    const withoutBase = clean.slice(basePath.length);
    return normalizeRoute(`/${withoutBase}`);
  }

  const currentUrl = new URL(currentRoute, 'https://tideborne.invalid');
  const resolved = new URL(clean, currentUrl);
  return normalizeRoute(resolved.pathname);
}

async function routeExists(distDir, route) {
  const normalized = normalizeRoute(route);
  const local = normalized === '/' ? '' : normalized.replace(/^\//, '');

  if (normalized.endsWith('/')) {
    return exists(join(distDir, local, 'index.html'));
  }

  const exact = join(distDir, local);
  if (await exists(exact)) return true;

  if (!extname(normalized)) {
    if (await exists(`${exact}.html`)) return true;
    if (await exists(join(exact, 'index.html'))) return true;
  }

  return false;
}

export async function validateBuild({ distDir, basePath = '/TIDEBORNE/', requiredRoutes = [] }) {
  const root = resolve(distDir);
  const base = normalizeBasePath(basePath);
  const issues = [];

  for (const route of requiredRoutes) {
    if (!(await routeExists(root, route))) issues.push(`Missing required route: ${normalizeRoute(route)}`);
  }

  for (const file of await htmlFiles(root)) {
    const currentRoute = routeForHtml(root, file);
    const html = await readFile(file, 'utf8');

    for (const href of hrefsFromHtml(html)) {
      if (shouldIgnoreHref(href)) continue;

      const targetRoute = resolveInternalRoute({ href, currentRoute, basePath: base });
      if (targetRoute === null) {
        issues.push(`Internal href escapes deployment base path in ${currentRoute}: ${href}`);
        continue;
      }

      if (!(await routeExists(root, targetRoute))) {
        issues.push(`Broken internal href in ${currentRoute}: ${href} -> ${targetRoute}`);
      }
    }
  }

  return [...new Set(issues)];
}

async function main() {
  const distDir = resolve(here, '../dist');
  const requiredRoutes = [
    '/',
    '/fish/',
    '/gear/',
    '/satchel/',
    '/records/',
    '/specimens/',
    '/tools/fishscore/',
    '/wiki-home/',
  ];

  const issues = await validateBuild({ distDir, basePath: '/TIDEBORNE/', requiredRoutes });
  if (issues.length) {
    console.error(`Built-site validation found ${issues.length} issue(s):`);
    for (const issue of issues) console.error(`- ${issue}`);
    process.exitCode = 1;
    return;
  }

  console.log(`Built-site validation passed for ${requiredRoutes.length} required routes and all internal hrefs.`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  await main();
}

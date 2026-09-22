import { afterEach, describe, expect, it } from 'vitest';
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { validateBuild } from './validate-build.mjs';

const roots = [];

function fixture() {
  const root = mkdtempSync(join(tmpdir(), 'tideborne-build-'));
  roots.push(root);
  return root;
}

function page(root, route, html) {
  const directory = route === '/' ? root : join(root, route.replace(/^\//, ''));
  mkdirSync(directory, { recursive: true });
  writeFileSync(join(directory, 'index.html'), html, 'utf8');
}

afterEach(() => {
  while (roots.length) rmSync(roots.pop(), { recursive: true, force: true });
});

describe('validateBuild', () => {
  it('accepts base-path and sibling-route links that resolve in the built site', async () => {
    const root = fixture();
    page(root, '/', '<a href="/TIDEBORNE/fish/">Fish</a>');
    page(root, '/fish/', '<a href="../wiki-home/">Wiki</a>');
    page(root, '/wiki-home/', '<a href="https://example.com/">External</a>');

    const result = await validateBuild({
      distDir: root,
      basePath: '/TIDEBORNE/',
      requiredRoutes: ['/', '/fish/', '/wiki-home/'],
    });

    expect(result).toEqual([]);
  });

  it('reports required routes and internal hrefs that do not exist', async () => {
    const root = fixture();
    page(root, '/', '<a href="/TIDEBORNE/missing/">Missing</a>');

    const result = await validateBuild({
      distDir: root,
      basePath: '/TIDEBORNE/',
      requiredRoutes: ['/', '/fish/'],
    });

    expect(result.some((issue) => issue.includes('Missing required route: /fish/'))).toBe(true);
    expect(result.some((issue) => issue.includes('/TIDEBORNE/missing/'))).toBe(true);
  });
});

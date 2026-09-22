import { afterEach, describe, expect, it } from 'vitest';
import {
  existsSync,
  mkdtempSync,
  mkdirSync,
  readFileSync,
  readdirSync,
  rmSync,
  unlinkSync,
  writeFileSync,
} from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';

const roots = [];

function fixture() {
  const root = mkdtempSync(join(tmpdir(), 'tideborne-docs-'));
  roots.push(root);
  const sourceDir = join(root, 'source');
  const outputDir = join(root, 'output');
  mkdirSync(sourceDir, { recursive: true });
  mkdirSync(outputDir, { recursive: true });
  return { sourceDir, outputDir };
}

afterEach(() => {
  while (roots.length) {
    rmSync(roots.pop(), { recursive: true, force: true });
  }
});

describe('syncDocs', () => {
  it('rebuilds generated docs cleanly and rewrites source links to published routes', async () => {
    const { syncDocs } = await import('./sync-docs.mjs');
    const { sourceDir, outputDir } = fixture();

    writeFileSync(
      join(sourceDir, 'README.md'),
      '# Tideborne Wiki\n\n[FishScore](FISHSCORE.md) · [FishScore section](FISHSCORE.md#mapping)\n',
    );
    writeFileSync(
      join(sourceDir, 'FISHSCORE.md'),
      '# FishScore\n\n[Wiki Home](README.md) · [External](https://example.com/FISHSCORE.md)\n',
    );
    writeFileSync(join(outputDir, 'STALE.md'), '# Old output\n');

    await syncDocs({ sourceDir, outputDir });

    expect(readdirSync(outputDir).sort()).toEqual(['fishscore.md', 'wiki-home.md']);
    const wikiHome = readFileSync(join(outputDir, 'wiki-home.md'), 'utf8');
    const fishScore = readFileSync(join(outputDir, 'fishscore.md'), 'utf8');
    expect(wikiHome).toContain('title: Tideborne Wiki');
    expect(wikiHome).toContain('[FishScore](../fishscore/)');
    expect(wikiHome).toContain('[FishScore section](../fishscore/#mapping)');
    expect(fishScore).toContain('[Wiki Home](../wiki-home/)');
    expect(fishScore).toContain('[External](https://example.com/FISHSCORE.md)');
    expect(existsSync(join(outputDir, 'STALE.md'))).toBe(false);

    unlinkSync(join(sourceDir, 'FISHSCORE.md'));
    await syncDocs({ sourceDir, outputDir });

    expect(readdirSync(outputDir)).toEqual(['wiki-home.md']);
  });
});

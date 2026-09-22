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
  it('rebuilds generated docs cleanly while reserving / for the custom homepage', async () => {
    const { syncDocs } = await import('./sync-docs.mjs');
    const { sourceDir, outputDir } = fixture();

    writeFileSync(join(sourceDir, 'README.md'), '# Tideborne Wiki\n\nWelcome.\n');
    writeFileSync(join(sourceDir, 'FISHSCORE.md'), '# FishScore\n\nExact scoring.\n');
    writeFileSync(join(outputDir, 'STALE.md'), '# Old output\n');

    await syncDocs({ sourceDir, outputDir });

    expect(readdirSync(outputDir).sort()).toEqual(['fishscore.md', 'wiki-home.md']);
    expect(readFileSync(join(outputDir, 'wiki-home.md'), 'utf8')).toContain('title: Tideborne Wiki');
    expect(readFileSync(join(outputDir, 'fishscore.md'), 'utf8')).toContain('# FishScore');
    expect(existsSync(join(outputDir, 'STALE.md'))).toBe(false);

    unlinkSync(join(sourceDir, 'FISHSCORE.md'));
    await syncDocs({ sourceDir, outputDir });

    expect(readdirSync(outputDir)).toEqual(['wiki-home.md']);
  });
});

import { afterEach, describe, expect, it, vi } from 'vitest';
import { mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { collectRenderFiles, syncFishRenders } from './sync-fish-renders.mjs';

const roots = [];

afterEach(() => {
  while (roots.length) rmSync(roots.pop(), { recursive: true, force: true });
});

describe('fish render asset sync', () => {
  it('collects each validated render filename once', () => {
    const catalog = {
      records: [
        {
          render: '/fish/renders/dragon.png',
          variants: {
            normal: '/fish/renders/dragon.png',
            giant: '/fish/renders/dragon__giant.png',
          },
        },
        { render: null, variants: {} },
      ],
    };

    expect(collectRenderFiles(catalog)).toEqual(['dragon.png', 'dragon__giant.png']);
  });

  it('downloads missing pinned assets and reuses files already present', async () => {
    const root = mkdtempSync(join(tmpdir(), 'tideborne-renders-'));
    roots.push(root);
    const outputDir = join(root, 'renders');
    mkdirSync(outputDir, { recursive: true });
    writeFileSync(join(outputDir, 'dragon.png'), 'cached');

    const catalogFile = join(root, 'fish.json');
    writeFileSync(catalogFile, JSON.stringify({
      meta: { sourceRevision: 'abc1234' },
      records: [{
        render: '/fish/renders/dragon.png',
        variants: { giant: '/fish/renders/dragon__giant.png' },
      }],
    }));

    const fetchImpl = vi.fn(async () => new Response(new Uint8Array([1, 2, 3]), { status: 200 }));
    const result = await syncFishRenders({ catalogFile, outputDir, fetchImpl });

    expect(result).toEqual({ total: 2, downloaded: 1, reused: 1 });
    expect(fetchImpl).toHaveBeenCalledTimes(1);
    expect(String(fetchImpl.mock.calls[0][0])).toContain('/abc1234/tideborne/assets/fish/renders/dragon__giant.png');
    expect([...readFileSync(join(outputDir, 'dragon__giant.png'))]).toEqual([1, 2, 3]);
  });
});

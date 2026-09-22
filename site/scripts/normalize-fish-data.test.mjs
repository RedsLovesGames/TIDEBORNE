import { describe, expect, it } from 'vitest';
import { normalizeCatalog } from './normalize-fish-data.mjs';

describe('normalizeCatalog', () => {
  it('filters by modpack scope and maps source-backed renders without inventing missing data', () => {
    const first = {
      meta: { version: 'test' },
      records: [
        {
          id: 'tide:dragon_fish',
          name: 'Dragon Fish',
          mod: 'Tide',
          rarity: 'legendary',
          stars: 5,
          group: 'saltwater',
          location: 'Deep Ocean',
          locationKey: 'deep_ocean',
          typicalLow: 85,
          typicalHigh: 140,
          recordHigh: 180,
          associatedMods: [],
          conditions: ['raining'],
        },
        {
          id: 'excluded:test_fish',
          name: 'Excluded Fish',
          mod: 'Excluded',
        },
      ],
    };
    const second = {
      records: [
        {
          id: 'minecraft:cod',
          name: 'Cod',
          sourceMod: 'Minecraft',
          rarity: 'common',
          stars: 1,
        },
      ],
    };
    const scope = { mod_ids: ['tide'], include_minecraft: true };
    const manifest = {
      fish: {
        'tide:dragon_fish': {
          variants: {
            normal: { status: 'ok', file: 'renders/dragon-normal.png' },
            giant: { status: 'ok', file: 'dragon-giant.png' },
            albino: { status: 'unavailable' },
          },
        },
      },
    };

    const result = normalizeCatalog({ first, second, scope, manifest, sourceRevision: 'abc123' });

    expect(result.meta.sourceRevision).toBe('abc123');
    expect(result.records.map((record) => record.id)).toEqual(['minecraft:cod', 'tide:dragon_fish']);

    const dragon = result.records.find((record) => record.id === 'tide:dragon_fish');
    expect(dragon).toMatchObject({
      source: 'Tide',
      namespace: 'tide',
      slug: 'dragon-fish',
      rarity: 5,
      habitats: ['Deep Ocean'],
      render: '/fish/renders/dragon-normal.png',
      variants: {
        normal: '/fish/renders/dragon-normal.png',
        giant: '/fish/renders/dragon-giant.png',
      },
    });

    const cod = result.records.find((record) => record.id === 'minecraft:cod');
    expect(cod).toMatchObject({
      render: null,
      habitats: [],
      rarity: 1,
      size: { typicalLow: null, typicalHigh: null, recordHigh: null },
    });
  });
});

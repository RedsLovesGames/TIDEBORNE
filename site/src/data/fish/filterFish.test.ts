import { describe, expect, it } from 'vitest';
import { filterFish } from './filterFish';

const fish = [
  {
    id: 'tide:dragon_fish',
    source: 'Tide',
    namespace: 'tide',
    slug: 'dragon-fish',
    name: 'Dragon Fish',
    rarity: 5,
    rarityKey: 'legendary',
    stars: 5,
    group: 'saltwater',
    habitats: ['Deep Ocean'],
    locationKey: 'deep_ocean',
    journalCategory: null,
    size: { typicalLow: 85, typicalHigh: 140, recordHigh: 180 },
    render: '/fish/renders/tide__dragon_fish__normal.png',
    variants: { normal: '/fish/renders/tide__dragon_fish__normal.png' },
    associatedMods: [],
    conditions: [],
  },
  {
    id: 'example:mystery_fish',
    source: 'Example Mod',
    namespace: 'example',
    slug: 'mystery-fish',
    name: 'Mystery Fish',
    rarity: null,
    rarityKey: null,
    stars: null,
    group: null,
    habitats: [],
    locationKey: null,
    journalCategory: null,
    size: { typicalLow: null, typicalHigh: null, recordHigh: null },
    render: null,
    variants: {},
    associatedMods: ['example'],
    conditions: [],
  },
] as const;

describe('filterFish', () => {
  it('searches display name, id, source, and habitat text', () => {
    expect(filterFish(fish, { query: 'dragon' }).map((entry) => entry.id)).toEqual(['tide:dragon_fish']);
    expect(filterFish(fish, { query: 'tide:' }).map((entry) => entry.id)).toEqual(['tide:dragon_fish']);
    expect(filterFish(fish, { query: 'example mod' }).map((entry) => entry.id)).toEqual(['example:mystery_fish']);
    expect(filterFish(fish, { query: 'deep ocean' }).map((entry) => entry.id)).toEqual(['tide:dragon_fish']);
  });

  it('combines source, rarity, habitat, and group filters', () => {
    expect(filterFish(fish, {
      sources: ['Tide'],
      rarities: [5],
      habitats: ['Deep Ocean'],
      groups: ['saltwater'],
    }).map((entry) => entry.id)).toEqual(['tide:dragon_fish']);

    expect(filterFish(fish, { sources: ['Tide'], rarities: [1] })).toEqual([]);
  });

  it('keeps partial records searchable without throwing', () => {
    expect(filterFish(fish, { query: 'mystery' }).map((entry) => entry.id)).toEqual(['example:mystery_fish']);
    expect(filterFish(fish, { habitats: ['Unknown'] })).toEqual([]);
  });
});

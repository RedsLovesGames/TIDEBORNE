import { describe, expect, it } from 'vitest';
import { buildFishRoutes, findFishByRoute } from './fishRoutes';

const records = [
  { id: 'tide:dragon_fish', namespace: 'tide', slug: 'dragon-fish', name: 'Dragon Fish' },
  { id: 'example:dragon_fish', namespace: 'example', slug: 'dragon-fish', name: 'Other Dragon Fish' },
  { id: 'minecraft:cod', namespace: 'minecraft', slug: 'cod', name: 'Cod' },
] as const;

describe('fish detail routes', () => {
  it('builds stable namespace plus slug routes without cross-mod collisions', () => {
    expect(buildFishRoutes(records)).toEqual([
      { params: { namespace: 'example', slug: 'dragon-fish' }, props: { record: records[1] } },
      { params: { namespace: 'minecraft', slug: 'cod' }, props: { record: records[2] } },
      { params: { namespace: 'tide', slug: 'dragon-fish' }, props: { record: records[0] } },
    ]);
  });

  it('finds a fish only when both namespace and slug match', () => {
    expect(findFishByRoute(records, 'tide', 'dragon-fish')?.id).toBe('tide:dragon_fish');
    expect(findFishByRoute(records, 'example', 'dragon-fish')?.id).toBe('example:dragon_fish');
    expect(findFishByRoute(records, 'tide', 'cod')).toBeUndefined();
  });
});

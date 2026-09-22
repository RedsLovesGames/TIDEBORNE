import { describe, expect, it } from 'vitest';
import { capacityLevels, satchelFeatures } from './satchelProgression';

describe('satchelProgression', () => {
  it('matches the six canonical Satchel feature defaults', () => {
    expect(satchelFeatures.map((feature) => [feature.id, feature.xpCost])).toEqual([
      ['tackle_organizer', 100],
      ['auto_stow', 200],
      ['record_keeper', 250],
      ['trait_scanner', 300],
      ['trophy_lock', 350],
      ['shared_ledger', 400],
    ]);
    expect(satchelFeatures.filter((feature) => feature.requiresMultiplayerExtras).map((feature) => feature.id)).toEqual(['shared_ledger']);
  });

  it('preserves the documented 2.1.0 capacity progression', () => {
    expect(capacityLevels.map((level) => [level.name, level.multiplier, level.xpCost])).toEqual([
      ['Base', 1, 0],
      ['Capacity I', 1.5, 150],
      ['Capacity II', 2, 450],
      ['Capacity III', 3, 1000],
    ]);
  });
});

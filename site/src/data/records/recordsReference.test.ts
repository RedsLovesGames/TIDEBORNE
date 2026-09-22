import { describe, expect, it } from 'vitest';
import { hallSortModes, leaderboardMetrics, recordEventTypes } from './recordsReference';

describe('recordsReference', () => {
  it('keeps the documented Team Journal leaderboard metrics', () => {
    expect(leaderboardMetrics).toEqual([
      'Catches',
      'Unique Species',
      'Records Set',
      'Active Records',
      'Best FishScore',
    ]);
  });

  it('keeps the documented history event and Hall sort vocabularies', () => {
    expect(recordEventTypes.map((event) => event.name)).toEqual([
      'First Discovery',
      'New Largest',
      'New Smallest',
      'Ownership Repair',
    ]);
    expect(hallSortModes).toEqual(['Best', 'Worst', 'Newest', 'Oldest']);
  });
});

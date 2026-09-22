import { describe, expect, it } from 'vitest';
import { rarityTraitFactors, specimenAxes, traitLuckProbability } from './specimenReference';

describe('specimenReference', () => {
  it('keeps the four independent canonical specimen axes', () => {
    expect(specimenAxes.map((axis) => [axis.name, axis.outcomes])).toEqual([
      ['Body Type', ['Normal', 'Giant', 'Dwarf']],
      ['Condition', ['Normal', 'Scarred', 'Parasite-Ridden']],
      ['Pigmentation', ['Normal', 'Albino', 'Iridescent']],
      ['Specimen Quality', ['Normal', 'Perfect Specimen']],
    ]);
  });

  it('keeps the documented rarity compensation factors', () => {
    expect(rarityTraitFactors).toEqual([1, 1.15, 1.4, 1.8, 2.4]);
  });

  it('uses the canonical Trait Luck probability transform', () => {
    expect(traitLuckProbability(0.05, 10)).toBeCloseTo(0.0975, 8);
    expect(traitLuckProbability(0, 15)).toBe(0);
    expect(traitLuckProbability(1, 15)).toBe(1);
  });
});

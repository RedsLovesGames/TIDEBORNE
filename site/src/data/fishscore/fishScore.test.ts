import { describe, expect, it } from 'vitest';
import { calculateFishScore, normalizeFishScore, speciesPoints } from './fishScore';

describe('FishScore V2 parity', () => {
  it('maps the frozen minimum and maximum cases exactly', () => {
    expect(calculateFishScore({
      rarity: 1,
      finalPercentile: 0,
      bodyType: 'NORMAL',
      condition: 'NORMAL',
      pigmentation: 'NORMAL',
      quality: 'NORMAL',
    })).toEqual({ rawScore: 50, fishScore: 1 });

    expect(calculateFishScore({
      rarity: 5,
      finalPercentile: 100,
      bodyType: 'GIANT',
      condition: 'PARASITE_RIDDEN',
      pigmentation: 'IRIDESCENT',
      quality: 'PERFECT_SPECIMEN',
    })).toEqual({ rawScore: 925, fishScore: 3000 });
  });

  it('matches the canonical Java intermediate fixtures', () => {
    expect(calculateFishScore({
      rarity: 2,
      finalPercentile: 50,
      bodyType: 'NORMAL',
      condition: 'NORMAL',
      pigmentation: 'NORMAL',
      quality: 'NORMAL',
    })).toEqual({ rawScore: 250, fishScore: 686 });

    expect(calculateFishScore({
      rarity: 3,
      finalPercentile: 50,
      bodyType: 'NORMAL',
      condition: 'SCARRED',
      pigmentation: 'NORMAL',
      quality: 'NORMAL',
    })).toEqual({ rawScore: 345, fishScore: 1012 });

    expect(calculateFishScore({
      rarity: 4,
      finalPercentile: 75,
      bodyType: 'NORMAL',
      condition: 'NORMAL',
      pigmentation: 'ALBINO',
      quality: 'NORMAL',
    })).toEqual({ rawScore: 545, fishScore: 1698 });
  });

  it('keeps every frozen species value exact', () => {
    expect([1, 2, 3, 4, 5].map((rarity) => speciesPoints(rarity as 1 | 2 | 3 | 4 | 5)))
      .toEqual([50, 100, 175, 250, 350]);
  });

  it('clamps outside the frozen raw range', () => {
    expect(normalizeFishScore(-1000)).toBe(1);
    expect(normalizeFishScore(49.999)).toBe(1);
    expect(normalizeFishScore(925.001)).toBe(3000);
    expect(normalizeFishScore(10000)).toBe(3000);
  });

  it('rejects non-finite raw values', () => {
    expect(() => normalizeFishScore(Number.NaN)).toThrow(/finite/i);
    expect(() => normalizeFishScore(Number.POSITIVE_INFINITY)).toThrow(/finite/i);
    expect(() => normalizeFishScore(Number.NEGATIVE_INFINITY)).toThrow(/finite/i);
  });
});

import { describe, expect, it } from 'vitest';
import { composeGearPreview, gearOptions } from './gearCatalog';

describe('gearCatalog', () => {
  it('contains the five player-facing loadout slots with compatibility-safe IDs', () => {
    expect(new Set(gearOptions.map((option) => option.slot))).toEqual(
      new Set(['rod', 'line', 'hook', 'leader', 'bait']),
    );
    expect(gearOptions.find((option) => option.id === 'tidebound_compatibility:swift_line')?.name).toBe('Abaia Line');
    expect(gearOptions.find((option) => option.id === 'tidebound_compatibility:steel_leader')?.name).toBe('Iron Leader');
  });

  it('composes additive and multiplicative effects using Fishing System 2 semantics', () => {
    const preview = composeGearPreview([
      'tide:diamond_fishing_rod',
      'tide:diamond_line',
      'tidebound_compatibility:diamond_leader',
      'tidebound_compatibility:leviathan_bait',
    ]);

    expect(preview.fishingLuck).toBe(4);
    expect(preview.traitLuck).toBe(1);
    expect(preview.strengthMultiplier).toBeCloseTo(0.98072, 5);
    expect(preview.tempoMultiplier).toBeCloseTo(1.272, 5);
    expect(preview.catchZoneMultiplier).toBeCloseTo(0.82, 5);
    expect(preview.minigameSpeedMultiplier).toBeCloseTo(1.1, 5);
    expect(preview.catchLossPrevention).toBe(0.95);
    expect(preview.trophyFightRelief).toBe(0.25);
  });

  it('caps public preview values at the same consumption limits as FishingGearEffects', () => {
    const preview = composeGearPreview([
      'tide:netherite_fishing_rod',
      'tide:diamond_line',
      'tidebound_compatibility:diamond_leader',
      'tidebound_compatibility:leviathan_bait',
    ]);

    expect(preview.catchLossPrevention).toBe(0.95);
    expect(preview.minigameSpeedMultiplier).toBeLessThanOrEqual(1.25);
    expect(preview.strengthMultiplier).toBeGreaterThanOrEqual(0.7);
    expect(preview.tempoMultiplier).toBeLessThanOrEqual(1.3);
  });
});

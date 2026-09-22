export type SpecimenAxis = {
  name: string;
  outcomes: string[];
  description: string;
};

export const specimenAxes: SpecimenAxis[] = [
  {
    name: 'Body Type',
    outcomes: ['Normal', 'Giant', 'Dwarf'],
    description: 'Changes final physical size after the one natural percentile roll.',
  },
  {
    name: 'Condition',
    outcomes: ['Normal', 'Scarred', 'Parasite-Ridden'],
    description: 'Independent condition state that can coexist with every other axis.',
  },
  {
    name: 'Pigmentation',
    outcomes: ['Normal', 'Albino', 'Iridescent'],
    description: 'Independent coloration state with its own FishScore contribution.',
  },
  {
    name: 'Specimen Quality',
    outcomes: ['Normal', 'Perfect Specimen'],
    description: 'Quality result tied strongly to an exceptional natural specimen.',
  },
];

export const rarityTraitFactors = [1, 1.15, 1.4, 1.8, 2.4] as const;

export const perfectSpecimenCurve = [
  { percentile: 'Below 95', chance: '0%' },
  { percentile: '95', chance: '2%' },
  { percentile: '97.5', chance: '8%' },
  { percentile: '99', chance: '25%' },
  { percentile: '99.9+', chance: '60%' },
] as const;

export function traitLuckProbability(baseProbability: number, traitLuck: number): number {
  if (!Number.isFinite(baseProbability) || baseProbability < 0 || baseProbability > 1) {
    throw new RangeError('baseProbability must be finite and within [0, 1]');
  }
  if (!Number.isFinite(traitLuck)) throw new RangeError('traitLuck must be finite');
  return 1 - Math.pow(1 - baseProbability, 1 + traitLuck / 10);
}

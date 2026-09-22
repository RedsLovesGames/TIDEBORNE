export const RAW_MIN = 50;
export const RAW_MAX = 925;
export const SCORE_MIN = 1;
export const SCORE_MAX = 3000;

export type FishRarity = 1 | 2 | 3 | 4 | 5;
export type BodyType = 'NORMAL' | 'GIANT' | 'DWARF';
export type Condition = 'NORMAL' | 'SCARRED' | 'PARASITE_RIDDEN';
export type Pigmentation = 'NORMAL' | 'ALBINO' | 'IRIDESCENT';
export type SpecimenQuality = 'NORMAL' | 'PERFECT_SPECIMEN';

export type FishScoreInput = {
  rarity: FishRarity;
  finalPercentile: number;
  bodyType: BodyType;
  condition: Condition;
  pigmentation: Pigmentation;
  quality: SpecimenQuality;
};

export type FishScoreResult = {
  rawScore: number;
  fishScore: number;
};

export type FishScoreBreakdown = {
  species: number;
  percentile: number;
  bodyType: number;
  condition: number;
  pigmentation: number;
  quality: number;
  rawScore: number;
  fishScore: number;
};

const speciesTable: Record<FishRarity, number> = {
  1: 50,
  2: 100,
  3: 175,
  4: 250,
  5: 350,
};

const bodyTypeTable: Record<BodyType, number> = {
  NORMAL: 0,
  GIANT: 40,
  DWARF: 40,
};

const conditionTable: Record<Condition, number> = {
  NORMAL: 0,
  SCARRED: 20,
  PARASITE_RIDDEN: 35,
};

const pigmentationTable: Record<Pigmentation, number> = {
  NORMAL: 0,
  ALBINO: 70,
  IRIDESCENT: 100,
};

const qualityTable: Record<SpecimenQuality, number> = {
  NORMAL: 0,
  PERFECT_SPECIMEN: 100,
};

/** Matches java.lang.Math.round(double): floor(value + 0.5). */
const javaRound = (value: number) => Math.floor(value + 0.5);

export function speciesPoints(rarity: FishRarity): number {
  const points = speciesTable[rarity];
  if (points === undefined) throw new RangeError(`Unsupported fish rarity: ${rarity}`);
  return points;
}

export function normalizeFishScore(rawScore: number): number {
  if (!Number.isFinite(rawScore)) throw new TypeError('rawScore must be finite');
  const normalized = (rawScore - RAW_MIN) / (RAW_MAX - RAW_MIN);
  const rounded = javaRound(SCORE_MIN + (SCORE_MAX - SCORE_MIN) * normalized);
  return Math.max(SCORE_MIN, Math.min(SCORE_MAX, rounded));
}

export function fishScoreBreakdown(input: FishScoreInput): FishScoreBreakdown {
  const species = speciesPoints(input.rarity);
  const percentile = 3 * input.finalPercentile;
  const bodyType = bodyTypeTable[input.bodyType];
  const condition = conditionTable[input.condition];
  const pigmentation = pigmentationTable[input.pigmentation];
  const quality = qualityTable[input.quality];
  const rawScore = species + percentile + bodyType + condition + pigmentation + quality;
  const fishScore = normalizeFishScore(rawScore);

  return { species, percentile, bodyType, condition, pigmentation, quality, rawScore, fishScore };
}

export function calculateFishScore(input: FishScoreInput): FishScoreResult {
  const { rawScore, fishScore } = fishScoreBreakdown(input);
  return { rawScore, fishScore };
}

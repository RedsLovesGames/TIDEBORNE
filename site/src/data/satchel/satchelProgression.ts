export type CapacityLevel = {
  name: string;
  multiplier: number;
  xpCost: number;
};

export type SatchelFeature = {
  id: string;
  name: string;
  xpCost: number;
  purpose: string;
  stage: 'organization' | 'retrieval' | 'records' | 'inspection' | 'protection' | 'multiplayer';
  requiresMultiplayerExtras: boolean;
};

export const capacityLevels: CapacityLevel[] = [
  { name: 'Base', multiplier: 1, xpCost: 0 },
  { name: 'Capacity I', multiplier: 1.5, xpCost: 150 },
  { name: 'Capacity II', multiplier: 2, xpCost: 450 },
  { name: 'Capacity III', multiplier: 3, xpCost: 1000 },
];

export const satchelFeatures: SatchelFeature[] = [
  {
    id: 'tackle_organizer',
    name: 'Tackle Organizer',
    xpCost: 100,
    purpose: 'Unlocks specimen-aware sorting and organization for catches already stored in the Satchel.',
    stage: 'organization',
    requiresMultiplayerExtras: false,
  },
  {
    id: 'auto_stow',
    name: 'Auto-Stow',
    xpCost: 200,
    purpose: 'Routes eligible catches into the active Satchel after Tide has selected and logged the catch.',
    stage: 'retrieval',
    requiresMultiplayerExtras: false,
  },
  {
    id: 'record_keeper',
    name: 'Record Keeper',
    xpCost: 250,
    purpose: 'Adds record-oriented Satchel presentation and utilities without changing specimen identity.',
    stage: 'records',
    requiresMultiplayerExtras: false,
  },
  {
    id: 'trait_scanner',
    name: 'Trait Scanner',
    xpCost: 300,
    purpose: 'Surfaces more canonical specimen and trait information for catches in storage.',
    stage: 'inspection',
    requiresMultiplayerExtras: false,
  },
  {
    id: 'trophy_lock',
    name: 'Trophy Lock',
    xpCost: 350,
    purpose: 'Protects valuable catches that match configured trophy and record evidence.',
    stage: 'protection',
    requiresMultiplayerExtras: false,
  },
  {
    id: 'shared_ledger',
    name: 'Shared Ledger',
    xpCost: 400,
    purpose: 'Connects Satchel progression to shared team record and ledger behavior.',
    stage: 'multiplayer',
    requiresMultiplayerExtras: true,
  },
];

export const trophyLockEvidence = [
  'Unusual or mutated specimens',
  'Trophy-size catches',
  'Legendary-size catches',
  'Personal largest records',
  'Personal smallest records',
  'Tide legendary-rarity fish',
] as const;

export const satchelSortKeys = [
  'Alphabetical',
  'Mutation Rarity',
  'Percentile',
  'Rarity',
  'Region',
  'Size',
] as const;

export type GearSlot = 'rod' | 'line' | 'hook' | 'leader' | 'bait';

export type GearModifiers = {
  fishingLuck?: number;
  traitLuck?: number;
  strengthMultiplier?: number;
  tempoMultiplier?: number;
  catchZoneMultiplier?: number;
  minigameSpeedMultiplier?: number;
  catchLossPrevention?: number;
  trophyFightRelief?: number;
  crateWeightMultiplier?: number;
};

export type GearOption = {
  id: string;
  name: string;
  slot: GearSlot;
  origin: 'Minecraft' | 'Tide' | 'Tideborne';
  requires?: string;
  modifiers?: GearModifiers;
  notes?: string[];
  provenance: string;
};

export type GearPreview = Required<GearModifiers> & {
  selected: GearOption[];
  notes: string[];
};

const neutral: GearModifiers = {};

/**
 * Website snapshot of canonical Fishing System 2.0 gear contributions.
 *
 * Values are transcribed from FishingGearRegistry, TideFishingLineModifiers,
 * LeaderTier, TideborneFishingGearModifiers, and FishingGearEffects on the
 * Tideborne 2.1.0 source branch. Native Tide mechanics that Tideborne does not
 * own are intentionally not reverse-simulated here.
 */
export const gearOptions: GearOption[] = [
  {
    id: 'minecraft:fishing_rod',
    name: 'Fishing Rod',
    slot: 'rod',
    origin: 'Minecraft',
    modifiers: neutral,
    provenance: 'FishingGearRegistry.GearProfile.WOOD_ROD',
  },
  {
    id: 'tide:iron_fishing_rod',
    name: 'Iron Fishing Rod',
    slot: 'rod',
    origin: 'Tide',
    modifiers: { catchZoneMultiplier: 1.04, catchLossPrevention: 0.05 },
    provenance: 'FishingGearRegistry.GearProfile.IRON_ROD.rodModifiers()',
  },
  {
    id: 'tide:golden_fishing_rod',
    name: 'Golden Fishing Rod',
    slot: 'rod',
    origin: 'Tide',
    modifiers: { catchZoneMultiplier: 0.97, catchLossPrevention: 0.02 },
    provenance: 'FishingGearRegistry.GearProfile.GOLD_ROD.rodModifiers()',
  },
  {
    id: 'tide:diamond_fishing_rod',
    name: 'Diamond Fishing Rod',
    slot: 'rod',
    origin: 'Tide',
    modifiers: { strengthMultiplier: 0.92, trophyFightRelief: 0.25, catchLossPrevention: 0.10 },
    provenance: 'FishingGearRegistry.GearProfile.DIAMOND_ROD.rodModifiers()',
  },
  {
    id: 'tide:netherite_fishing_rod',
    name: 'Netherite Fishing Rod',
    slot: 'rod',
    origin: 'Tide',
    modifiers: { tempoMultiplier: 0.95, trophyFightRelief: 0.10, catchLossPrevention: 0.15 },
    provenance: 'FishingGearRegistry.GearProfile.NETHERITE_ROD.rodModifiers()',
  },
  {
    id: 'tidebound_compatibility:kujira_bone_fishing_rod',
    name: 'Kujira Bone Fishing Rod',
    slot: 'rod',
    origin: 'Tideborne',
    requires: 'Myths of the Sea',
    modifiers: { strengthMultiplier: 0.88, tempoMultiplier: 1.10, crateWeightMultiplier: 0.70 },
    notes: ['1.35× Kujira target weight when its compatibility path is active.'],
    provenance: 'FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD.rodModifiers()',
  },

  {
    id: 'tide:fishing_line',
    name: 'Fishing Line',
    slot: 'line',
    origin: 'Tide',
    modifiers: neutral,
    provenance: 'FishingGearRegistry.GearProfile.TIDE_BASE_LINE',
  },
  {
    id: 'tide:copper_line',
    name: 'Copper Line',
    slot: 'line',
    origin: 'Tide',
    modifiers: { tempoMultiplier: 0.94, catchZoneMultiplier: 1.02 },
    provenance: 'TideFishingLineModifiers.COPPER',
  },
  {
    id: 'tide:iron_line',
    name: 'Iron Line',
    slot: 'line',
    origin: 'Tide',
    modifiers: { strengthMultiplier: 0.92, catchZoneMultiplier: 1.03 },
    provenance: 'TideFishingLineModifiers.IRON',
  },
  {
    id: 'tide:golden_line',
    name: 'Golden Line',
    slot: 'line',
    origin: 'Tide',
    modifiers: { strengthMultiplier: 1.05, tempoMultiplier: 0.86 },
    provenance: 'TideFishingLineModifiers.GOLDEN',
  },
  {
    id: 'tide:diamond_line',
    name: 'Diamond Line',
    slot: 'line',
    origin: 'Tide',
    modifiers: { strengthMultiplier: 0.82, tempoMultiplier: 1.06 },
    provenance: 'TideFishingLineModifiers.DIAMOND',
  },
  {
    id: 'tidebound_compatibility:tentacle_line',
    name: 'Tentacle Line',
    slot: 'line',
    origin: 'Tideborne',
    requires: 'Myths of the Sea',
    modifiers: { catchZoneMultiplier: 1.32, minigameSpeedMultiplier: 1.16 },
    notes: ['Uses the current default Tideborne balance values; server configuration can change these values.'],
    provenance: 'TideboundConfig.Values defaults + TideborneFishingGearModifiers.tentacleLine()',
  },
  {
    id: 'tidebound_compatibility:swift_line',
    name: 'Abaia Line',
    slot: 'line',
    origin: 'Tideborne',
    requires: 'Myths of the Sea',
    modifiers: { catchZoneMultiplier: 1.16, minigameSpeedMultiplier: 1.08 },
    notes: ['Internal registry ID remains swift_line for compatibility.', 'Server configuration can change these default values.'],
    provenance: 'TideboundConfig.Values defaults + TideborneFishingGearModifiers.swiftLine()',
  },

  {
    id: 'tide:fishing_hook',
    name: 'Fishing Hook',
    slot: 'hook',
    origin: 'Tide',
    modifiers: neutral,
    provenance: 'FishingGearRegistry.GearProfile.TIDE_BASE_HOOK',
  },
  {
    id: 'tide:fiery_hook',
    name: 'Fiery Hook',
    slot: 'hook',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide behavior is not re-simulated by this Tideborne preview.'],
    provenance: 'FishingGearRegistry.GearProfile.FIERY_HOOK',
  },
  {
    id: 'tide:permafrost_hook',
    name: 'Permafrost Hook',
    slot: 'hook',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide behavior is not re-simulated by this Tideborne preview.'],
    provenance: 'FishingGearRegistry.GearProfile.PERMAFROST_HOOK',
  },
  {
    id: 'tide:twilight_hook',
    name: 'Twilight Hook',
    slot: 'hook',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide behavior is not re-simulated by this Tideborne preview.'],
    provenance: 'FishingGearRegistry.GearProfile.TWILIGHT_HOOK',
  },
  {
    id: 'tide:lavaproof_hook',
    name: 'Lavaproof Hook',
    slot: 'hook',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide behavior is not re-simulated by this Tideborne preview.'],
    provenance: 'FishingGearRegistry.GearProfile.LAVAPROOF_HOOK',
  },
  {
    id: 'tide:void_hook',
    name: 'Void Hook',
    slot: 'hook',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide behavior is not re-simulated by this Tideborne preview.'],
    provenance: 'FishingGearRegistry.GearProfile.VOID_HOOK',
  },
  {
    id: 'tidebound_compatibility:seafarers_hook',
    name: "Seafarer's Hook",
    slot: 'hook',
    origin: 'Tideborne',
    requires: 'Myths of the Sea',
    modifiers: neutral,
    notes: ['At ocean night, 1.35× target weight for legendary-tagged fish using the current default config.'],
    provenance: 'TideborneFishingGearModifiers.hookTargets()',
  },
  {
    id: 'tidebound_compatibility:shark_tooth_hook',
    name: 'Shark Tooth Hook',
    slot: 'hook',
    origin: 'Tideborne',
    requires: 'Apex Waters',
    modifiers: neutral,
    notes: ['With Apex active: 2.0× heavy target weight and 0.45× very-small target weight using current defaults.'],
    provenance: 'TideborneFishingGearModifiers.hookTargets()',
  },

  {
    id: 'none:leader',
    name: 'No Leader',
    slot: 'leader',
    origin: 'Tideborne',
    modifiers: neutral,
    provenance: 'Neutral attachment state',
  },
  {
    id: 'tidebound_compatibility:copper_leader',
    name: 'Copper Leader',
    slot: 'leader',
    origin: 'Tideborne',
    requires: 'Apex Waters',
    modifiers: { catchLossPrevention: 0.30, catchZoneMultiplier: 0.98, minigameSpeedMultiplier: 1.01 },
    provenance: 'LeaderTier.COPPER',
  },
  {
    id: 'tidebound_compatibility:steel_leader',
    name: 'Iron Leader',
    slot: 'leader',
    origin: 'Tideborne',
    requires: 'Apex Waters',
    modifiers: { catchLossPrevention: 0.55, catchZoneMultiplier: 0.95, minigameSpeedMultiplier: 1.03 },
    notes: ['Internal registry ID remains steel_leader for compatibility.'],
    provenance: 'LeaderTier.IRON',
  },
  {
    id: 'tidebound_compatibility:gold_leader',
    name: 'Gold Leader',
    slot: 'leader',
    origin: 'Tideborne',
    requires: 'Apex Waters',
    modifiers: { catchLossPrevention: 0.75, catchZoneMultiplier: 0.90, minigameSpeedMultiplier: 1.06 },
    provenance: 'LeaderTier.GOLD',
  },
  {
    id: 'tidebound_compatibility:diamond_leader',
    name: 'Diamond Leader',
    slot: 'leader',
    origin: 'Tideborne',
    requires: 'Apex Waters',
    modifiers: { catchLossPrevention: 0.95, catchZoneMultiplier: 0.82, minigameSpeedMultiplier: 1.10 },
    provenance: 'LeaderTier.DIAMOND',
  },

  {
    id: 'none:bait',
    name: 'No Special Bait',
    slot: 'bait',
    origin: 'Tideborne',
    modifiers: neutral,
    provenance: 'Neutral bait state',
  },
  {
    id: 'tide:bait',
    name: 'Bait',
    slot: 'bait',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide bait luck/lure behavior is not duplicated in this Tideborne-only preview.'],
    provenance: 'FishingGearRegistry.GearProfile.NORMAL_BAIT',
  },
  {
    id: 'tide:lucky_bait',
    name: 'Lucky Bait',
    slot: 'bait',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide bait luck/lure behavior is not duplicated in this Tideborne-only preview.'],
    provenance: 'FishingGearRegistry.GearProfile.LUCKY_BAIT',
  },
  {
    id: 'tide:magnetic_bait',
    name: 'Magnetic Bait',
    slot: 'bait',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Native Tide bait luck/lure behavior is not duplicated in this Tideborne-only preview.'],
    provenance: 'FishingGearRegistry.GearProfile.MAGNETIC_BAIT',
  },
  {
    id: 'tide:incandescent_bait',
    name: 'Incandescent Bait',
    slot: 'bait',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Adds a 1.40× warm-target weight in Tideborne in addition to native Tide bait behavior.'],
    provenance: 'FishingGearRegistry.GearProfile.INCANDESCENT_BAIT.baitTargetModifiers()',
  },
  {
    id: 'tide:abyss_bait',
    name: 'Abyss Bait',
    slot: 'bait',
    origin: 'Tide',
    modifiers: neutral,
    notes: ['Adds a 1.40× deep-target weight in Tideborne in addition to native Tide bait behavior.'],
    provenance: 'FishingGearRegistry.GearProfile.ABYSS_BAIT.baitTargetModifiers()',
  },
  {
    id: 'tidebound_compatibility:leviathan_bait',
    name: 'Leviathan Bait',
    slot: 'bait',
    origin: 'Tideborne',
    requires: 'Myths of the Sea',
    modifiers: { fishingLuck: 4, traitLuck: 1, strengthMultiplier: 1.30, tempoMultiplier: 1.20 },
    notes: ['Restricts the Tideborne catch category to fish when enabled.', 'Adds a 2.00× boss target weight in the canonical Fishing System 2 modifier object.'],
    provenance: 'TideborneFishingGearModifiers.LEVIATHAN_BAIT',
  },
];

const byId = new Map(gearOptions.map((option) => [option.id, option]));

const multiply = (current: number, value: number | undefined) => current * (value ?? 1);
const add = (current: number, value: number | undefined) => current + (value ?? 0);
const clamp = (value: number, minimum: number, maximum: number) => Math.max(minimum, Math.min(maximum, value));

export function optionsForSlot(slot: GearSlot): GearOption[] {
  return gearOptions.filter((option) => option.slot === slot);
}

export function composeGearPreview(ids: string[]): GearPreview {
  const selected = ids.map((id) => byId.get(id)).filter((option): option is GearOption => Boolean(option));

  let fishingLuck = 0;
  let traitLuck = 0;
  let strengthMultiplier = 1;
  let tempoMultiplier = 1;
  let catchZoneMultiplier = 1;
  let minigameSpeedMultiplier = 1;
  let catchLossPrevention = 0;
  let trophyFightRelief = 0;
  let crateWeightMultiplier = 1;
  const notes: string[] = [];

  for (const option of selected) {
    const modifier = option.modifiers ?? neutral;
    fishingLuck = add(fishingLuck, modifier.fishingLuck);
    traitLuck = add(traitLuck, modifier.traitLuck);
    strengthMultiplier = multiply(strengthMultiplier, modifier.strengthMultiplier);
    tempoMultiplier = multiply(tempoMultiplier, modifier.tempoMultiplier);
    catchZoneMultiplier = multiply(catchZoneMultiplier, modifier.catchZoneMultiplier);
    minigameSpeedMultiplier = multiply(minigameSpeedMultiplier, modifier.minigameSpeedMultiplier);
    catchLossPrevention = add(catchLossPrevention, modifier.catchLossPrevention);
    trophyFightRelief = add(trophyFightRelief, modifier.trophyFightRelief);
    crateWeightMultiplier = multiply(crateWeightMultiplier, modifier.crateWeightMultiplier);
    notes.push(...(option.notes ?? []));
  }

  return {
    selected,
    notes: [...new Set(notes)],
    fishingLuck: Math.min(8, fishingLuck),
    traitLuck: Math.min(3, traitLuck),
    strengthMultiplier: clamp(strengthMultiplier, 0.70, 1.35),
    tempoMultiplier: clamp(tempoMultiplier, 0.80, 1.30),
    catchZoneMultiplier: clamp(catchZoneMultiplier, 0.70, 1.40),
    minigameSpeedMultiplier: clamp(minigameSpeedMultiplier, 0.80, 1.25),
    catchLossPrevention: clamp(catchLossPrevention, 0, 0.95),
    trophyFightRelief: clamp(trophyFightRelief, 0, 1),
    crateWeightMultiplier,
  };
}

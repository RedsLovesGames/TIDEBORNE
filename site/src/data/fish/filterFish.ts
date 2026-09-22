export type FishRecord = {
  id: string;
  source?: string | null;
  namespace?: string | null;
  slug?: string | null;
  name?: string | null;
  rarity?: number | null;
  rarityKey?: string | null;
  stars?: number | null;
  group?: string | null;
  habitats?: readonly string[];
  locationKey?: string | null;
  journalCategory?: string | null;
  size?: {
    typicalLow?: number | null;
    typicalHigh?: number | null;
    recordHigh?: number | null;
  };
  render?: string | null;
  variants?: Readonly<Record<string, string>>;
  associatedMods?: readonly string[];
  conditions?: readonly unknown[];
};

export type FishFilters = {
  query?: string;
  sources?: readonly string[];
  rarities?: readonly number[];
  habitats?: readonly string[];
  groups?: readonly string[];
};

const normalize = (value: unknown) => String(value ?? '').trim().toLocaleLowerCase();

const includesAny = (value: unknown, selected?: readonly unknown[]) => {
  if (!selected?.length) return true;
  return selected.some((entry) => value === entry);
};

const includesHabitat = (record: FishRecord, selected?: readonly string[]) => {
  if (!selected?.length) return true;
  const habitats = Array.isArray(record.habitats) ? record.habitats : [];
  return selected.some((selectedHabitat) => habitats.includes(selectedHabitat));
};

const searchableText = (record: FishRecord) => [
  record.name,
  record.id,
  record.slug,
  record.source,
  record.namespace,
  record.group,
  record.locationKey,
  ...(record.habitats ?? []),
  ...(record.associatedMods ?? []),
].map(normalize).filter(Boolean).join(' ');

export function filterFish<T extends FishRecord>(records: readonly T[], filters: FishFilters = {}): T[] {
  const query = normalize(filters.query);

  return records.filter((record) => {
    if (query && !searchableText(record).includes(query)) return false;
    if (!includesAny(record.source, filters.sources)) return false;
    if (!includesAny(record.rarity, filters.rarities)) return false;
    if (!includesHabitat(record, filters.habitats)) return false;
    if (!includesAny(record.group, filters.groups)) return false;
    return true;
  });
}

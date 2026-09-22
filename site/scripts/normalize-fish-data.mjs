const rarityPoints = {
  common: 1,
  uncommon: 2,
  rare: 3,
  very_rare: 4,
  legendary: 5,
};

const namespaceOf = (id) => {
  const value = String(id ?? '');
  const separator = value.indexOf(':');
  return separator >= 0 ? value.slice(0, separator) : 'minecraft';
};

const slugOf = (id) => {
  const value = String(id ?? '');
  const separator = value.indexOf(':');
  const path = separator >= 0 ? value.slice(separator + 1) : value;
  return path
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/[_\s]+/g, '-')
    .replace(/[^a-zA-Z0-9-]/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
    .toLowerCase();
};

const numberOrNull = (value) => {
  if (value === null || value === undefined || value === '') return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
};

const cleanRenderPath = (file) => {
  if (!file) return null;
  const filename = String(file).split(/[\\/]/).pop();
  return filename ? `/fish/renders/${filename}` : null;
};

const normalizeVariants = (entry) => {
  const variants = entry?.variants ?? {};
  const result = {};
  for (const [key, value] of Object.entries(variants)) {
    if (!value || value.status === 'unavailable' || !value.file) continue;
    const path = cleanRenderPath(value.file);
    if (path) result[key] = path;
  }
  return result;
};

const sourceName = (record) => String(record?.mod || record?.sourceMod || namespaceOf(record?.id) || 'Unknown');

const normalizeRecord = (record, manifestEntry) => {
  const namespace = namespaceOf(record.id);
  const variants = normalizeVariants(manifestEntry);
  const rarityKey = typeof record.rarity === 'string' && record.rarity ? record.rarity : null;
  const location = typeof record.location === 'string' && record.location.trim() ? record.location.trim() : null;

  return {
    id: String(record.id),
    source: sourceName(record),
    namespace,
    slug: slugOf(record.id),
    name: String(record.name || record.id),
    rarity: rarityKey ? rarityPoints[rarityKey] ?? numberOrNull(record.stars) : numberOrNull(record.stars),
    rarityKey,
    stars: numberOrNull(record.stars),
    group: typeof record.group === 'string' && record.group ? record.group : null,
    habitats: location ? [location] : [],
    locationKey: typeof record.locationKey === 'string' && record.locationKey ? record.locationKey : null,
    journalCategory: typeof record.journalCategory === 'string' && record.journalCategory ? record.journalCategory : null,
    size: {
      typicalLow: numberOrNull(record.typicalLow),
      typicalHigh: numberOrNull(record.typicalHigh),
      recordHigh: numberOrNull(record.recordHigh),
    },
    render: variants.normal ?? null,
    variants,
    associatedMods: Array.isArray(record.associatedMods) ? [...record.associatedMods] : [],
    conditions: Array.isArray(record.conditions) ? [...record.conditions] : [],
  };
};

export function normalizeCatalog({ first = {}, second = {}, scope = {}, manifest = {}, sourceRevision = null } = {}) {
  const allowedNamespaces = new Set(Array.isArray(scope.mod_ids) ? scope.mod_ids : []);
  if (scope.include_minecraft !== false) allowedNamespaces.add('minecraft');

  const sourceRecords = [
    ...(Array.isArray(first.records) ? first.records : []),
    ...(Array.isArray(second.records) ? second.records : []),
  ];

  const records = sourceRecords
    .filter((record) => {
      if (!record?.id) return false;
      return allowedNamespaces.size === 0 || allowedNamespaces.has(namespaceOf(record.id));
    })
    .map((record) => normalizeRecord(record, manifest?.fish?.[record.id]))
    .sort((a, b) => a.id.localeCompare(b.id));

  return {
    meta: {
      ...(first.meta && typeof first.meta === 'object' ? first.meta : {}),
      sourceRevision,
      schemaVersion: 1,
      recordCount: records.length,
    },
    records,
  };
}

export { cleanRenderPath, namespaceOf, slugOf };

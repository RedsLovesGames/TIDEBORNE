import React, { useMemo, useState } from 'react';
import { RotateCcw, Search } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { filterFish, type FishRecord } from '@/data/fish/filterFish';
import './fish-explorer.css';

type FishExplorerProps = {
  records: readonly FishRecord[];
  basePath: string;
  sourceRevision: string;
};

const legacyRenderRoot = (revision: string) =>
  `https://raw.githubusercontent.com/RedsLovesGames/random-info-pages/${revision}/tideborne/assets/fish/renders`;

const normalizeBase = (basePath: string) => basePath.endsWith('/') ? basePath : `${basePath}/`;

const titleCase = (value: string) => value
  .replaceAll('_', ' ')
  .replace(/\b\w/g, (character) => character.toUpperCase());

const rarityName = (record: FishRecord) => {
  if (record.rarityKey) return titleCase(record.rarityKey);
  if (record.rarity) return `Rarity ${record.rarity}`;
  return 'Unknown rarity';
};

const sizeText = (record: FishRecord) => {
  const low = record.size?.typicalLow;
  const high = record.size?.recordHigh ?? record.size?.typicalHigh;
  if (low == null && high == null) return 'Size unknown';
  if (low == null) return `Up to ${high} cm`;
  if (high == null) return `From ${low} cm`;
  return `${low}–${high} cm`;
};

function FishRender({ record, sourceRevision }: { record: FishRecord; sourceRevision: string }) {
  const [failed, setFailed] = useState(false);
  const filename = record.render?.split('/').pop();
  const src = filename ? `${legacyRenderRoot(sourceRevision)}/${encodeURIComponent(filename)}` : null;

  if (!src || failed) {
    return (
      <div className="tb-fish-render tb-fish-render--missing" aria-label="Validated render unavailable">
        <span aria-hidden="true">◇</span>
        <small>Render unavailable</small>
      </div>
    );
  }

  return (
    <div className="tb-fish-render">
      <img
        src={src}
        alt={`${record.name ?? record.id} source-backed fish render`}
        loading="lazy"
        decoding="async"
        onError={() => setFailed(true)}
      />
    </div>
  );
}

function FishCard({ record, basePath, sourceRevision }: { record: FishRecord; basePath: string; sourceRevision: string }) {
  const href = `${normalizeBase(basePath)}fish/${record.namespace ?? 'unknown'}/${record.slug ?? encodeURIComponent(record.id)}/`;
  const stars = Math.max(0, Math.min(5, Number(record.stars ?? record.rarity ?? 0)));

  return (
    <a className="tb-fish-card" href={href} aria-label={`Open ${record.name ?? record.id} fish details`}>
      <FishRender record={record} sourceRevision={sourceRevision} />
      <div className="tb-fish-card__body">
        <div className="tb-fish-card__eyebrow">
          <Badge variant="secondary">{record.source ?? record.namespace ?? 'Unknown source'}</Badge>
          <span className="tb-fish-stars" aria-label={`${stars} star rarity`}>
            <span aria-hidden="true">{'★'.repeat(stars) || '◇'}</span>
          </span>
        </div>
        <div>
          <h2>{record.name ?? record.id}</h2>
          <code>{record.id}</code>
        </div>
        <dl className="tb-fish-card__facts">
          <div><dt>Rarity</dt><dd>{rarityName(record)}</dd></div>
          <div><dt>Habitat</dt><dd>{record.habitats?.[0] ?? 'Unknown'}</dd></div>
          <div><dt>Size</dt><dd>{sizeText(record)}</dd></div>
        </dl>
      </div>
    </a>
  );
}

const uniqueStrings = (values: Array<string | null | undefined>) =>
  [...new Set(values.filter((value): value is string => Boolean(value)))].sort((a, b) => a.localeCompare(b));

export function FishExplorer({ records, basePath, sourceRevision }: FishExplorerProps) {
  const [query, setQuery] = useState('');
  const [source, setSource] = useState('');
  const [rarity, setRarity] = useState('');
  const [habitat, setHabitat] = useState('');
  const [group, setGroup] = useState('');

  const sources = useMemo(() => uniqueStrings(records.map((record) => record.source)), [records]);
  const habitats = useMemo(() => uniqueStrings(records.flatMap((record) => [...(record.habitats ?? [])])), [records]);
  const groups = useMemo(() => uniqueStrings(records.map((record) => record.group)), [records]);
  const rarities = useMemo(
    () => [...new Set(records.map((record) => record.rarity).filter((value): value is number => typeof value === 'number'))].sort((a, b) => b - a),
    [records],
  );

  const filtered = useMemo(() => filterFish(records, {
    query,
    sources: source ? [source] : undefined,
    rarities: rarity ? [Number(rarity)] : undefined,
    habitats: habitat ? [habitat] : undefined,
    groups: group ? [group] : undefined,
  }), [records, query, source, rarity, habitat, group]);

  const hasFilters = Boolean(query || source || rarity || habitat || group);
  const clearFilters = () => {
    setQuery('');
    setSource('');
    setRarity('');
    setHabitat('');
    setGroup('');
  };

  return (
    <section className="tb-fish-explorer" aria-labelledby="fish-explorer-title">
      <header className="tb-fish-explorer__intro">
        <div>
          <p className="tb-fish-kicker">Field catalog</p>
          <h1 id="fish-explorer-title">Find a Fish</h1>
          <p>Search the current Tideborne catalog by fish, source mod, rarity, habitat, or water type.</p>
        </div>
        <div className="tb-fish-count" aria-live="polite" aria-atomic="true">
          {filtered.length} {filtered.length === 1 ? 'fish' : 'fish'}
        </div>
      </header>

      <div className="tb-fish-search-row">
        <label className="tb-fish-search">
          <span className="sr-only">Search fish</span>
          <Search aria-hidden="true" size={18} />
          <Input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search fish, ID, source, habitat…"
            aria-label="Search fish"
          />
        </label>
        {hasFilters && (
          <Button type="button" variant="ghost" onClick={clearFilters}>
            <RotateCcw aria-hidden="true" size={16} />
            Reset
          </Button>
        )}
      </div>

      <div className="tb-fish-layout">
        <aside className="tb-fish-filters" aria-label="Fish filters">
          <div className="tb-fish-filter-heading">
            <strong>Filters</strong>
            <span>{records.length} catalog entries</span>
          </div>

          <label>
            <span>Source mod</span>
            <select value={source} onChange={(event) => setSource(event.target.value)}>
              <option value="">All sources</option>
              {sources.map((value) => <option key={value} value={value}>{value}</option>)}
            </select>
          </label>

          <label>
            <span>Rarity</span>
            <select value={rarity} onChange={(event) => setRarity(event.target.value)}>
              <option value="">All rarities</option>
              {rarities.map((value) => <option key={value} value={value}>{value} star{value === 1 ? '' : 's'}</option>)}
            </select>
          </label>

          <label>
            <span>Habitat</span>
            <select value={habitat} onChange={(event) => setHabitat(event.target.value)}>
              <option value="">All habitats</option>
              {habitats.map((value) => <option key={value} value={value}>{value}</option>)}
            </select>
          </label>

          <label>
            <span>Water type</span>
            <select value={group} onChange={(event) => setGroup(event.target.value)}>
              <option value="">All types</option>
              {groups.map((value) => <option key={value} value={value}>{titleCase(value)}</option>)}
            </select>
          </label>
        </aside>

        <div className="tb-fish-results">
          {filtered.length > 0 ? (
            <div className="tb-fish-grid">
              {filtered.map((record) => (
                <FishCard key={record.id} record={record} basePath={basePath} sourceRevision={sourceRevision} />
              ))}
            </div>
          ) : (
            <div className="tb-fish-empty" role="status">
              <strong>No fish match those filters.</strong>
              <p>Try clearing a filter or searching a broader name, ID, source, or habitat.</p>
              <Button type="button" variant="secondary" onClick={clearFilters}>Clear filters</Button>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

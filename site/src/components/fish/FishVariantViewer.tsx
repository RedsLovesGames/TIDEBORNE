import { useMemo, useState } from 'react';
import './fish-variant-viewer.css';

type VariantMap = Readonly<Partial<Record<string, string>>>;

type FishVariantViewerProps = {
  fishName: string;
  basePath: string;
  variants: VariantMap;
};

const order = ['normal', 'giant', 'dwarf', 'albino', 'iridescent', 'scarred', 'parasite_ridden'];
const labels: Record<string, string> = {
  normal: 'Normal',
  giant: 'Giant',
  dwarf: 'Dwarf',
  albino: 'Albino',
  iridescent: 'Iridescent',
  scarred: 'Scarred',
  parasite_ridden: 'Parasite-Ridden',
};

const normalizeBase = (basePath: string) => basePath.endsWith('/') ? basePath : `${basePath}/`;
const renderUrl = (path: string, basePath: string) => {
  const normalized = path.replace(/^\/+/, '');
  return normalized ? `${normalizeBase(basePath)}${normalized}` : null;
};

const titleCase = (value: string) => labels[value] ?? value.replaceAll('_', ' ').replace(/\b\w/g, (c) => c.toUpperCase());

export function FishVariantViewer({ fishName, basePath, variants }: FishVariantViewerProps) {
  const available = useMemo(() => {
    const entries = Object.entries(variants).filter((entry): entry is [string, string] => Boolean(entry[1]));
    return entries.sort(([a], [b]) => {
      const ai = order.indexOf(a);
      const bi = order.indexOf(b);
      return (ai === -1 ? 99 : ai) - (bi === -1 ? 99 : bi) || a.localeCompare(b);
    });
  }, [variants]);

  const [active, setActive] = useState(() => available.find(([key]) => key === 'normal')?.[0] ?? available[0]?.[0] ?? '');
  const [failed, setFailed] = useState(false);
  const selected = available.find(([key]) => key === active) ?? available[0];
  const selectedUrl = selected ? renderUrl(selected[1], basePath) : null;
  const activeLabel = selected ? titleCase(selected[0]) : 'Unavailable';

  if (!selected || !selectedUrl) {
    return (
      <div className="tb-variant-viewer tb-variant-viewer--empty" role="status">
        <span aria-hidden="true">◇</span>
        <strong>Validated render unavailable</strong>
      </div>
    );
  }

  return (
    <section className="tb-variant-viewer" aria-label={`${fishName} verified renders`}>
      <div className="tb-variant-viewer__stage">
        {failed ? (
          <div className="tb-variant-viewer__fallback" role="status">
            <span aria-hidden="true">◇</span>
            <strong>Render could not load</strong>
            <small>The fish data remains available below.</small>
          </div>
        ) : (
          <img
            key={`${selected[0]}:${selectedUrl}`}
            src={selectedUrl}
            alt={`${fishName} ${activeLabel} source-backed render`}
            decoding="async"
            onError={() => setFailed(true)}
          />
        )}
      </div>
      <div className="tb-variant-viewer__controls" aria-label="Render variant">
        {available.map(([key]) => (
          <button
            type="button"
            key={key}
            aria-pressed={key === selected[0]}
            onClick={() => {
              setActive(key);
              setFailed(false);
            }}
          >
            {titleCase(key)}
          </button>
        ))}
      </div>
    </section>
  );
}

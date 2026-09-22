import { useMemo, useState } from 'react';
import {
  fishScoreBreakdown,
  type BodyType,
  type Condition,
  type FishRarity,
  type Pigmentation,
  type SpecimenQuality,
} from '@/data/fishscore/fishScore';
import './fishscore-calculator.css';

const clampPercentile = (value: number) => Math.max(0, Math.min(100, Number.isFinite(value) ? value : 0));

const labels = {
  NORMAL: 'Normal',
  GIANT: 'Giant',
  DWARF: 'Dwarf',
  SCARRED: 'Scarred',
  PARASITE_RIDDEN: 'Parasite-Ridden',
  ALBINO: 'Albino',
  IRIDESCENT: 'Iridescent',
  PERFECT_SPECIMEN: 'Perfect Specimen',
} as const;

function Contribution({ label, value }: { label: string; value: number }) {
  return (
    <div className="tb-score-contribution">
      <span>{label}</span>
      <strong>+{value}</strong>
    </div>
  );
}

export function FishScoreCalculator() {
  const [rarity, setRarity] = useState<FishRarity>(1);
  const [finalPercentile, setFinalPercentile] = useState(50);
  const [bodyType, setBodyType] = useState<BodyType>('NORMAL');
  const [condition, setCondition] = useState<Condition>('NORMAL');
  const [pigmentation, setPigmentation] = useState<Pigmentation>('NORMAL');
  const [quality, setQuality] = useState<SpecimenQuality>('NORMAL');

  const breakdown = useMemo(() => fishScoreBreakdown({
    rarity,
    finalPercentile,
    bodyType,
    condition,
    pigmentation,
    quality,
  }), [rarity, finalPercentile, bodyType, condition, pigmentation, quality]);

  const scorePercent = (breakdown.fishScore / 3000) * 100;

  return (
    <section className="tb-score-calculator" aria-labelledby="fishscore-calculator-title">
      <header className="tb-score-calculator__intro">
        <div>
          <p className="tb-fish-kicker">Canonical 2.1.0 tool</p>
          <h1 id="fishscore-calculator-title">FishScore Calculator</h1>
          <p>Build a specimen and see the exact frozen FishScore V2 result and point contribution from every axis.</p>
        </div>
        <span className="tb-score-calculator__range">1–3000</span>
      </header>

      <div className="tb-score-calculator__layout">
        <form className="tb-score-controls" onSubmit={(event) => event.preventDefault()}>
          <div className="tb-score-control">
            <label htmlFor="fishscore-rarity">Species rarity</label>
            <select
              id="fishscore-rarity"
              value={rarity}
              onChange={(event) => setRarity(Number(event.target.value) as FishRarity)}
            >
              <option value={1}>1 star</option>
              <option value={2}>2 stars</option>
              <option value={3}>3 stars</option>
              <option value={4}>4 stars</option>
              <option value={5}>5 stars</option>
            </select>
          </div>

          <div className="tb-score-control tb-score-control--percentile">
            <div className="tb-score-control__heading">
              <label htmlFor="fishscore-percentile">Final percentile</label>
              <output htmlFor="fishscore-percentile">P{finalPercentile}</output>
            </div>
            <input
              id="fishscore-percentile"
              type="number"
              min={0}
              max={100}
              step={0.1}
              value={finalPercentile}
              onChange={(event) => setFinalPercentile(clampPercentile(Number(event.target.value)))}
            />
            <input
              className="tb-score-percentile-slider"
              type="range"
              min={0}
              max={100}
              step={0.1}
              value={finalPercentile}
              aria-label="Final percentile slider"
              onChange={(event) => setFinalPercentile(clampPercentile(Number(event.target.value)))}
            />
          </div>

          <div className="tb-score-control">
            <label htmlFor="fishscore-body">Body type</label>
            <select id="fishscore-body" value={bodyType} onChange={(event) => setBodyType(event.target.value as BodyType)}>
              <option value="NORMAL">Normal</option>
              <option value="GIANT">Giant</option>
              <option value="DWARF">Dwarf</option>
            </select>
          </div>

          <div className="tb-score-control">
            <label htmlFor="fishscore-condition">Condition</label>
            <select id="fishscore-condition" value={condition} onChange={(event) => setCondition(event.target.value as Condition)}>
              <option value="NORMAL">Normal</option>
              <option value="SCARRED">Scarred</option>
              <option value="PARASITE_RIDDEN">Parasite-Ridden</option>
            </select>
          </div>

          <div className="tb-score-control">
            <label htmlFor="fishscore-pigmentation">Pigmentation</label>
            <select
              id="fishscore-pigmentation"
              value={pigmentation}
              onChange={(event) => setPigmentation(event.target.value as Pigmentation)}
            >
              <option value="NORMAL">Normal</option>
              <option value="ALBINO">Albino</option>
              <option value="IRIDESCENT">Iridescent</option>
            </select>
          </div>

          <div className="tb-score-control">
            <label htmlFor="fishscore-quality">Specimen quality</label>
            <select id="fishscore-quality" value={quality} onChange={(event) => setQuality(event.target.value as SpecimenQuality)}>
              <option value="NORMAL">Normal</option>
              <option value="PERFECT_SPECIMEN">Perfect Specimen</option>
            </select>
          </div>
        </form>

        <div className="tb-score-result" aria-live="polite" aria-atomic="true">
          <div className="tb-score-result__hero">
            <span>FishScore</span>
            <strong>{breakdown.fishScore}</strong>
            <small>{breakdown.rawScore} raw</small>
          </div>

          <div className="tb-score-meter" aria-label={`${breakdown.fishScore} out of 3000 FishScore`}>
            <span style={{ width: `${scorePercent}%` }} />
          </div>

          <div className="tb-score-specimen-summary" aria-label="Selected specimen traits">
            <span>{rarity}★ species</span>
            <span>P{finalPercentile}</span>
            <span>{labels[bodyType]}</span>
            <span>{labels[condition]}</span>
            <span>{labels[pigmentation]}</span>
            <span>{labels[quality]}</span>
          </div>

          <div className="tb-score-breakdown">
            <div className="tb-score-breakdown__heading">
              <strong>Raw score breakdown</strong>
              <span>50–925 canonical range</span>
            </div>
            <Contribution label="Species" value={breakdown.species} />
            <Contribution label="Final percentile × 3" value={breakdown.percentile} />
            <Contribution label="Body type" value={breakdown.bodyType} />
            <Contribution label="Condition" value={breakdown.condition} />
            <Contribution label="Pigmentation" value={breakdown.pigmentation} />
            <Contribution label="Specimen quality" value={breakdown.quality} />
          </div>

          <p className="tb-score-formula">
            FishScore = round(1 + 2999 × ((RawScore − 50) / 875)), clamped to 1–3000.
          </p>
        </div>
      </div>
    </section>
  );
}

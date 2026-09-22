import { useMemo, useState } from 'react';
import { composeGearPreview, optionsForSlot, type GearSlot } from '../../data/gear/gearCatalog';
import './gear-builder.css';

const slots: Array<{ id: GearSlot; label: string }> = [
  { id: 'rod', label: 'Rod' },
  { id: 'line', label: 'Line' },
  { id: 'hook', label: 'Hook' },
  { id: 'leader', label: 'Leader' },
  { id: 'bait', label: 'Bait' },
];

const defaults: Record<GearSlot, string> = {
  rod: 'minecraft:fishing_rod',
  line: 'tide:fishing_line',
  hook: 'tide:fishing_hook',
  leader: 'none:leader',
  bait: 'none:bait',
};

const percent = (value: number) => `${Math.round(value * 100)}%`;
const multiplier = (value: number) => `${value.toFixed(2)}×`;
const signed = (value: number) => `${value >= 0 ? '+' : ''}${Number.isInteger(value) ? value : value.toFixed(2)}`;

export function GearBuilder() {
  const [selected, setSelected] = useState<Record<GearSlot, string>>(defaults);
  const preview = useMemo(
    () => composeGearPreview(slots.map((slot) => selected[slot.id])),
    [selected],
  );

  const requirements = [...new Set(preview.selected.map((option) => option.requires).filter(Boolean))] as string[];

  return (
    <section className="tb-gear-builder" aria-labelledby="gear-builder-heading">
      <div className="tb-gear-builder__heading">
        <div>
          <p className="tb-gear-builder__kicker">Fishing System 2 preview</p>
          <h2 id="gear-builder-heading">Build a loadout</h2>
          <p>
            Combine Tideborne's canonical gear contributions before you craft. The preview uses current 2.1.0 defaults and applies the same public caps as the runtime.
          </p>
        </div>
        <div className="tb-gear-builder__scope">
          <strong>Tideborne-owned effects</strong>
          <span>Native Tide enchantment and bait behavior can add effects beyond this preview.</span>
        </div>
      </div>

      <div className="tb-gear-builder__body">
        <div className="tb-gear-builder__slots" aria-label="Loadout slots">
          {slots.map((slot) => {
            const options = optionsForSlot(slot.id);
            const active = options.find((option) => option.id === selected[slot.id]);
            return (
              <label className="tb-gear-slot" key={slot.id}>
                <span className="tb-gear-slot__label">{slot.label}</span>
                <select
                  aria-label={slot.label}
                  value={selected[slot.id]}
                  onChange={(event) => setSelected((current) => ({ ...current, [slot.id]: event.target.value }))}
                >
                  {options.map((option) => (
                    <option key={option.id} value={option.id}>{option.name}</option>
                  ))}
                </select>
                {active && (
                  <span className="tb-gear-slot__meta">
                    <span>{active.origin}</span>
                    <code>{active.id}</code>
                  </span>
                )}
                {active?.requires && <span className="tb-gear-slot__requires">Requires {active.requires}</span>}
              </label>
            );
          })}
        </div>

        <div className="tb-gear-builder__results" aria-live="polite">
          <div className="tb-gear-builder__result-head">
            <div>
              <p className="tb-gear-builder__kicker">Combined preview</p>
              <h3>Canonical modifiers</h3>
            </div>
            <span>{preview.selected.length} slots</span>
          </div>

          <div className="tb-gear-metrics">
            <div><span>Fishing Luck</span><strong data-metric="fishing-luck">{signed(preview.fishingLuck)}</strong></div>
            <div><span>Trait Luck</span><strong>{signed(preview.traitLuck)}</strong></div>
            <div><span>Strength</span><strong data-metric="strength">{multiplier(preview.strengthMultiplier)}</strong></div>
            <div><span>Tempo</span><strong>{multiplier(preview.tempoMultiplier)}</strong></div>
            <div><span>Catch zone</span><strong>{multiplier(preview.catchZoneMultiplier)}</strong></div>
            <div><span>Minigame speed</span><strong>{multiplier(preview.minigameSpeedMultiplier)}</strong></div>
            <div><span>Catch-loss protection</span><strong data-metric="catch-loss">{percent(preview.catchLossPrevention)}</strong></div>
            <div><span>Trophy fight relief</span><strong>{percent(preview.trophyFightRelief)}</strong></div>
            <div><span>Crate weight</span><strong>{multiplier(preview.crateWeightMultiplier)}</strong></div>
          </div>

          {requirements.length > 0 && (
            <div className="tb-gear-builder__requirements" aria-label="Required integrations">
              {requirements.map((requirement) => <span key={requirement}>Requires {requirement}</span>)}
            </div>
          )}

          {preview.notes.length > 0 ? (
            <div className="tb-gear-builder__notes">
              <h4>Conditional and native behavior</h4>
              <ul>
                {preview.notes.map((note) => <li key={note}>{note}</li>)}
              </ul>
            </div>
          ) : (
            <p className="tb-gear-builder__empty-note">This loadout adds no special Tideborne notes beyond the numeric preview.</p>
          )}
        </div>
      </div>
    </section>
  );
}

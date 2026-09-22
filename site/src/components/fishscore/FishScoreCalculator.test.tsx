// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { FishScoreCalculator } from './FishScoreCalculator';

afterEach(cleanup);

describe('FishScoreCalculator', () => {
  it('updates the canonical score and contribution breakdown from accessible controls', () => {
    render(<FishScoreCalculator />);

    expect(screen.getByText('515')).toBeTruthy();

    fireEvent.change(screen.getByLabelText('Species rarity'), { target: { value: '5' } });
    fireEvent.change(screen.getByLabelText('Final percentile'), { target: { value: '100' } });
    fireEvent.change(screen.getByLabelText('Body type'), { target: { value: 'GIANT' } });
    fireEvent.change(screen.getByLabelText('Condition'), { target: { value: 'PARASITE_RIDDEN' } });
    fireEvent.change(screen.getByLabelText('Pigmentation'), { target: { value: 'IRIDESCENT' } });
    fireEvent.change(screen.getByLabelText('Specimen quality'), { target: { value: 'PERFECT_SPECIMEN' } });

    expect(screen.getByText('3000')).toBeTruthy();
    expect(screen.getByText('925 raw')).toBeTruthy();
    expect(screen.getByText('+350')).toBeTruthy();
    expect(screen.getByText('+300')).toBeTruthy();
    expect(screen.getAllByText('+100').length).toBeGreaterThanOrEqual(2);
  });

  it('clamps percentile entry to the canonical 0-100 input scale', () => {
    render(<FishScoreCalculator />);
    const percentile = screen.getByLabelText('Final percentile') as HTMLInputElement;

    fireEvent.change(percentile, { target: { value: '140' } });
    expect(percentile.value).toBe('100');

    fireEvent.change(percentile, { target: { value: '-10' } });
    expect(percentile.value).toBe('0');
  });
});

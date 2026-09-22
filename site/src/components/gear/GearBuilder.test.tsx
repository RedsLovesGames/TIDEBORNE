// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { GearBuilder } from './GearBuilder';

afterEach(cleanup);

describe('GearBuilder', () => {
  it('composes a selected five-slot loadout and exposes compatibility notes', () => {
    render(<GearBuilder />);

    fireEvent.change(screen.getByLabelText('Rod'), { target: { value: 'tide:diamond_fishing_rod' } });
    fireEvent.change(screen.getByLabelText('Line'), { target: { value: 'tide:diamond_line' } });
    fireEvent.change(screen.getByLabelText('Leader'), { target: { value: 'tidebound_compatibility:diamond_leader' } });
    fireEvent.change(screen.getByLabelText('Bait'), { target: { value: 'tidebound_compatibility:leviathan_bait' } });

    expect(screen.getByText('+4', { selector: '[data-metric="fishing-luck"]' })).toBeTruthy();
    expect(screen.getByText('0.98×', { selector: '[data-metric="strength"]' })).toBeTruthy();
    expect(screen.getByText('95%', { selector: '[data-metric="catch-loss"]' })).toBeTruthy();
    expect(screen.getAllByText('Requires Myths of the Sea').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Requires Apex Waters').length).toBeGreaterThan(0);
  });

  it('keeps native Tide-only behavior explicitly outside the preview', () => {
    render(<GearBuilder />);
    fireEvent.change(screen.getByLabelText('Bait'), { target: { value: 'tide:lucky_bait' } });
    expect(screen.getByText(/Native Tide bait luck\/lure behavior is not duplicated/i)).toBeTruthy();
  });
});

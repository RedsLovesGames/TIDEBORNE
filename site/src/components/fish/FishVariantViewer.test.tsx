// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { FishVariantViewer } from './FishVariantViewer';

afterEach(cleanup);

describe('FishVariantViewer', () => {
  it('switches between verified render variants and exposes the active state', () => {
    render(
      <FishVariantViewer
        fishName="Dragon Fish"
        sourceRevision="rev"
        variants={{
          normal: '/fish/renders/dragon.png',
          giant: '/fish/renders/dragon_giant.png',
          albino: '/fish/renders/dragon_albino.png',
        }}
      />,
    );

    expect(screen.getByRole('button', { name: 'Normal' }).getAttribute('aria-pressed')).toBe('true');
    fireEvent.click(screen.getByRole('button', { name: 'Giant' }));
    expect(screen.getByRole('button', { name: 'Giant' }).getAttribute('aria-pressed')).toBe('true');
    expect(screen.getByRole('img').getAttribute('alt')).toContain('Giant');
  });
});

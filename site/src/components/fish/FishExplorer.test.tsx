// @vitest-environment jsdom
import React from 'react';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { FishExplorer } from './FishExplorer';

const fish = [
  {
    id: 'tide:dragon_fish',
    source: 'Tide',
    namespace: 'tide',
    slug: 'dragon-fish',
    name: 'Dragon Fish',
    rarity: 5,
    rarityKey: 'legendary',
    stars: 5,
    group: 'saltwater',
    habitats: ['Deep Ocean'],
    locationKey: 'deep_ocean',
    journalCategory: null,
    size: { typicalLow: 85, typicalHigh: 140, recordHigh: 180 },
    render: '/fish/renders/tide__dragon_fish.png',
    variants: { normal: '/fish/renders/tide__dragon_fish.png' },
    associatedMods: [],
    conditions: [],
  },
  {
    id: 'minecraft:cod',
    source: 'Minecraft',
    namespace: 'minecraft',
    slug: 'cod',
    name: 'Cod',
    rarity: 1,
    rarityKey: 'common',
    stars: 1,
    group: 'saltwater',
    habitats: ['Ocean'],
    locationKey: 'ocean',
    journalCategory: null,
    size: { typicalLow: null, typicalHigh: null, recordHigh: null },
    render: null,
    variants: {},
    associatedMods: [],
    conditions: [],
  },
] as const;

afterEach(cleanup);

describe('FishExplorer', () => {
  it('searches and filters fish while keeping an accessible result count', () => {
    render(<FishExplorer records={fish} basePath="/TIDEBORNE/" sourceRevision="test-revision" />);

    expect(screen.getByText('2 fish')).toBeTruthy();
    expect(screen.getByRole('link', { name: /Dragon Fish/i })).toBeTruthy();

    fireEvent.change(screen.getByRole('searchbox', { name: /Search fish/i }), {
      target: { value: 'dragon' },
    });
    expect(screen.getByText('1 fish')).toBeTruthy();
    expect(screen.queryByRole('link', { name: /Cod/i })).toBeNull();

    fireEvent.change(screen.getByLabelText(/Source mod/i), { target: { value: 'Minecraft' } });
    expect(screen.getByText('0 fish')).toBeTruthy();
    expect(screen.getByText(/No fish match/i)).toBeTruthy();
  });
});

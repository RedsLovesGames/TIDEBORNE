// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { Button } from './button';
import { Checkbox } from './checkbox';


afterEach(cleanup);
describe('Tideborne UI primitives', () => {
  it('preserves native button semantics and disabled behavior', () => {
    render(<Button disabled>Apply filters</Button>);
    const button = screen.getByRole('button', { name: 'Apply filters' });
    expect(button.hasAttribute('disabled')).toBe(true);
    expect(button.getAttribute('data-variant')).toBe('default');
  });

  it('exposes checkbox state accessibly', () => {
    render(<Checkbox aria-label="Tide fish" />);
    const checkbox = screen.getByRole('checkbox', { name: 'Tide fish' });
    expect(checkbox.getAttribute('data-state')).toBe('unchecked');
    fireEvent.click(checkbox);
    expect(checkbox.getAttribute('data-state')).toBe('checked');
  });
});

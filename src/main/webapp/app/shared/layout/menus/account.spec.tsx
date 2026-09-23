import { describe, expect, it } from 'vitest';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { render } from '@testing-library/react';

import { AccountMenuItemsAuthenticated } from './account';

describe('AccountMenuItemsAuthenticated', () => {
  it('renders links to settings, password and logout', () => {
    const { container } = render(
      <MemoryRouter>
        <AccountMenuItemsAuthenticated />
      </MemoryRouter>,
    );

    expect(container.querySelector('[data-cy="settings"]')).not.toBeNull();
    expect(container.querySelector('[data-cy="passwordItem"]')).not.toBeNull();
    expect(container.querySelector('[data-cy="logout"]')).not.toBeNull();
  });
});

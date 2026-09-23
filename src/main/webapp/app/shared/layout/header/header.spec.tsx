import { describe, expect, it } from 'vitest';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { render } from '@testing-library/react';
import { Provider } from 'react-redux';

import initStore from 'app/config/store';

import Header from './header';

describe('Header', () => {
  let mountedWrapper;
  const devProps = {
    isAuthenticated: true,
    isAdmin: true,
    currentLocale: 'en',
    ribbonEnv: 'dev',
    isInProduction: false,
    isOpenAPIEnabled: true,
  };
  const prodProps = {
    ...devProps,
    ribbonEnv: 'prod',
    isInProduction: true,
    isOpenAPIEnabled: false,
  };
  const userProps = {
    ...prodProps,
    isAdmin: false,
  };
  const guestProps = {
    ...prodProps,
    isAdmin: false,
    isAuthenticated: false,
  };

  const wrapper = (props = devProps) => {
    if (!mountedWrapper) {
      const store = initStore();
      const { container } = render(
        <Provider store={store}>
          <MemoryRouter>
            <Header {...props} />
          </MemoryRouter>
        </Provider>,
      );
      mountedWrapper = container.innerHTML;
    }
    return mountedWrapper;
  };

  beforeEach(() => {
    mountedWrapper = undefined;
  });

  // El ribbon de desarrollo se eliminó del header (ver comentario en header.tsx); el AccountMenu
  // (dropdown) se movió al sidebar (mockup) y en el header solo sobrevive como los mismos
  // MenuItem de Settings/Password/Logout dentro de un bloque 'd-md-none' (visibilidad por CSS,
  // no por JSX), para que sigan alcanzables en móvil donde el sidebar está oculto.
  it('Renders a Header component in dev profile with LoadingBar, Navbar, Nav and account menu items.', () => {
    const html = wrapper();

    // Find Navbar component
    expect(html).toContain('navbar');
    // Find AdminMenu component
    expect(html).toContain('admin-menu');
    // Find EntitiesMenu component
    expect(html).toContain('entity-menu');
    // Find the account items (Settings/Password/Logout)
    expect(html).toContain('data-cy="logout"');
  });

  it('Renders a Header component in prod profile with LoadingBar, Navbar, Nav.', () => {
    const html = wrapper(prodProps);

    // Find Navbar component
    expect(html).toContain('navbar');
    // Find AdminMenu component
    expect(html).toContain('admin-menu');
    // Find EntitiesMenu component
    expect(html).toContain('entity-menu');
    // Find the account items (Settings/Password/Logout)
    expect(html).toContain('data-cy="logout"');
  });

  it('Renders a Header component in prod profile with logged in User', () => {
    const html = wrapper(userProps);

    // Find Navbar component
    expect(html).toContain('navbar');
    // Not find AdminMenu component
    expect(html).not.toContain('admin-menu');
    // Find EntitiesMenu component
    expect(html).toContain('entity-menu');
    // Find the account items (Settings/Password/Logout)
    expect(html).toContain('data-cy="logout"');
  });

  it('Renders a Header component in prod profile with no logged in User', () => {
    const html = wrapper(guestProps);

    // Find Navbar component
    expect(html).toContain('navbar');
    // Not find AdminMenu component
    expect(html).not.toContain('admin-menu');
    // Not find EntitiesMenu component
    expect(html).not.toContain('entity-menu');
    // A guest has no session, so no account items either
    expect(html).not.toContain('data-cy="logout"');
    // Instead, shows the login/register entry points
    expect(html).toContain('data-cy="login"');
    expect(html).toContain('data-cy="register"');
  });
});

import './header.scss';

import React, { useEffect, useRef } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Storage } from 'react-jhipster';

import LoadingBar, { LoadingBarRef } from 'react-top-loading-bar';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { setLocale } from 'app/shared/reducers/locale';
import LinkButton from 'app/shared/components/link-button';
import { AccountMenuItemsAuthenticated, AdminMenu, EntitiesMenu, LocaleMenu } from '../menus';

import { Brand, Notifications, SearchBar, UserInfo } from './header-components';

const ROLE_LABELS: Record<string, string> = {
  ROLE_ADMIN: 'Administrador',
  ROLE_INSTRUCTOR: 'Instructor',
  ROLE_APPRENTICE: 'Aprendiz',
};

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  ribbonEnv: string;
  isInProduction: boolean;
  isOpenAPIEnabled: boolean;
  currentLocale: string;
}

const Header = (props: IHeaderProps) => {
  const dispatch = useAppDispatch();

  const handleLocaleChange = langKey => {
    Storage.session.set('locale', langKey);
    dispatch(setLocale(langKey));
  };

  const loadingBarRef = useRef<LoadingBarRef>(null);
  const loadingCount = useAppSelector(state => state.loadingBar.count);

  const account = useAppSelector(state => state.authentication.account);
  const profile = useAppSelector(state => state.settings.profile);

  const fullName = [profile?.firstName, profile?.firstLastName].filter(Boolean).join(' ') || account?.login || '';
  const roleKey = account?.authorities?.find(authority => authority !== 'ROLE_USER');
  const roleLabel = roleKey ? (ROLE_LABELS[roleKey] ?? '') : '';

  useEffect(() => {
    if (loadingCount > 0) {
      loadingBarRef.current?.continuousStart();
    } else {
      loadingBarRef.current?.complete();
    }
  }, [loadingCount]);

  // Se eliminó la cinta de desarrollo (ribbon dev)

  /* jhipster-needle-add-element-to-menu - JHipster will add new menu items here */

  return (
    <div id="app-header">
      <LoadingBar ref={loadingBarRef} className="loading-bar" color="#009cd8" />
      <Navbar data-cy="navbar" data-bs-theme="light" expand="md" className="navbar pad" collapseOnSelect>
        <Navbar.Toggle aria-controls="header-tabs" aria-label="Menu" />
        {/* CONTROL DE LOGO RESPONSIVO */}
        {props.isAuthenticated ? (
          /* Si está autenticado: Solo se muestra en móviles (se oculta de 'md' en adelante) */
          <div className="d-block d-md-none">
            <Brand isAuthenticated={props.isAuthenticated} />
          </div>
        ) : (
          /* Si NO está autenticado (Landing): Se muestra siempre en cualquier pantalla */
          <Brand isAuthenticated={props.isAuthenticated} />
        )}
        <Navbar.Collapse id="header-tabs">
          <Nav className="ms-auto gap-3">
            {!props.isAuthenticated && (
              <LinkButton to="/login" variant="primary" translationKey="global.menu.account.login" data-cy="login">
                Sign in
              </LinkButton>
            )}
            {!props.isAuthenticated && (
              <LinkButton to="/account/register" translationKey="global.menu.account.register" data-cy="register">
                Register
              </LinkButton>
            )}
            {/* Admin/Entities ya se muestran en el sidebar desde 'md' en adelante; en el header solo hacen falta en móvil. */}
            {props.isAuthenticated && props.isAdmin && (
              <div className="d-md-none">
                <AdminMenu showOpenAPI={props.isOpenAPIEnabled} />
              </div>
            )}
            {props.isAuthenticated && (
              <div className="d-md-none">
                <EntitiesMenu />
              </div>
            )}
            {/* Settings/Password/Logout ya viven en el footer del sidebar (>= md); en móvil, donde el
                sidebar está oculto (app.tsx: 'd-none d-md-block'), son la única forma de llegar a ellos. */}
            {props.isAuthenticated && (
              <div className="d-md-none">
                <AccountMenuItemsAuthenticated />
              </div>
            )}
            {props.isAuthenticated && <SearchBar icon="search" placeholder="Buscar" isAuthenticated={props.isAuthenticated} />}
            {props.isAuthenticated && <Notifications isAuthenticated={props.isAuthenticated} icon="bell" />}
            {props.isAuthenticated && <UserInfo fullName={fullName} role={roleLabel} />}
            <LocaleMenu currentLocale={props.currentLocale} onClick={handleLocaleChange} />
          </Nav>
        </Navbar.Collapse>
      </Navbar>
    </div>
  );
};

export default Header;

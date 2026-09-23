import './header.scss';

import React, { useEffect, useRef, useState } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Storage } from 'react-jhipster';

import LoadingBar, { LoadingBarRef } from 'react-top-loading-bar';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { setLocale } from 'app/shared/reducers/locale';
import LinkButton from 'app/shared/components/link-button';
import { AccountMenu, AdminMenu, EntitiesMenu, LocaleMenu, AccountMenuItemsAuthenticated } from '../menus';
import MenuItem from '../menus/menu-item';
import { Brand } from './header-components';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
  const [navExpanded, setNavExpanded] = useState(false);

  const handleLocaleChange = langKey => {
    Storage.session.set('locale', langKey);
    dispatch(setLocale(langKey));
  };

  const loadingBarRef = useRef<LoadingBarRef>(null);
  const loadingCount = useAppSelector(state => state.loadingBar.count);
  const account = useAppSelector(state => state.authentication.account);
  // Construye el nombre: si tiene nombre/apellido lo usa, sino usa su username (login)
  const fullName = account?.firstName ? `${account.firstName} ${account.lastName || ''}`.trim() : account?.login;

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
      <Navbar
        expanded={navExpanded}
        onToggle={expanded => setNavExpanded(expanded)}
        data-cy="navbar"
        data-bs-theme={props.isAuthenticated ? 'dark' : 'light'}
        expand="md"
        className={`navbar pad ${props.isAuthenticated ? 'navbar-authenticated' : 'navbar-public'}`}
        collapseOnSelect
      >
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
          <Nav className="ms-auto d-flex flex-row flex-wrap align-items-center justify-content-center header-right-panel gap-3 mt-3 mt-md-0">
            {/* VISTA PARA NO LOGUEADOS */}
            {!props.isAuthenticated && (
              <>
                <LinkButton to="/login" variant="primary" translationKey="global.menu.account.login">
                  Sign in
                </LinkButton>
                <LinkButton to="/account/register" translationKey="global.menu.account.register">
                  Register
                </LinkButton>
              </>
            )}

            {/* VISTA PARA LOGUEADOS */}
            {props.isAuthenticated && (
              <>
                {/* Campana */}
                <div className="header-icon-btn">
                  <FontAwesomeIcon icon="bell" />
                  <span className="notification-dot"></span>
                </div>

                {/* Ayuda */}
                <div className="header-icon-btn">
                  <FontAwesomeIcon icon="question-circle" />
                </div>

                {/* Dropdown del Admin (Solo visible para Admin, mostrando solo icono) */}
                {props.isAdmin && (
                  <div className="admin-icon-menu">
                    <AdminMenu showOpenAPI={props.isOpenAPIEnabled} showName={false} />
                  </div>
                )}

                {/* Separador */}
                <div className="header-separator"></div>

                {/* Perfil de Usuario */}
                <div className="user-profile-info d-flex align-items-center">
                  <span className="user-name me-3">{fullName}</span>
                  <div className="user-avatar-icon">
                    <FontAwesomeIcon icon="user" />
                  </div>
                </div>
              </>
            )}

            <LocaleMenu currentLocale={props.currentLocale} onClick={handleLocaleChange} />
          </Nav>
          {props.isAuthenticated && (
            <Nav className="d-md-none flex-column w-100 mobile-nav-links mt-4 pt-3 border-top" onClick={() => setNavExpanded(false)}>
              <MenuItem icon="tachometer-alt" to="/dashboard">
                Dashboard
              </MenuItem>
              <EntitiesMenu />
              <div className="mt-3 pt-2 border-top">
                <AccountMenuItemsAuthenticated />
              </div>
            </Nav>
          )}
        </Navbar.Collapse>
      </Navbar>
    </div>
  );
};

export default Header;

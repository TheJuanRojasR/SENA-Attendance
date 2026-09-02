import './header.scss';

import React, { useEffect, useRef } from 'react';
import { Nav, Navbar } from 'react-bootstrap';
import { Storage } from 'react-jhipster';

import LoadingBar, { LoadingBarRef } from 'react-top-loading-bar';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { setLocale } from 'app/shared/reducers/locale';
import LinkButton from 'app/shared/components/link-button';
import { AccountMenu, AdminMenu, EntitiesMenu, LocaleMenu } from '../menus';

import { Brand } from './header-components';

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
            {props.isAuthenticated && <AccountMenu isAuthenticated={props.isAuthenticated} />}
            <LocaleMenu currentLocale={props.currentLocale} onClick={handleLocaleChange} />
          </Nav>
        </Navbar.Collapse>
      </Navbar>
    </div>
  );
};

export default Header;

import 'react-toastify/dist/ReactToastify.css';
import './app.scss';
import 'app/config/dayjs';

import React, { useEffect } from 'react';
import { Card } from 'react-bootstrap';
import { BrowserRouter } from 'react-router';

import { ToastContainer } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import AppRoutes from 'app/routes';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import ErrorBoundary from 'app/shared/error/error-boundary';
import { Authority } from 'app/shared/jhipster/constants';
import Footer from 'app/shared/layout/footer/footer';
import Header from 'app/shared/layout/header/header';
import Sidebar from 'app/shared/layout/sidebar/sidebar';
import { getProfile } from 'app/shared/reducers/application-profile';
import { getSession } from 'app/shared/reducers/authentication';
import { getAccountProfile } from 'app/modules/account/settings/settings.reducer';

const baseHref = document.querySelector('base')!.getAttribute('href')!.replace(/\/$/, '');

export const App = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getProfile());
  }, []);

  const currentLocale = useAppSelector(state => state.locale.currentLocale);
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.ADMIN]));
  const isInstructor = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.INSTRUCTOR]));
  const isAprentice = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.APPRENTICE]));
  const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
  const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);
  const isOpenAPIEnabled = useAppSelector(state => state.applicationProfile.isOpenAPIEnabled);

  useEffect(() => {
    // GET /api/account/profile exige sesión: si se pide sin estar autenticado, el 401
    // no queda excluido por axios-interceptor.ts (solo excluye rutas que terminan en
    // exactamente 'api/account') y dispara un cierre de sesión indebido.
    if (isAuthenticated) {
      dispatch(getAccountProfile());
    }
  }, [isAuthenticated]);

  // const paddingTop = '60px';
  return (
    <BrowserRouter basename={baseHref}>
      <div className="app-container d-flex">
        <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />
        <div className="app-sidebar-wrapper d-none d-md-block">
          <Sidebar isAuthenticated={isAuthenticated} isAdmin={isAdmin} isOpenAPIEnabled={isOpenAPIEnabled} />
        </div>
        <div className="app-main-content flex-grow-1">
          <ErrorBoundary>
            <Header
              isAuthenticated={isAuthenticated}
              isAdmin={isAdmin}
              currentLocale={currentLocale}
              ribbonEnv={ribbonEnv}
              isInProduction={isInProduction}
              isOpenAPIEnabled={isOpenAPIEnabled}
            />
          </ErrorBoundary>
          <div className="container-fluid view-container flex-grow-1" id="app-view-container">
            {!isAuthenticated ? (
              <Card className="jh-card">
                <ErrorBoundary>
                  <AppRoutes isAuthenticated={isAuthenticated} isAdmin={isAdmin} isInstructor={isInstructor} isAprentice={isAprentice} />
                </ErrorBoundary>
              </Card>
            ) : (
              <ErrorBoundary>
                <AppRoutes isAuthenticated={isAuthenticated} isAdmin={isAdmin} isInstructor={isInstructor} isAprentice={isAprentice} />
              </ErrorBoundary>
            )}
            <Footer isAuthenticated={isAuthenticated} />
          </div>
        </div>
      </div>
    </BrowserRouter>
  );
};

export default App;

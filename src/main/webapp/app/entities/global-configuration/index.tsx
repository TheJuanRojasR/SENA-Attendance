import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import GlobalConfiguration from './global-configuration';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

const GlobalConfigurationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
          <GlobalConfiguration />
        </PrivateRoute>
      }
    />
  </ErrorBoundaryRoutes>
);

export default GlobalConfigurationRoutes;

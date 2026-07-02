import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import GlobalConfiguration from './global-configuration';
import GlobalConfigurationDeleteDialog from './global-configuration-delete-dialog';
import GlobalConfigurationDetail from './global-configuration-detail';
import GlobalConfigurationUpdate from './global-configuration-update';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

const GlobalConfigurationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <GlobalConfiguration />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <GlobalConfigurationUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <GlobalConfigurationDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <GlobalConfigurationUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <GlobalConfigurationDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default GlobalConfigurationRoutes;

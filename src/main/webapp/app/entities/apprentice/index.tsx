import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Apprentice from './apprentice';
import ApprenticeDetail from './apprentice-detail';
import ApprenticeUnlinkDialog from './apprentice-unlink-dialog';
import ApprenticeUpdate from './apprentice-update';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

// UC008: la lectura (lista y detalle) es de ROLE_ADMIN o ROLE_INSTRUCTOR; vincular y desvincular
// son solo ROLE_ADMIN. No existe edición (no hay PUT/PATCH genérico para Apprentice).
const ApprenticeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR]}>
          <Apprentice />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
          <ApprenticeUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR]}>
            <ApprenticeDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="unlink"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
            <ApprenticeUnlinkDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ApprenticeRoutes;

import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Program from './program';
import ProgramDeleteDialog from './program-delete-dialog';
import ProgramDetail from './program-detail';
import ProgramUpdate from './program-update';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

const ProgramRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <Program />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <ProgramUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <ProgramDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <ProgramUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <ProgramDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ProgramRoutes;

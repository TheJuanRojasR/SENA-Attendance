import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

import JustificationDetails from './justification-details';
import JustificationDetailsDetail from './justification-details-detail';

const allowedRoles = [Authority.ADMIN, Authority.INSTRUCTOR];

const JustificationDetailsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <JustificationDetails />
        </PrivateRoute>
      }
    />
    <Route
      path=":id"
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <JustificationDetailsDetail />
        </PrivateRoute>
      }
    />
  </ErrorBoundaryRoutes>
);

export default JustificationDetailsRoutes;

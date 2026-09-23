import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

import Alert from './alert';
import AlertDetail from './alert-detail';
import AlertStudentHistory from './alert-student-history';

const allowedRoles = [Authority.ADMIN, Authority.INSTRUCTOR];

const AlertRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <Alert />
        </PrivateRoute>
      }
    />
    <Route
      path="students/:studentId"
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <AlertStudentHistory />
        </PrivateRoute>
      }
    />
    <Route
      path=":id"
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <AlertDetail />
        </PrivateRoute>
      }
    />
  </ErrorBoundaryRoutes>
);

export default AlertRoutes;

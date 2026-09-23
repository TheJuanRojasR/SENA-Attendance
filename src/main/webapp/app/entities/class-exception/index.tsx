import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

import ClassException from './class-exception';
import ClassExceptionDeleteDialog from './class-exception-delete-dialog';
import ClassExceptionDetail from './class-exception-detail';
import ClassExceptionUpdate from './class-exception-update';

// UC009-A4/UC015: ADMIN o el instructor asignado a la materia; el backend acota la lectura y
// la escritura del instructor a sus propias materias.
const allowedRoles = [Authority.ADMIN, Authority.INSTRUCTOR];

const ClassExceptionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <ClassException />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={allowedRoles}>
          <ClassExceptionUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={allowedRoles}>
            <ClassExceptionDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={allowedRoles}>
            <ClassExceptionUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={allowedRoles}>
            <ClassExceptionDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassExceptionRoutes;

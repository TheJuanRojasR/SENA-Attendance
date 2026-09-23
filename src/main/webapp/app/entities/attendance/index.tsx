import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

import Attendance from './attendance';
import AttendanceDetail from './attendance-detail';
import AttendanceSession from './attendance-session';
import AttendanceUpdate from './attendance-update';

// UC009/UC011: la consulta (historial y detalle) es de ADMIN, INSTRUCTOR o APPRENTICE (cada
// uno acotado por el backend a lo suyo). Tomar sesión y la edición A2 son solo del instructor;
// no hay alta ni borrado genérico (POST/PUT/DELETE por id responden 405).
const AttendanceRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR, Authority.APPRENTICE]}>
          <Attendance />
        </PrivateRoute>
      }
    />
    <Route
      path="session"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.INSTRUCTOR]}>
          <AttendanceSession />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR, Authority.APPRENTICE]}>
            <AttendanceDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.INSTRUCTOR]}>
            <AttendanceUpdate />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AttendanceRoutes;

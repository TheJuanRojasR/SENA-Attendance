import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ClassSchedule from './class-schedule';
import ClassScheduleDeleteDialog from './class-schedule-delete-dialog';
import ClassScheduleDetail from './class-schedule-detail';
import ClassScheduleUpdate from './class-schedule-update';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

// UC015: los GET son de ADMIN o INSTRUCTOR (el instructor solo ve los horarios de sus
// materias); las escrituras (crear, editar, eliminar) quedan solo para ADMIN.
const readRoles = [Authority.ADMIN, Authority.INSTRUCTOR];
const writeRoles = [Authority.ADMIN];

const ClassScheduleRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={readRoles}>
          <ClassSchedule />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={writeRoles}>
          <ClassScheduleUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={readRoles}>
            <ClassScheduleDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={writeRoles}>
            <ClassScheduleUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={writeRoles}>
            <ClassScheduleDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassScheduleRoutes;

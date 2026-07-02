import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ClassSchedule from './class-schedule';
import ClassScheduleDeleteDialog from './class-schedule-delete-dialog';
import ClassScheduleDetail from './class-schedule-detail';
import ClassScheduleUpdate from './class-schedule-update';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

const ClassScheduleRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <ClassSchedule />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <ClassScheduleUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <ClassScheduleDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <ClassScheduleUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <ClassScheduleDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassScheduleRoutes;

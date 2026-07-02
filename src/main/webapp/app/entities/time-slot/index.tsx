import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TimeSlot from './time-slot';
import TimeSlotDeleteDialog from './time-slot-delete-dialog';
import TimeSlotDetail from './time-slot-detail';
import TimeSlotUpdate from './time-slot-update';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

const TimeSlotRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <TimeSlot />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <TimeSlotUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <TimeSlotDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <TimeSlotUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <TimeSlotDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TimeSlotRoutes;

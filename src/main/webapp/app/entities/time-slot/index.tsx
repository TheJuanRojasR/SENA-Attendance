import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TimeSlot from './time-slot';
import TimeSlotDeleteDialog from './time-slot-delete-dialog';
import TimeSlotDetail from './time-slot-detail';
import TimeSlotUpdate from './time-slot-update';

const TimeSlotRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TimeSlot />} />
    <Route path="new" element={<TimeSlotUpdate />} />
    <Route path=":id">
      <Route index element={<TimeSlotDetail />} />
      <Route path="edit" element={<TimeSlotUpdate />} />
      <Route path="delete" element={<TimeSlotDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TimeSlotRoutes;

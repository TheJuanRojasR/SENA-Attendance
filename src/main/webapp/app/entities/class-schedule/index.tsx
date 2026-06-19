import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ClassSchedule from './class-schedule';
import ClassScheduleDeleteDialog from './class-schedule-delete-dialog';
import ClassScheduleDetail from './class-schedule-detail';
import ClassScheduleUpdate from './class-schedule-update';

const ClassScheduleRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ClassSchedule />} />
    <Route path="new" element={<ClassScheduleUpdate />} />
    <Route path=":id">
      <Route index element={<ClassScheduleDetail />} />
      <Route path="edit" element={<ClassScheduleUpdate />} />
      <Route path="delete" element={<ClassScheduleDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassScheduleRoutes;

import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Grade from './grade';
import GradeDeleteDialog from './grade-delete-dialog';
import GradeDetail from './grade-detail';
import GradeUpdate from './grade-update';

const GradeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Grade />} />
    <Route path="new" element={<GradeUpdate />} />
    <Route path=":id">
      <Route index element={<GradeDetail />} />
      <Route path="edit" element={<GradeUpdate />} />
      <Route path="delete" element={<GradeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default GradeRoutes;

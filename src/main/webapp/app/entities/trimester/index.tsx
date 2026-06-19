import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Trimester from './trimester';
import TrimesterDeleteDialog from './trimester-delete-dialog';
import TrimesterDetail from './trimester-detail';
import TrimesterUpdate from './trimester-update';

const TrimesterRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Trimester />} />
    <Route path="new" element={<TrimesterUpdate />} />
    <Route path=":id">
      <Route index element={<TrimesterDetail />} />
      <Route path="edit" element={<TrimesterUpdate />} />
      <Route path="delete" element={<TrimesterDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TrimesterRoutes;

import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Apprentice from './apprentice';
import ApprenticeDeleteDialog from './apprentice-delete-dialog';
import ApprenticeDetail from './apprentice-detail';
import ApprenticeUpdate from './apprentice-update';

const ApprenticeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Apprentice />} />
    <Route path="new" element={<ApprenticeUpdate />} />
    <Route path=":id">
      <Route index element={<ApprenticeDetail />} />
      <Route path="edit" element={<ApprenticeUpdate />} />
      <Route path="delete" element={<ApprenticeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ApprenticeRoutes;

import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Justification from './justification';
import JustificationDeleteDialog from './justification-delete-dialog';
import JustificationDetail from './justification-detail';
import JustificationUpdate from './justification-update';

const JustificationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Justification />} />
    <Route path="new" element={<JustificationUpdate />} />
    <Route path=":id">
      <Route index element={<JustificationDetail />} />
      <Route path="edit" element={<JustificationUpdate />} />
      <Route path="delete" element={<JustificationDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default JustificationRoutes;

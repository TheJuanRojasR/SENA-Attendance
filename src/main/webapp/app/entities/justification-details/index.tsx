import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import JustificationDetails from './justification-details';
import JustificationDetailsDeleteDialog from './justification-details-delete-dialog';
import JustificationDetailsDetail from './justification-details-detail';
import JustificationDetailsUpdate from './justification-details-update';

const JustificationDetailsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<JustificationDetails />} />
    <Route path="new" element={<JustificationDetailsUpdate />} />
    <Route path=":id">
      <Route index element={<JustificationDetailsDetail />} />
      <Route path="edit" element={<JustificationDetailsUpdate />} />
      <Route path="delete" element={<JustificationDetailsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default JustificationDetailsRoutes;

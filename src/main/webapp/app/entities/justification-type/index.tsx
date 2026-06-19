import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import JustificationType from './justification-type';
import JustificationTypeDeleteDialog from './justification-type-delete-dialog';
import JustificationTypeDetail from './justification-type-detail';
import JustificationTypeUpdate from './justification-type-update';

const JustificationTypeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<JustificationType />} />
    <Route path="new" element={<JustificationTypeUpdate />} />
    <Route path=":id">
      <Route index element={<JustificationTypeDetail />} />
      <Route path="edit" element={<JustificationTypeUpdate />} />
      <Route path="delete" element={<JustificationTypeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default JustificationTypeRoutes;

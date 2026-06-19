import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Modality from './modality';
import ModalityDeleteDialog from './modality-delete-dialog';
import ModalityDetail from './modality-detail';
import ModalityUpdate from './modality-update';

const ModalityRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Modality />} />
    <Route path="new" element={<ModalityUpdate />} />
    <Route path=":id">
      <Route index element={<ModalityDetail />} />
      <Route path="edit" element={<ModalityUpdate />} />
      <Route path="delete" element={<ModalityDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ModalityRoutes;

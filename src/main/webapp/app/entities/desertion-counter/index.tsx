import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DesertionCounter from './desertion-counter';
import DesertionCounterDeleteDialog from './desertion-counter-delete-dialog';
import DesertionCounterDetail from './desertion-counter-detail';
import DesertionCounterUpdate from './desertion-counter-update';

const DesertionCounterRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DesertionCounter />} />
    <Route path="new" element={<DesertionCounterUpdate />} />
    <Route path=":id">
      <Route index element={<DesertionCounterDetail />} />
      <Route path="edit" element={<DesertionCounterUpdate />} />
      <Route path="delete" element={<DesertionCounterDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DesertionCounterRoutes;

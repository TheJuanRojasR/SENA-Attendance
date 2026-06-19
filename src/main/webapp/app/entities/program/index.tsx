import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Program from './program';
import ProgramDeleteDialog from './program-delete-dialog';
import ProgramDetail from './program-detail';
import ProgramUpdate from './program-update';

const ProgramRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Program />} />
    <Route path="new" element={<ProgramUpdate />} />
    <Route path=":id">
      <Route index element={<ProgramDetail />} />
      <Route path="edit" element={<ProgramUpdate />} />
      <Route path="delete" element={<ProgramDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ProgramRoutes;

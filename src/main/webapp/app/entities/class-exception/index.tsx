import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import ClassException from './class-exception';
import ClassExceptionDeleteDialog from './class-exception-delete-dialog';
import ClassExceptionDetail from './class-exception-detail';
import ClassExceptionUpdate from './class-exception-update';

const ClassExceptionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ClassException />} />
    <Route path="new" element={<ClassExceptionUpdate />} />
    <Route path=":id">
      <Route index element={<ClassExceptionDetail />} />
      <Route path="edit" element={<ClassExceptionUpdate />} />
      <Route path="delete" element={<ClassExceptionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassExceptionRoutes;

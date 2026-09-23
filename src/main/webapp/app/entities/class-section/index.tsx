import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PrivateRoute from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

import ClassSection from './class-section';
import ClassSectionDeleteDialog from './class-section-delete-dialog';
import ClassSectionDetail from './class-section-detail';
import ClassSectionMine from './class-section-mine';
import ClassSectionUpdate from './class-section-update';

const ClassSectionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<ClassSection />} />
    <Route path="new" element={<ClassSectionUpdate />} />
    <Route
      path="mine"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR]}>
          <ClassSectionMine />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route index element={<ClassSectionDetail />} />
      <Route path="edit" element={<ClassSectionUpdate />} />
      <Route path="delete" element={<ClassSectionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassSectionRoutes;

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

// UC015: el listado y el detalle genéricos de materias (fuera de /mine) quedan solo para
// ADMIN; el instructor consulta las suyas por /class-section/mine (UC017).
const ClassSectionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
          <ClassSection />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
          <ClassSectionUpdate />
        </PrivateRoute>
      }
    />
    <Route
      path="mine"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR]}>
          <ClassSectionMine />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
            <ClassSectionDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
            <ClassSectionUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
            <ClassSectionDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ClassSectionRoutes;

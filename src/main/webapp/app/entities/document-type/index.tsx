import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DocumentType from './document-type';
import DocumentTypeDeleteDialog from './document-type-delete-dialog';
import DocumentTypeDetail from './document-type-detail';
import DocumentTypeUpdate from './document-type-update';
import { Authority } from 'app/shared/jhipster/constants';
import PrivateRoute from 'app/shared/auth/private-route';

const DocumentTypeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <DocumentType />
        </PrivateRoute>
      }
    />
    <Route
      path="new"
      element={
        <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
          <DocumentTypeUpdate />
        </PrivateRoute>
      }
    />
    <Route path=":id">
      <Route
        index
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <DocumentTypeDetail />
          </PrivateRoute>
        }
      />
      <Route
        path="edit"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <DocumentTypeUpdate />
          </PrivateRoute>
        }
      />
      <Route
        path="delete"
        element={
          <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.COORDINATOR]}>
            <DocumentTypeDeleteDialog />
          </PrivateRoute>
        }
      />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DocumentTypeRoutes;

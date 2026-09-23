import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PrivateRoute from 'app/shared/auth/private-route';

import Notification from './notification';

// UC018: cualquier usuario autenticado consulta su propia bandeja; el backend la acota al
// usuario de la sesión, así que no hay gating por rol aquí, solo por sesión activa.
const NotificationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route
      index
      element={
        <PrivateRoute>
          <Notification />
        </PrivateRoute>
      }
    />
  </ErrorBoundaryRoutes>
);

export default NotificationRoutes;

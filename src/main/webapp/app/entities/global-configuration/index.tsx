import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import GlobalConfiguration from './global-configuration';
import GlobalConfigurationDeleteDialog from './global-configuration-delete-dialog';
import GlobalConfigurationDetail from './global-configuration-detail';
import GlobalConfigurationUpdate from './global-configuration-update';

const GlobalConfigurationRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<GlobalConfiguration />} />
    <Route path="new" element={<GlobalConfigurationUpdate />} />
    <Route path=":id">
      <Route index element={<GlobalConfigurationDetail />} />
      <Route path="edit" element={<GlobalConfigurationUpdate />} />
      <Route path="delete" element={<GlobalConfigurationDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default GlobalConfigurationRoutes;

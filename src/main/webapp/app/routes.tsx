import React, { Suspense } from 'react';
import { Route } from 'react-router';

import EntitiesRoutes from 'app/entities/routes';
import Activate from 'app/modules/account/activate/activate';
import PasswordResetFinish from 'app/modules/account/password-reset/finish/password-reset-finish';
import PasswordResetInit from 'app/modules/account/password-reset/init/password-reset-init';
import Register from 'app/modules/account/register/register';
import Home from 'app/modules/home/home';
import Login from 'app/modules/login/login';
import Logout from 'app/modules/login/logout';
import Dashboard from 'app/modules/dashboard/dashboard';
import PrivateRoute from 'app/shared/auth/private-route';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import PageNotFound from 'app/shared/error/page-not-found';
import { Authority } from 'app/shared/jhipster/constants';

const loading = <div>loading ...</div>;

const Account = React.lazy(() => import(/* webpackChunkName: "account" */ 'app/modules/account'));

const Admin = React.lazy(() => import(/* webpackChunkName: "administration" */ 'app/modules/administration'));

export interface IDashboardProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isInstructor: boolean;
  isCoordinator: boolean;
  isAprentice: boolean;
}

const AppRoutes = (props: IDashboardProps) => {
  return (
    <div className="view-routes">
      <Suspense fallback={loading}>
        <ErrorBoundaryRoutes>
          <Route index element={<Home />} />
          <Route path="login" element={<Login />} />
          <Route path="logout" element={<Logout />} />
          <Route path="account">
            <Route
              path="*"
              element={
                <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.USER]}>
                  <Account />
                </PrivateRoute>
              }
            />
            <Route path="register" element={<Register />} />
            <Route path="activate" element={<Activate />} />
            <Route path="reset">
              <Route path="request" element={<PasswordResetInit />} />
              <Route path="finish" element={<PasswordResetFinish />} />
            </Route>
          </Route>
          <Route
            path="admin/*"
            element={
              <PrivateRoute hasAnyAuthorities={[Authority.ADMIN]}>
                <Admin />
              </PrivateRoute>
            }
          />
          <Route
            path="*"
            element={
              <PrivateRoute hasAnyAuthorities={[Authority.USER]}>
                <EntitiesRoutes />
              </PrivateRoute>
            }
          />
          {/* Ruta del Admin Dashboard Protegida */}
          <Route
            path="/dashboard"
            element={
              <PrivateRoute hasAnyAuthorities={[Authority.ADMIN, Authority.INSTRUCTOR, Authority.COORDINATOR, Authority.APPRENTICE]}>
                <Dashboard
                  isAuthenticated={props.isAuthenticated}
                  isAdmin={props.isAdmin}
                  isCoordinator={props.isCoordinator}
                  isInstructor={props.isInstructor}
                  isAprentice={props.isAprentice}
                />
              </PrivateRoute>
            }
          />
          <Route path="*" element={<PageNotFound />} />
        </ErrorBoundaryRoutes>
      </Suspense>
    </div>
  );
};

export default AppRoutes;

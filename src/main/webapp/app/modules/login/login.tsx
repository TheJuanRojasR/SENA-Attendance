import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { Navigate, useLocation, useNavigate } from 'react-router';

import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { login } from 'app/shared/reducers/authentication';

import LoginModal from './login-modal';

export const Login = () => {
  const dispatch = useAppDispatch();
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const loginError = useAppSelector(state => state.authentication.loginError);
  const errorMessage = useAppSelector(state => state.authentication.errorMessage);
  const redirectMessage = useAppSelector(state => state.authentication.redirectMessage);
  const showModalLogin = useAppSelector(state => state.authentication.showModalLogin);
  const [showModal, setShowModal] = useState(showModalLogin);
  const navigate = useNavigate();
  const pageLocation = useLocation();

  useEffect(() => {
    setShowModal(true);
    if (redirectMessage) {
      toast.info(translate(redirectMessage));
    }
  }, []);

  const handleLogin = (documentTypeId, documentNumber, password, rememberMe) =>
    dispatch(login(documentTypeId, documentNumber, password, rememberMe));

  const handleClose = () => {
    setShowModal(false);
    navigate('/');
  };

  const { from } = pageLocation.state || { from: { pathname: '/dashboard', search: pageLocation.search } };
  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }
  return (
    <LoginModal
      showModal={showModal}
      handleLogin={handleLogin}
      handleClose={handleClose}
      loginError={loginError}
      errorMessage={errorMessage}
    />
  );
};

export default Login;

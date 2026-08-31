import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';

import { useAppSelector } from 'app/config/store';

import RegisterModal from './register-modal';

const Register = () => {
  const showModalRegister = useAppSelector(state => state.register.showModalRegister);
  const [showModal, setShowModal] = useState(showModalRegister);
  const navigate = useNavigate();

  useEffect(() => {
    setShowModal(true);
  }, []);

  const handleClose = () => {
    setShowModal(false);
    navigate('/');
  };

  return <RegisterModal showModal={showModal} handleClose={handleClose} />;
};

export default Register;

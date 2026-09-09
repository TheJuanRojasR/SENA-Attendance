import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';

import PasswordResetInitModal from 'app/modules/account/password-reset/init/password-reset-init-modal';

const PasswordResetInit = () => {
  const [showModal, setShowModal] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    setShowModal(true);
  }, []);

  const handleClose = () => {
    setShowModal(false);
    navigate('/');
  };

  return <PasswordResetInitModal showModal={showModal} handleClose={handleClose} />;
};

export default PasswordResetInit;

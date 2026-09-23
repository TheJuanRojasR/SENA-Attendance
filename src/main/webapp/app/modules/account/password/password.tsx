import React, { useEffect, useState } from 'react';
import { Alert, Button, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { useNavigate } from 'react-router';

import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';
import { getSession, logout } from 'app/shared/reducers/authentication';

import { reset, savePassword } from './password.reducer';

export const PasswordPage = () => {
  const [password, setPassword] = useState('');
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    dispatch(reset());
    dispatch(getSession());
    return () => {
      dispatch(reset());
    };
  }, []);

  const handleValidSubmit = ({ currentPassword, newPassword }: Record<string, any>) => {
    dispatch(savePassword({ currentPassword, newPassword }));
  };

  const updatePassword = event => setPassword(event.target.value);

  const account = useAppSelector(state => state.authentication.account);
  // UC002-A1/UC003-A2: se captura antes de guardar porque el cambio exitoso limpia el flag en
  // el servidor; después de guardar ya no hay forma de distinguir si este cambio era el obligatorio.
  const isMandatoryChange = account.mustChangePassword;
  const successMessage = useAppSelector(state => state.password.successMessage);
  const updateFailure = useAppSelector(state => state.password.updateFailure);

  // El error específico (E4/E5/E6) lo muestra el middleware global a partir de la respuesta del
  // backend; aquí solo se reacciona al éxito (A1 cierra sesión, A2 refresca y redirige).
  useEffect(() => {
    if (successMessage) {
      toast.success(translate(successMessage));
      if (isMandatoryChange) {
        dispatch(getSession()).then(() => navigate('/', { replace: true }));
      } else {
        dispatch(logout());
      }
    }
    dispatch(reset());
  }, [successMessage, updateFailure]);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="password-title">
            <Translate contentKey="password.title" interpolate={{ username: account.login }}>
              Password for {account.login}
            </Translate>
          </h2>
          {isMandatoryChange && (
            <Alert variant="warning">
              Debes cambiar tu contraseña antes de continuar. Una vez la cambies, seguirás con tu sesión activa.
            </Alert>
          )}
          <ValidatedForm id="password-form" onSubmit={handleValidSubmit}>
            <ValidatedField
              name="currentPassword"
              label={translate('global.form.currentpassword.label')}
              placeholder={translate('global.form.currentpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.newpassword.required') },
              }}
              data-cy="currentPassword"
            />
            <ValidatedField
              name="newPassword"
              label={translate('global.form.newpassword.label')}
              placeholder={translate('global.form.newpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.newpassword.required') },
                minLength: { value: 8, message: translate('global.messages.validate.newpassword.minlength') },
                maxLength: { value: 20, message: translate('global.messages.validate.newpassword.maxlength') },
                pattern: {
                  value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/,
                  message: 'La contraseña debe incluir mayúscula, minúscula, número y carácter especial',
                },
              }}
              onChange={updatePassword}
              data-cy="newPassword"
            />
            <PasswordStrengthBar password={password} />
            <ValidatedField
              name="confirmPassword"
              label={translate('global.form.confirmpassword.label')}
              placeholder={translate('global.form.confirmpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.confirmpassword.required') },
                minLength: { value: 8, message: translate('global.messages.validate.newpassword.minlength') },
                maxLength: { value: 20, message: translate('global.messages.validate.newpassword.maxlength') },
                validate: v => v === password || translate('global.messages.error.dontmatch'),
              }}
              data-cy="confirmPassword"
            />
            <Button variant="success" type="submit" data-cy="submit">
              <Translate contentKey="password.form.button">Save</Translate>
            </Button>
          </ValidatedForm>
        </Col>
      </Row>
    </div>
  );
};

export default PasswordPage;

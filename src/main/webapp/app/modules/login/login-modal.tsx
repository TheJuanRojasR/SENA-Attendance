import React from 'react';
import { Alert, Button, Col, Form, Modal, ModalBody, ModalFooter, ModalHeader, Row } from 'react-bootstrap';
import { Translate, ValidatedField, translate } from 'react-jhipster';
import { Link } from 'react-router';

import { type FieldError, type FieldValues, useForm } from 'react-hook-form';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (username: string, password: string, rememberMe: boolean) => void;
  handleClose: () => void;
}

const LoginModal = (props: ILoginModalProps) => {
  const login = ({ username, password, rememberMe }: FieldValues) => {
    props.handleLogin(username, password, rememberMe);
  };

  const {
    handleSubmit,
    register,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched' });

  const { loginError, handleClose } = props;

  const handleLoginSubmit = e => {
    handleSubmit(login)(e);
  };

  return (
    <Modal show={props.showModal} onHide={handleClose} backdrop="static" id="login-page" autoFocus={false}>
      <Form onSubmit={handleLoginSubmit}>
        <ModalHeader id="login-title" data-cy="loginTitle" closeButton>
          <span style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
            <Translate contentKey="login.title">Sign in</Translate>
          </span>
        </ModalHeader>
        <ModalBody>
          <Row>
            <Col md="12">
              {loginError && (
                <Alert variant="danger" data-cy="loginError">
                  <Translate contentKey="login.messages.error.authentication">
                    <strong>Failed to sign in!</strong> Please check your credentials and try again.
                  </Translate>
                </Alert>
              )}
            </Col>
            <Col md="12">
              <ValidatedField
                name="username"
                label={translate('global.form.username.label')}
                placeholder={translate('global.form.username.placeholder')}
                required
                autoFocus
                data-cy="username"
                validate={{ required: 'Username cannot be empty!' }}
                register={register}
                error={errors.username as FieldError}
                isTouched={touchedFields.username}
              />
              <ValidatedField
                name="password"
                type="password"
                label={translate('login.form.password')}
                placeholder={translate('login.form.password.placeholder')}
                required
                data-cy="password"
                validate={{ required: 'Password cannot be empty!' }}
                register={register}
                error={errors.password as FieldError}
                isTouched={touchedFields.password}
              />
              {/* <ValidatedField
                name="rememberMe"
                type="checkbox"
                check
                label={translate('login.form.rememberme')}
                value={true}
                register={register}
              /> */}
            </Col>
          </Row>
          <div className="mt-1 text-center" style={{ color: '#6c757d', fontSize: '0.85rem' }}>
            <Link to="/account/reset/request" data-cy="forgetYourPasswordSelector" className="text-primary">
              <Translate contentKey="login.password.forgot" />
            </Link>
          </div>
          <div className="mt-2 text-center" style={{ color: '#6c757d', fontSize: '0.85rem' }}>
            <Translate contentKey="global.messages.info.register.noaccount" />{' '}
            <Link to="/account/register" className="text-primary">
              <Translate contentKey="global.messages.info.register.link" />
            </Link>
          </div>
        </ModalBody>
        <ModalFooter>
          <Button variant="secondary" onClick={handleClose} tabIndex={1}>
            <Translate contentKey="entity.action.cancel">Cancel</Translate>
          </Button>{' '}
          <Button variant="primary" type="submit" data-cy="submit">
            <Translate contentKey="login.form.button">Sign in</Translate>
          </Button>
        </ModalFooter>
      </Form>
    </Modal>
  );
};

export default LoginModal;

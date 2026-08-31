import React, { useEffect } from 'react';
import { Alert, Button, Col, Form, Modal, ModalBody, ModalFooter, ModalHeader, Row } from 'react-bootstrap';
import { Translate, ValidatedField, translate } from 'react-jhipster';
import { Link } from 'react-router';

import { type FieldError, type FieldValues, useForm } from 'react-hook-form';
import { getEntities as getDocumentTypes } from 'app/entities/document-type/document-type.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (documentTypeId: string, documentNumber: string, password: string) => void;
  handleClose: () => void;
}

const LoginModal = (props: ILoginModalProps) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getDocumentTypes({}));
  }, []);

  const documentTypes = useAppSelector(state => state.documentType.entities);

  const login = ({ documentTypeId, documentNumber, password }: FieldValues) => {
    props.handleLogin(documentTypeId, documentNumber, password);
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
                name="documentTypeId"
                label={translate('global.form.documentType.label')}
                type="select"
                required
                autoFocus
                data-cy="documentTypeId"
                validate={{ required: 'Selecciona un tipo de documento!' }}
                register={register}
                error={errors.documentTypeId as FieldError}
                isTouched={touchedFields.documentTypeId}
              >
                <option value="" key="0">
                  Selecciona una opcion
                </option>
                {documentTypes
                  ? documentTypes.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                name="documentNumber"
                label={translate('global.form.documentNumber.label')}
                placeholder={translate('global.form.documentNumber.placeholder')}
                required
                data-cy="documentNumber"
                validate={{ required: 'El numero de documento no puede estar vacio!' }}
                register={register}
                error={errors.documentNumber as FieldError}
                isTouched={touchedFields.documentNumber}
              />
              <ValidatedField
                name="password"
                type="password"
                label={translate('login.form.password')}
                placeholder={translate('login.form.password.placeholder')}
                required
                data-cy="password"
                validate={{ required: 'La contraseña no puede estar vacia!' }}
                register={register}
                error={errors.password as FieldError}
                isTouched={touchedFields.password}
              />
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

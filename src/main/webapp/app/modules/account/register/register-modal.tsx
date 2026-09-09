import React, { useEffect, useState } from 'react';
import { Alert, Button, Form, Modal, ModalBody, ModalFooter, ModalHeader, Row } from 'react-bootstrap';
import { Translate, ValidatedField, isEmail, translate } from 'react-jhipster';
import { Link } from 'react-router';

import { type FieldError, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

import './register-modal.scss';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDocumentTypes } from 'app/entities/document-type/document-type.reducer';
import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';

import { handleRegister, reset } from './register.reducer';

export interface IRegisterProps {
  showModal?: boolean;
  handleClose?: () => void;
}

export const RegisterModal = (props: IRegisterProps) => {
  const [password, setPassword] = useState('');
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getDocumentTypes({}));
    return () => {
      dispatch(reset());
    };
  }, []);

  const currentLocale = useAppSelector(state => state.locale.currentLocale);
  const documentTypes = useAppSelector(state => state.documentType.entities);

  const handleValidSubmit = ({
    firstName,
    middleName,
    firstLastName,
    secondLastName,
    documentNumber,
    phoneNumber,
    documentTypeId,
    email,
    firstPassword,
  }: Record<string, any>) => {
    dispatch(
      handleRegister({
        firstName,
        middleName,
        firstLastName,
        secondLastName,
        documentNumber,
        phoneNumber,
        documentTypeId,
        email,
        password: firstPassword,
        langKey: currentLocale,
      }),
    );
  };

  const {
    handleSubmit,
    register,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched' });

  const updatePassword = event => setPassword(event.target.value);

  const successMessage = useAppSelector(state => state.register.successMessage);

  useEffect(() => {
    if (successMessage) {
      toast.success(translate(successMessage));
    }
  }, [successMessage]);

  return (
    <Modal show={props.showModal} onHide={props.handleClose} id={'register-page'} autoFocus={false}>
      <Row className="justify-content-center">
        <Form id="register-form" onSubmit={handleSubmit(handleValidSubmit)}>
          <ModalHeader id="register-title" data-cy="registerTitle" closeButton>
            <h1>
              <Translate contentKey="register.title">Registration</Translate>
            </h1>
          </ModalHeader>
          <ModalBody>
            <Row>
              <ValidatedField
                name="firstName"
                label={translate('global.form.firstName.label')}
                placeholder={translate('global.form.firstName.placeholder')}
                required
                data-cy="firstName"
                validate={{ required: 'Ingrese su primer nombre' }}
                register={register}
                error={errors.firstName as FieldError}
                isTouched={touchedFields.firstName}
                className="test"
              />
              <ValidatedField
                name="middleName"
                label={translate('global.form.middleName.label')}
                placeholder={translate('global.form.middleName.placeholder')}
                data-cy="middleName"
                register={register}
                error={errors.middleName as FieldError}
                isTouched={touchedFields.middleName}
                className="test"
              />
            </Row>
            <Row>
              <ValidatedField
                name="firstLastName"
                label={translate('global.form.firstLastName.label')}
                placeholder={translate('global.form.firstLastName.placeholder')}
                required
                data-cy="firstLastName"
                validate={{ required: 'Ingrese su primer apellido' }}
                register={register}
                error={errors.firstLastName as FieldError}
                isTouched={touchedFields.firstLastName}
                className="test"
              />
              <ValidatedField
                name="secondLastName"
                label={translate('global.form.secondLastName.label')}
                placeholder={translate('global.form.secondLastName.placeholder')}
                data-cy="secondLastName"
                register={register}
                error={errors.secondLastName as FieldError}
                isTouched={touchedFields.secondLastName}
                className="test"
              />
            </Row>
            <ValidatedField
              id="documentTypeId"
              name="documentTypeId"
              data-cy="documentTypeId"
              label="Tipo de Documento"
              type="select"
              required
              validate={{ required: 'Selecciona un tipo de documento' }}
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
              label="Numero de Documento"
              placeholder="Numero de Documento"
              type="text"
              required
              data-cy="documentNumber"
              validate={{ required: 'Ingrese su numero de documento' }}
              register={register}
              error={errors.documentNumber as FieldError}
              isTouched={touchedFields.documentNumber}
            />
            <ValidatedField
              name="phoneNumber"
              label={translate('global.form.phoneNumber.label')}
              placeholder={translate('global.form.phoneNumber.placeholder')}
              required
              data-cy="phoneNumber"
              validate={{ required: 'Ingrese su numero de telefono' }}
              register={register}
              error={errors.phoneNumber as FieldError}
              isTouched={touchedFields.phoneNumber}
            />
            <ValidatedField
              name="email"
              label={translate('global.form.email.label')}
              placeholder={translate('global.form.email.placeholder')}
              type="email"
              validate={{
                required: { value: true, message: translate('global.messages.validate.email.required') },
                minLength: { value: 5, message: translate('global.messages.validate.email.minlength') },
                maxLength: { value: 254, message: translate('global.messages.validate.email.maxlength') },
                validate: v => isEmail(v) || translate('global.messages.validate.email.invalid'),
              }}
              data-cy="email"
              register={register}
              error={errors.email as FieldError}
              isTouched={touchedFields.email}
            />
            <ValidatedField
              name="firstPassword"
              label={translate('global.form.newpassword.label')}
              placeholder={translate('global.form.newpassword.placeholder')}
              type="password"
              onChange={updatePassword}
              validate={{
                required: { value: true, message: translate('global.messages.validate.newpassword.required') },
                minLength: { value: 4, message: translate('global.messages.validate.newpassword.minlength') },
                maxLength: { value: 50, message: translate('global.messages.validate.newpassword.maxlength') },
              }}
              data-cy="firstPassword"
              register={register}
              error={errors.firstPassword as FieldError}
              isTouched={touchedFields.firstPassword}
            />
            <PasswordStrengthBar password={password} />
            <ValidatedField
              name="secondPassword"
              label={translate('global.form.confirmpassword.label')}
              placeholder={translate('global.form.confirmpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.confirmpassword.required') },
                minLength: { value: 4, message: translate('global.messages.validate.confirmpassword.minlength') },
                maxLength: { value: 50, message: translate('global.messages.validate.confirmpassword.maxlength') },
                validate: v => v === password || translate('global.messages.error.dontmatch'),
              }}
              data-cy="secondPassword"
              register={register}
              error={errors.secondPassword as FieldError}
              isTouched={touchedFields.secondPassword}
            />
          </ModalBody>
          <ModalFooter>
            <Button id="register-submit" color="primary" type="submit" data-cy="submit">
              <Translate contentKey="register.form.button">Register</Translate>
            </Button>
            {/* <p>&nbsp;</p> */}
            <Alert variant="success">
              <span>
                <Translate contentKey="global.messages.info.authenticated.prefix">If you want to</Translate>{' '}
              </span>
              <Link to="/login" className="alert-link">
                <Translate contentKey="global.messages.info.authenticated.link">sign in</Translate>
              </Link>
              <span>
                <Translate contentKey="global.messages.info.authenticated.suffix">
                  , you can try the default accounts:
                  <br />- Administrator (login=&quot;admin&quot; and password=&quot;admin&quot;)
                  <br />- User (login=&quot;user&quot; and password=&quot;user&quot;).
                </Translate>
              </span>
            </Alert>
          </ModalFooter>
        </Form>
      </Row>
    </Modal>
  );
};

export default RegisterModal;

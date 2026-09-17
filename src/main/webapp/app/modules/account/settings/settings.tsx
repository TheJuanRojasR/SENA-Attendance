import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Form, FormLabel, Row } from 'react-bootstrap';
import { Translate, ValidatedField, isEmail, translate } from 'react-jhipster';

import { type FieldError, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';

import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';

import { getAccountProfile, reset, saveAccountSettings } from './settings.reducer';

export const SettingsPage = () => {
  const [password, setPassword] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useAppDispatch();
  const profile = useAppSelector(state => state.settings.profile);
  const successMessage = useAppSelector(state => state.settings.successMessage);
  const updateFailure = useAppSelector(state => state.settings.updateFailure);
  const errorMessage = useAppSelector(state => state.settings.errorMessage);

  const {
    handleSubmit,
    register,
    reset: resetForm,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched', defaultValues: profile });

  useEffect(() => {
    dispatch(getSession());
    dispatch(getAccountProfile());
    return () => {
      dispatch(reset());
    };
  }, []);

  useEffect(() => {
    if (profile) {
      resetForm({
        ...profile,
        email: profile.user?.email,
        documentType: profile.documentType?.name,
      });
    }
  }, [profile, resetForm]);

  useEffect(() => {
    if (successMessage) {
      toast.success(translate(successMessage));
      setIsEditing(false);
    }
  }, [successMessage]);

  useEffect(() => {
    if (updateFailure && errorMessage) {
      toast.error(translate(errorMessage));
    }
  }, [updateFailure, errorMessage]);

  const handleEditClick = () => setIsEditing(true);
  const handleCancelClick = () => setIsEditing(false);
  const updatePassword = event => setPassword(event.target.value);

  const handleValidSubmit = values => {
    // documentType/documentNumber are read-only display fields; the document is immutable and must never be sent.
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { documentType, documentNumber, currentPassword, newPassword, ...rest } = values;

    dispatch(
      saveAccountSettings({
        ...rest,
        ...(currentPassword && newPassword ? { currentPassword, newPassword } : {}),
      }),
    );
  };

  return (
    <div>
      <Card className="top-border-card">
        <Row className="justify-content-center">
          <Col md="8">
            <h2 id="settings-title">Mi Perfil</h2>
            <p> Administra tu información personal y configuración de seguridad </p>
            <Form id="settings-form" onSubmit={handleSubmit(handleValidSubmit)}>
              <Row>
                <FormLabel className="formTitles"> Informacion Personal </FormLabel>
                <ValidatedField
                  name="firstName"
                  label={translate('global.form.firstName.label')}
                  required
                  disabled={!isEditing}
                  data-cy="firstName"
                  validate={{
                    required: { value: true, message: translate('settings.messages.validate.firstname.required') },
                    minLength: { value: 1, message: translate('settings.messages.validate.firstname.minlength') },
                    maxLength: { value: 50, message: translate('settings.messages.validate.firstname.maxlength') },
                  }}
                  className="formInput"
                  register={register}
                  error={errors.firstName as FieldError}
                  isTouched={touchedFields.firstName}
                />
                <ValidatedField
                  name="middleName"
                  label={translate('global.form.middleName.label')}
                  placeholder={translate('global.form.middleName.placeholder')}
                  disabled={!isEditing}
                  data-cy="middleName"
                  className="formInput"
                  register={register}
                  error={errors.middleName as FieldError}
                  isTouched={touchedFields.middleName}
                />
              </Row>
              <Row>
                <ValidatedField
                  name="firstLastName"
                  label={translate('global.form.firstLastName.label')}
                  placeholder={translate('global.form.firstLastName.placeholder')}
                  required
                  disabled={!isEditing}
                  data-cy="firstLastName"
                  validate={{
                    required: { value: true, message: translate('settings.messages.validate.lastname.required') },
                    minLength: { value: 1, message: translate('settings.messages.validate.lastname.minlength') },
                    maxLength: { value: 50, message: translate('settings.messages.validate.lastname.maxlength') },
                  }}
                  className="formInput"
                  register={register}
                  error={errors.firstLastName as FieldError}
                  isTouched={touchedFields.firstLastName}
                />
                <ValidatedField
                  name="secondLastName"
                  label={translate('global.form.secondLastName.label')}
                  placeholder={translate('global.form.secondLastName.placeholder')}
                  disabled={!isEditing}
                  data-cy="secondLastName"
                  className="formInput"
                  register={register}
                  error={errors.secondLastName as FieldError}
                  isTouched={touchedFields.secondLastName}
                />
              </Row>
              <Row>
                <FormLabel className="formTitles"> Identificacion </FormLabel>
                <ValidatedField
                  name="documentType"
                  label="Tipo de Documento"
                  placeholder="Tipo de Documento"
                  type="text"
                  required
                  disabled={true}
                  data-cy="documentType"
                  className="formInput"
                  register={register}
                />
                <ValidatedField
                  name="documentNumber"
                  label="Numero de Documento"
                  placeholder="Numero de Documento"
                  type="text"
                  required
                  disabled={true}
                  data-cy="documentNumber"
                  className="formInput"
                  register={register}
                />
              </Row>
              <Row>
                <FormLabel className="formTitles"> Contacto y Seguirdad </FormLabel>
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
                  disabled={!isEditing}
                  data-cy="email"
                  className="smallFormField"
                  register={register}
                  error={errors.email as FieldError}
                  isTouched={touchedFields.email}
                />
                <ValidatedField
                  name="phoneNumber"
                  label={translate('global.form.phoneNumber.label')}
                  placeholder={translate('global.form.phoneNumber.placeholder')}
                  required
                  disabled={!isEditing}
                  data-cy="phoneNumber"
                  className="smallFormField"
                  validate={{ pattern: { value: /^\d{10}$/, message: 'El teléfono debe tener exactamente 10 dígitos' } }}
                  register={register}
                  error={errors.phoneNumber as FieldError}
                  isTouched={touchedFields.phoneNumber}
                />
                <ValidatedField
                  name="currentPassword"
                  label={translate('global.form.currentpassword.label')}
                  placeholder={translate('global.form.currentpassword.placeholder')}
                  type="password"
                  disabled={!isEditing}
                  validate={{
                    minLength: { value: 4, message: translate('global.messages.validate.newpassword.minlength') },
                    maxLength: { value: 50, message: translate('global.messages.validate.newpassword.maxlength') },
                  }}
                  data-cy="currentPassword"
                  className="smallFormField"
                  register={register}
                  error={errors.currentPassword as FieldError}
                  isTouched={touchedFields.currentPassword}
                />
                <ValidatedField
                  name="newPassword"
                  label={translate('global.form.newpassword.label')}
                  placeholder={translate('global.form.newpassword.placeholder')}
                  type="password"
                  onChange={updatePassword}
                  disabled={!isEditing}
                  validate={{
                    minLength: { value: 4, message: translate('global.messages.validate.newpassword.minlength') },
                    maxLength: { value: 50, message: translate('global.messages.validate.newpassword.maxlength') },
                  }}
                  data-cy="newPassword"
                  className="smallFormField"
                  register={register}
                  error={errors.newPassword as FieldError}
                  isTouched={touchedFields.newPassword}
                />
              </Row>
              <PasswordStrengthBar password={password} />
              {isEditing ? (
                <div className="d-flex">
                  <Button variant="primary" type="submit" data-cy="submit">
                    <Translate contentKey="settings.form.savebutton">Save</Translate>
                  </Button>
                  <Button variant="primary" type="button" onClick={handleCancelClick} data-cy="cancel">
                    <Translate contentKey="settings.form.cancelbutton">Cancel</Translate>
                  </Button>
                </div>
              ) : (
                <Button variant="primary" type="button" onClick={handleEditClick} data-cy="edit">
                  <Translate contentKey="settings.form.editbutton">Edit</Translate>
                </Button>
              )}
            </Form>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default SettingsPage;

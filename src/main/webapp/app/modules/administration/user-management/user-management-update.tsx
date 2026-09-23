import React, { useEffect, useState } from 'react';
import { Button, Card, Form, Col, Row, FormLabel } from 'react-bootstrap';
import { Translate, ValidatedField, isEmail, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { faArrowLeft, faSave } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDocumentTypes } from 'app/entities/document-type/document-type.reducer';

import { createUser, getRoles, getUser, reset, updateUser } from './user-management.reducer';
import { type FieldError, useForm } from 'react-hook-form';

export const UserManagementUpdate = () => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { login } = useParams<'login'>();
  const isNew = login === undefined;

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getUser(login));
    }
    dispatch(getRoles());
    dispatch(getDocumentTypes({}));
    return () => {
      dispatch(reset());
    };
  }, [login]);

  const handleEditClick = () => setIsEditing(true);
  const handleCancelClick = () => setIsEditing(false);
  const handleClose = () => {
    navigate('/admin/users');
  };

  const saveUser = values => {
    if (isNew) {
      if (values.id === '') {
        delete values.id;
      }
      dispatch(createUser(values));
    } else {
      dispatch(updateUser(values));
    }
    handleClose();
  };

  const {
    handleSubmit,
    register,
    reset: resetForm,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched' });

  const user = useAppSelector(state => state.userManagement.user);
  const updating = useAppSelector(state => state.userManagement.updating);
  const documentTypes = useAppSelector(state => state.documentType.entities);
  const authorities = useAppSelector(state => state.userManagement.authorities);

  useEffect(() => {
    if (!isNew && user.id) {
      const role = user.authorities?.find(authority => authority !== 'ROLE_USER') ?? '';
      resetForm({ ...user, role });
    }
  }, [user, isNew, resetForm]);

  return (
    <div className="px-4">
      <Row className="justify-content-start">
        <Col md="12">
          {!isNew && (
            <div>
              <h1 data-cy="UserManagementCreateUpdateHeading"> Editar Usuario </h1>
              <p>
                {' '}
                Modifique los datos correspondientes al usuario seleccinado. Asegúrese de que la información sea correcta antes de guardar.
              </p>
            </div>
          )}
          {isNew && (
            <div>
              <h1 data-cy="UserManagementCreateUpdateHeading"> Nuevo Usuario </h1>
              <p>
                {' '}
                Diligencie el siguiente formulario para registrar un nuevo usuario en la plataforma. Asegúrese de verificar la información
                antes de guardar.
              </p>
            </div>
          )}
        </Col>
      </Row>
      <Card className="top-border-card">
        <Row className="justify-content-start">
          <Col md="12">
            <Form onSubmit={handleSubmit(saveUser)}>
              {user.id && (
                <ValidatedField
                  type="text"
                  name="id"
                  data-cy="id"
                  disabled={!isNew && !isEditing}
                  required
                  readOnly
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                  register={register}
                  error={errors.id as FieldError}
                  isTouched={touchedFields.id}
                />
              )}
              <div>
                <FormLabel className="formTitles"> Informacion Personal </FormLabel>
                <Col md="12" className="d-flex justify-content-between">
                  <ValidatedField
                    name="firstName"
                    label={translate('global.form.firstName.label')}
                    placeholder={translate('global.form.firstName.placeholder')}
                    disabled={!isNew && !isEditing}
                    required
                    data-cy="firstName"
                    validate={{ required: 'Ingrese su primer nombre' }}
                    register={register}
                    error={errors.firstName as FieldError}
                    isTouched={touchedFields.firstName}
                    className="formInput"
                  />
                  <ValidatedField
                    name="middleName"
                    label={translate('global.form.middleName.label')}
                    placeholder={translate('global.form.middleName.placeholder')}
                    disabled={!isNew && !isEditing}
                    data-cy="middleName"
                    register={register}
                    error={errors.middleName as FieldError}
                    isTouched={touchedFields.middleName}
                    className="formInput"
                  />
                </Col>
                <Col md="12" className="d-flex justify-content-between">
                  <ValidatedField
                    name="firstLastName"
                    label={translate('global.form.firstLastName.label')}
                    placeholder={translate('global.form.firstLastName.placeholder')}
                    disabled={!isNew && !isEditing}
                    required
                    data-cy="firstLastName"
                    validate={{ required: 'Ingrese su primer apellido' }}
                    register={register}
                    error={errors.firstLastName as FieldError}
                    isTouched={touchedFields.firstLastName}
                    className="formInput"
                  />
                  <ValidatedField
                    name="secondLastName"
                    label={translate('global.form.secondLastName.label')}
                    placeholder={translate('global.form.secondLastName.placeholder')}
                    disabled={!isNew && !isEditing}
                    data-cy="secondLastName"
                    register={register}
                    error={errors.secondLastName as FieldError}
                    isTouched={touchedFields.secondLastName}
                    className="formInput"
                  />
                </Col>
              </div>
              <div>
                <FormLabel className="formTitles"> Identificacion y Rol </FormLabel>
                <Col md="12" className="d-flex justify-content-between">
                  <ValidatedField
                    id="documentTypeId"
                    name="documentTypeId"
                    data-cy="documentTypeId"
                    label="Tipo de Documento"
                    disabled={!isNew && !isEditing}
                    type="select"
                    required
                    validate={{ required: 'Selecciona un tipo de documento' }}
                    register={register}
                    error={errors.documentTypeId as FieldError}
                    isTouched={touchedFields.documentTypeId}
                    className="smallFormField"
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
                    disabled={!isNew && !isEditing}
                    required
                    data-cy="documentNumber"
                    validate={{ required: 'Ingrese su numero de documento' }}
                    register={register}
                    error={errors.documentNumber as FieldError}
                    isTouched={touchedFields.documentNumber}
                    className="smallFormField"
                  />
                  <ValidatedField
                    name="role"
                    data-cy="profiles"
                    label="Rol"
                    type="select"
                    disabled={!isNew && !isEditing}
                    required
                    validate={{ required: 'Selecciona un Rol' }}
                    register={register}
                    error={errors.role as FieldError}
                    isTouched={touchedFields.role}
                    className="smallFormField"
                  >
                    <option value="" key="0">
                      Selecciona una opcion
                    </option>
                    {authorities
                      ? authorities.map(role => (
                          <option value={role} key={role}>
                            {role}
                          </option>
                        ))
                      : null}
                  </ValidatedField>
                </Col>
              </div>
              <div>
                <FormLabel className="formTitles"> Contacto y Seguridad </FormLabel>
                <Col md="12" className="d-flex justify-content-between">
                  <ValidatedField
                    name="email"
                    data-cy="email"
                    label={translate('global.form.email.label')}
                    placeholder={translate('global.form.email.placeholder')}
                    disabled={!isNew && !isEditing}
                    type="email"
                    className="smallFormField"
                    validate={{
                      required: {
                        value: true,
                        message: translate('global.messages.validate.email.required'),
                      },
                      minLength: {
                        value: 5,
                        message: translate('global.messages.validate.email.minlength'),
                      },
                      maxLength: {
                        value: 254,
                        message: translate('global.messages.validate.email.maxlength'),
                      },
                      validate: v => isEmail(v) || translate('global.messages.validate.email.invalid'),
                    }}
                    register={register}
                    error={errors.email as FieldError}
                    isTouched={touchedFields.email}
                  />
                  <ValidatedField
                    name="phoneNumber"
                    label={translate('global.form.phoneNumber.label')}
                    placeholder={translate('global.form.phoneNumber.placeholder')}
                    disabled={!isNew && !isEditing}
                    required
                    data-cy="phoneNumber"
                    validate={{ required: 'Ingrese su numero de telefono' }}
                    register={register}
                    error={errors.phoneNumber as FieldError}
                    isTouched={touchedFields.phoneNumber}
                    className="smallFormField"
                  />
                  <ValidatedField
                    name="password"
                    label={translate('global.form.newpassword.label')}
                    placeholder={translate('global.form.newpassword.placeholder')}
                    disabled={!isNew && !isEditing}
                    type="password"
                    className="smallFormField"
                    validate={{
                      required: { value: true, message: translate('global.messages.validate.newpassword.required') },
                      minLength: { value: 4, message: translate('global.messages.validate.newpassword.minlength') },
                      maxLength: { value: 50, message: translate('global.messages.validate.newpassword.maxlength') },
                    }}
                    data-cy="firstPassword"
                    register={register}
                    error={errors.password as FieldError}
                    isTouched={touchedFields.password}
                  />
                </Col>
              </div>
              {isNew ? (
                <div>
                  <Button as={Link as any} to="/admin/users" replace variant="info" data-cy="entityCreateCancelButton">
                    <FontAwesomeIcon icon={faArrowLeft} />
                    &nbsp;
                    <span className="d-none d-md-inline">Cancelar</span>
                  </Button>
                  &nbsp;
                  <Button variant="primary" type="submit" disabled={updating} data-cy="entityCreateSaveButton">
                    <FontAwesomeIcon icon={faSave} />
                    &nbsp;
                    <Translate contentKey="entity.action.save">Save</Translate>
                  </Button>
                </div>
              ) : isEditing ? (
                <div>
                  <Button type="button" variant="info" onClick={handleCancelClick} data-cy="entityCreateCancelButton">
                    <FontAwesomeIcon icon={faArrowLeft} />
                    &nbsp;
                    <span className="d-none d-md-inline">Cancelar</span>
                  </Button>
                  &nbsp;
                  <Button variant="primary" type="submit" disabled={updating} data-cy="entityCreateSaveButton">
                    <FontAwesomeIcon icon={faSave} />
                    &nbsp;
                    <Translate contentKey="entity.action.save">Save</Translate>
                  </Button>
                </div>
              ) : (
                <div>
                  <Button as={Link as any} to="/admin/users" replace variant="info" data-cy="entityCreateCancelButton">
                    <FontAwesomeIcon icon={faArrowLeft} />
                    &nbsp;
                    <span className="d-none d-md-inline">Cancelar</span>
                  </Button>
                  &nbsp;
                  <Button type="button" variant="primary" data-cy="entityCreateCancelButton" onClick={handleEditClick}>
                    <span className="d-none d-md-inline">Editar</span>
                  </Button>
                </div>
              )}
            </Form>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default UserManagementUpdate;

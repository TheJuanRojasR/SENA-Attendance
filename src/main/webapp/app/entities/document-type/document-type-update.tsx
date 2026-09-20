import React, { useEffect } from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './document-type.reducer';

export const DocumentTypeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const documentTypeEntity = useAppSelector(state => state.documentType.entity);
  const loading = useAppSelector(state => state.documentType.loading);
  const updating = useAppSelector(state => state.documentType.updating);
  const updateSuccess = useAppSelector(state => state.documentType.updateSuccess);

  const handleClose = () => {
    navigate('/document-type');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...documentTypeEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...documentTypeEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <div>
              <h2 id="senaAttendanceApp.documentType.home.createOrEditLabel" data-cy="DocumentTypeCreateUpdateHeading">
                Editar Tipo de Documento
              </h2>
              <p> Actualice el nombre oficial o siglas instuticionales del tipo de documento para la plataforma. </p>
            </div>
          )}
          {isNew && (
            <div>
              <h2 id="senaAttendanceApp.documentType.home.createOrEditLabel" data-cy="DocumentTypeCreateUpdateHeading">
                Crear Nuevo Tipo de Documento
              </h2>
              <p>
                {' '}
                Complete el siguiente formulario para registrar un nuevo tipo de documento válido para la identiciacion y registro
                institucional.
              </p>
            </div>
          )}
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="12">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <Card>
              <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
                <ValidatedField
                  label={translate('senaAttendanceApp.documentType.name')}
                  id="document-type-name"
                  name="name"
                  data-cy="name"
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 30, message: translate('entity.validation.maxlength', { max: 30 }) },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.documentType.initials')}
                  id="document-type-initials"
                  name="initials"
                  data-cy="initials"
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 10, message: translate('entity.validation.maxlength', { max: 10 }) },
                  }}
                />
                {!isNew && (
                  <ValidatedField
                    label={translate('senaAttendanceApp.documentType.Active')}
                    id="documentType-isActive"
                    name="isActive"
                    data-cy="isActive"
                    check
                    type="checkbox"
                  />
                )}
                <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/document-type" replace variant="info">
                  <FontAwesomeIcon icon="arrow-left" />
                  &nbsp;
                  <span className="d-none d-md-inline">Cancelar</span>
                </Button>
                &nbsp;
                <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                  <FontAwesomeIcon icon="save" />
                  &nbsp;
                  <Translate contentKey="entity.action.save">Save</Translate>
                </Button>
              </ValidatedForm>
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default DocumentTypeUpdate;

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
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> TIPOS DE DOCUMENTO <span className="separator">/</span>{' '}
            <span className="current" style={{ color: '#6bc120', fontWeight: 'bold' }}>
              {isNew ? 'CREAR NUEVO' : 'EDITAR'}
            </span>
          </div>
          <h2 id="documentType-heading" data-cy="DocumentTypeCreateUpdateHeading" className="page-title">
            {isNew ? 'Crear Nuevo Tipo de Documento' : 'Editar Tipo de Documento'}
          </h2>
          <p className="page-description">
            {isNew
              ? 'Complete el siguiente formulario para registrar un nuevo tipo de documento válido para la identificación y registro institucional.'
              : 'Actualice el nombre oficial o siglas institucionales del tipo de documento para la plataforma.'}
          </p>
        </div>
      </div>

      <div className="entity-card">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
            <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
              <h5 className="mb-0 fw-bold" style={{ color: '#00324d' }}>
                <FontAwesomeIcon icon="id-card" className="me-2" style={{ color: '#6bc120' }} />
                Información del Documento
              </h5>
              <span className="text-muted" style={{ fontSize: '0.85rem' }}>
                * Campos obligatorios
              </span>
            </div>

            <ValidatedField
              label="Nombre del Documento *"
              id="document-type-name"
              name="name"
              data-cy="name"
              type="text"
              placeholder="Ej. Cédula de Ciudadanía, Tarjeta de Identidad, Pasaporte..."
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 30, message: translate('entity.validation.maxlength', { max: 30 }) },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Ingrese el nombre formal completo del tipo de documento de identidad.
            </small>

            <Row>
              <Col md={6}>
                <ValidatedField
                  label="Iniciales / Sigla *"
                  id="document-type-initials"
                  name="initials"
                  data-cy="initials"
                  type="text"
                  placeholder="EJ. CC, TI, PAS, CE..."
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 10, message: translate('entity.validation.maxlength', { max: 10 }) },
                  }}
                />
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Abreviatura oficial utilizada en reportes y listas del sistema.
                </small>
              </Col>
            </Row>

            {!isNew && (
              <ValidatedField label="Estado (Activo)" id="documentType-isActive" name="isActive" data-cy="isActive" check type="checkbox" />
            )}

            <div className="d-flex justify-content-end mt-4 gap-2">
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/document-type"
                replace
                variant="outline-secondary"
                className="fw-bold"
                style={{ borderRadius: '8px', padding: '10px 20px', borderColor: '#d3d3d3', color: '#4a4a4a' }}
              >
                Cancelar
              </Button>
              <Button
                variant="success"
                id="save-entity"
                data-cy="entityCreateSaveButton"
                type="submit"
                disabled={updating}
                className="fw-bold"
                style={{ backgroundColor: '#6bc120', borderColor: '#6bc120', borderRadius: '8px', padding: '10px 20px' }}
              >
                <FontAwesomeIcon icon="save" className="me-2" />
                {isNew ? 'Crear Tipo de Documento' : 'Guardar Cambios'}
              </Button>
            </div>
          </ValidatedForm>
        )}
      </div>
    </div>
  );
};

export default DocumentTypeUpdate;

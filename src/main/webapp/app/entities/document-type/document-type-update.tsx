import React, { useEffect, useState } from 'react';
import { Button } from 'react-bootstrap';
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

  // UC022-E3: si el backend rechaza el cambio porque el tipo ya está en uso, las iniciales se
  // bloquean visualmente para que el Administrador no repita el mismo intento.
  const [initialsLocked, setInitialsLocked] = useState(false);

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

  const saveEntity = async values => {
    const entity = {
      ...documentTypeEntity,
      ...values,
    };

    const resultAction = isNew ? await dispatch(createEntity(entity)) : await dispatch(updateEntity(entity));
    if (!isNew && updateEntity.rejected.match(resultAction)) {
      const data = (resultAction.error as any)?.response?.data;
      if (data?.message === 'error.documentTypeInitialsInUse') {
        setInitialsLocked(true);
      }
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

            <ValidatedField
              label="Iniciales / Sigla *"
              id="document-type-initials"
              name="initials"
              data-cy="initials"
              type="text"
              placeholder="EJ. CC, TI, PAS, CE..."
              disabled={initialsLocked}
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 10, message: translate('entity.validation.maxlength', { max: 10 }) },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              {initialsLocked
                ? 'Este tipo de documento ya está en uso por usuarios: las iniciales no se pueden modificar.'
                : 'Abreviatura oficial utilizada en reportes y listas del sistema.'}
            </small>

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

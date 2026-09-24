import React, { useEffect } from 'react';
import { Button } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, partialUpdateEntity } from './modality.reducer';

export const ModalityUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const modalityEntity = useAppSelector(state => state.modality.entity);
  const loading = useAppSelector(state => state.modality.loading);
  const updating = useAppSelector(state => state.modality.updating);
  const updateSuccess = useAppSelector(state => state.modality.updateSuccess);

  const handleClose = () => {
    navigate('/modality');
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
      ...modalityEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(partialUpdateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...modalityEntity,
        };

  return (
    <div>
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> MODALIDADES <span className="separator">/</span>{' '}
            <span className="current">{isNew ? 'CREAR NUEVA' : 'EDITAR'}</span>
          </div>
          <h2 id="modality-heading" data-cy="ModalityCreateUpdateHeading" className="page-title">
            {isNew ? 'Crear Nueva Modalidad' : 'Editar Modalidad'}
          </h2>
          <p className="page-description">
            {isNew
              ? 'Complete el siguiente formulario para registrar una nueva modalidad formativa institucional en el centro de formación.'
              : 'Actualice el nombre oficial y disponibilidad de la modalidad formativa en la institución.'}
          </p>
        </div>
      </div>

      <div className="entity-card">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
            <h5 className="mb-4 pb-2 border-bottom fw-bold" style={{ color: '#00324d' }}>
              Información de la Modalidad
            </h5>

            <ValidatedField
              label="Nombre de la Modalidad *"
              id="modality-name"
              name="name"
              data-cy="name"
              type="text"
              placeholder="Ej. Presencial, Virtual, A Distancia / Mixta..."
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 50, message: translate('entity.validation.maxlength', { max: 50 }) },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Ingrese el nombre identificador oficial para la modalidad formativa institucional.
            </small>

            {!isNew && (
              <ValidatedField label="Estado (Activo)" id="modality-isActive" name="isActive" data-cy="isActive" check type="checkbox" />
            )}

            <div className="d-flex justify-content-end mt-4 gap-2">
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/modality"
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
                style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
              >
                <FontAwesomeIcon icon="save" className="me-2" />
                {isNew ? 'Crear Modalidad' : 'Guardar Cambios'}
              </Button>
            </div>
          </ValidatedForm>
        )}
      </div>
    </div>
  );
};

export default ModalityUpdate;

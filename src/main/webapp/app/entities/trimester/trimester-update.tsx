import React, { useEffect, useState } from 'react';
import { Button } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './trimester.reducer';
import { faArrowLeft } from '@fortawesome/free-solid-svg-icons';

export const TrimesterUpdate = () => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const trimesterEntity = useAppSelector(state => state.trimester.entity);
  const loading = useAppSelector(state => state.trimester.loading);
  const updating = useAppSelector(state => state.trimester.updating);
  const updateSuccess = useAppSelector(state => state.trimester.updateSuccess);

  const handleEditClick = () => setIsEditing(true);
  const handleCancelClick = () => setIsEditing(false);
  const handleClose = () => {
    navigate(`/trimester${location.search}`);
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
      ...trimesterEntity,
      ...values,
    };
    // El backend calcula status por fechas y lo ignora en POST/PUT/PATCH; no se envía.
    delete entity.status;

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
          ...trimesterEntity,
          status: translate(`senaAttendanceApp.StateTrimester.${trimesterEntity.status}`),
        };

  return (
    <div>
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> TRIMESTRES ACADÉMICOS <span className="separator">/</span>{' '}
            <span className="current" style={{ color: '#6bc120', fontWeight: 'bold' }}>
              {isNew ? 'CREAR NUEVO' : 'EDITAR'}
            </span>
          </div>
          <h2 id="trimester-heading" data-cy="TrimesterCreateUpdateHeading" className="page-title">
            {isNew ? 'Crear Nuevo Trimestre' : 'Editar Trimestre'}
          </h2>
          <p className="page-description">
            {isNew
              ? 'Ingrese la información requerida para registrar y programar un nuevo trimestre académico en el calendario formativo institucional.'
              : 'Actualice la información y el rango de fechas lectivas del trimestre académico en el calendario formativo institucional.'}
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
                <FontAwesomeIcon icon="calendar-alt" className="me-2" style={{ color: '#6bc120' }} />
                Información del Trimestre
              </h5>
              <span className="text-muted" style={{ fontSize: '0.85rem' }}>
                * Campos obligatorios
              </span>
            </div>

            <ValidatedField
              label="NOMBRE DEL TRIMESTRE *"
              id="trimester-name"
              name="name"
              data-cy="name"
              disabled={!isNew && !isEditing}
              type="text"
              placeholder="Ej. Trimestre I - 2025"
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 30, message: translate('entity.validation.maxlength', { max: 30 }) },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Ingrese el nombre identificador oficial del período académico.
            </small>

            <ValidatedField
              label="FECHA DE INICIO *"
              id="trimester-startDate"
              name="startDate"
              data-cy="startDate"
              disabled={!isNew && !isEditing}
              type="date"
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Fecha oficial de inicio de actividades lectivas.
            </small>
            <ValidatedField
              label="FECHA DE FIN *"
              id="trimester-endDate"
              name="endDate"
              data-cy="endDate"
              disabled={!isNew && !isEditing}
              type="date"
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Fecha oficial de culminación del trimestre.
            </small>

            {!isNew && <ValidatedField label="ESTADO" id="trimester-status" name="status" data-cy="status" type="text" disabled />}

            <div className="d-flex justify-content-end mt-4 gap-2">
              {isNew ? (
                <>
                  <Button
                    as={Link as any}
                    id="cancel-save"
                    data-cy="entityCreateCancelButton"
                    to="/trimester"
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
                    <FontAwesomeIcon icon="check" className="me-2" />
                    Crear Trimestre
                  </Button>
                </>
              ) : isEditing ? (
                <>
                  <Button
                    type="button"
                    variant="outline-secondary"
                    onClick={handleCancelClick}
                    data-cy="entityCreateCancelButton"
                    className="fw-bold"
                    style={{ borderRadius: '8px', padding: '10px 20px', borderColor: '#d3d3d3', color: '#4a4a4a' }}
                  >
                    Cancelar
                  </Button>
                  <Button
                    variant="success"
                    type="submit"
                    disabled={updating}
                    data-cy="entityCreateSaveButton"
                    className="fw-bold"
                    style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
                  >
                    <FontAwesomeIcon icon="check" className="me-2" />
                    Guardar Cambios
                  </Button>
                </>
              ) : (
                <>
                  <Button
                    as={Link as any}
                    to="/trimester"
                    replace
                    variant="outline-secondary"
                    data-cy="entityCreateCancelButton"
                    className="fw-bold"
                    style={{ borderRadius: '8px', padding: '10px 20px', borderColor: '#d3d3d3', color: '#4a4a4a' }}
                  >
                    <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
                    Volver
                  </Button>
                  <Button
                    type="button"
                    variant="primary"
                    data-cy="entityCreateCancelButton"
                    onClick={handleEditClick}
                    className="fw-bold"
                    style={{ borderRadius: '8px', padding: '10px 20px' }}
                  >
                    Editar
                  </Button>
                </>
              )}
            </div>
          </ValidatedForm>
        )}
      </div>
    </div>
  );
};

export default TrimesterUpdate;

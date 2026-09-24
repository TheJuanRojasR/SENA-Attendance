import React, { useEffect } from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, partialUpdateEntity } from './time-slot.reducer';

export const TimeSlotUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const timeSlotEntity = useAppSelector(state => state.timeSlot.entity);
  const loading = useAppSelector(state => state.timeSlot.loading);
  const updating = useAppSelector(state => state.timeSlot.updating);
  const updateSuccess = useAppSelector(state => state.timeSlot.updateSuccess);

  const handleClose = () => {
    navigate('/time-slot');
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
      ...timeSlotEntity,
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
          ...timeSlotEntity,
        };

  return (
    <div>
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> JORNADAS <span className="separator">/</span>{' '}
            <span className="current">{isNew ? 'CREAR NUEVA' : 'EDITAR'}</span>
          </div>
          <h2 id="time-slot-heading" data-cy="TimeSlotCreateUpdateHeading" className="page-title">
            {isNew ? 'Crear Nueva Jornada' : 'Editar Jornada'}
          </h2>
          <p className="page-description">
            {isNew
              ? 'Complete el siguiente formulario para registrar una nueva jornada formativa en el centro. Asegúrese de especificar correctamente los horarios de inicio y finalización.'
              : 'Actualice los datos y franja horaria de la jornada formativa en el centro formativo.'}
          </p>
        </div>
      </div>

      <div className="entity-card">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
            <h5 className="mb-4 pb-2 border-bottom fw-bold" style={{ color: '#00324d' }}>
              Información de la Jornada
            </h5>

            <ValidatedField
              label="Nombre de la Jornada *"
              id="time-slot-name"
              name="name"
              data-cy="name"
              type="text"
              placeholder="Ej. Jornada Diurna Especial, Jornada Tarde..."
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 50, message: translate('entity.validation.maxlength', { max: 50 }) },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Ingrese el nombre identificador para la jornada institucional.
            </small>

            <Row>
              <Col md={6}>
                <ValidatedField
                  label="Hora Inicio *"
                  id="time-slot-startTime"
                  name="startTime"
                  data-cy="startTime"
                  type="time"
                  placeholder="HH:mm"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Hora de inicio de la jornada formativa.
                </small>
              </Col>
              <Col md={6}>
                <ValidatedField
                  label="Hora Fin *"
                  id="time-slot-endTime"
                  name="endTime"
                  data-cy="endTime"
                  type="time"
                  placeholder="HH:mm"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Hora de finalización habitual.
                </small>
              </Col>
            </Row>

            {!isNew && (
              <ValidatedField label="Estado (Activo)" id="time-slot-isActive" name="isActive" data-cy="isActive" check type="checkbox" />
            )}

            <div className="d-flex justify-content-end mt-4 gap-2">
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/time-slot"
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
                {isNew ? 'Crear Jornada' : 'Guardar Cambios'}
              </Button>
            </div>
          </ValidatedForm>
        )}
      </div>
    </div>
  );
};

export default TimeSlotUpdate;

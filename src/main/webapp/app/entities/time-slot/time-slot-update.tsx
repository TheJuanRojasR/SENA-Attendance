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
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <div>
              <h2 id="senaAttendanceApp.timeSlot.home.createOrEditLabel" data-cy="TimeSlotCreateUpdateHeading">
                Editar Jornada
              </h2>
              <p> Actualice los datos y franja horaria de la jornada formativa en el centro formativo. </p>
            </div>
          )}
          {isNew && (
            <div>
              <h2 id="senaAttendanceApp.timeSlot.home.createOrEditLabel" data-cy="TimeSlotCreateUpdateHeading">
                Crear Jornada
              </h2>
              <p>
                Complete el siguiente formulario para registrar una nueva jornada formativa en el centro. Asegúrese de especificar
                correctamente los horarios de inicio y finalizacion
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
            <Card className="top-border-card">
              <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
                <ValidatedField
                  label={translate('senaAttendanceApp.timeSlot.name')}
                  id="time-slot-name"
                  name="name"
                  data-cy="name"
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 50, message: translate('entity.validation.maxlength', { max: 50 }) },
                  }}
                />
                {!isNew && (
                  <ValidatedField
                    label={translate('senaAttendanceApp.timeSlot.isActive')}
                    id="time-slot-isActive"
                    name="isActive"
                    data-cy="isActive"
                    check
                    type="checkbox"
                  />
                )}
                <ValidatedField
                  label={translate('senaAttendanceApp.timeSlot.startTime')}
                  id="time-slot-startTime"
                  name="startTime"
                  data-cy="startTime"
                  type="time"
                  placeholder="HH:mm"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.timeSlot.endTime')}
                  id="time-slot-endTime"
                  name="endTime"
                  data-cy="endTime"
                  type="time"
                  placeholder="HH:mm"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/time-slot" replace variant="info">
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

export default TimeSlotUpdate;

import React, { useEffect } from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { State } from 'app/shared/model/enumerations/state.model';

import { createEntity, getEntity, reset, updateEntity } from './justification-type.reducer';

export const JustificationTypeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const justificationTypeEntity = useAppSelector(state => state.justificationType.entity);
  const loading = useAppSelector(state => state.justificationType.loading);
  const updating = useAppSelector(state => state.justificationType.updating);
  const updateSuccess = useAppSelector(state => state.justificationType.updateSuccess);
  const stateValues = Object.keys(State);

  const handleClose = () => {
    navigate('/justification-type');
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
    if (values.limitPerTrimester !== undefined && typeof values.limitPerTrimester !== 'number') {
      values.limitPerTrimester = Number(values.limitPerTrimester);
    }

    const entity = {
      ...justificationTypeEntity,
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
          status: 'ACTIVO',
          ...justificationTypeEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <h2 id="senaAttendanceApp.justificationType.home.createOrEditLabel" data-cy="JustificationTypeCreateUpdateHeading">
              Editar Tipo de Justificación
            </h2>
          )}
          {isNew && (
            <h2 id="senaAttendanceApp.justificationType.home.createOrEditLabel" data-cy="JustificationTypeCreateUpdateHeading">
              Crear Tipo de Justificación
            </h2>
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
                  label={translate('senaAttendanceApp.justificationType.name')}
                  id="justification-type-name"
                  name="name"
                  data-cy="name"
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.justificationType.limitPerTrimester')}
                  id="justification-type-limitPerTrimester"
                  name="limitPerTrimester"
                  data-cy="limitPerTrimester"
                  type="text"
                />
                {!isNew && (
                  <ValidatedField
                    label={translate('senaAttendanceApp.justificationType.status')}
                    id="justification-type-status"
                    name="status"
                    data-cy="status"
                    type="select"
                  >
                    {stateValues.map(state => (
                      <option value={state} key={state}>
                        {translate(`senaAttendanceApp.State.${state}`)}
                      </option>
                    ))}
                  </ValidatedField>
                )}
                <Button
                  as={Link as any}
                  id="cancel-save"
                  data-cy="entityCreateCancelButton"
                  to="/justification-type"
                  replace
                  variant="info"
                >
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

export default JustificationTypeUpdate;

import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './trimester.reducer';
import { faArrowLeft, faSave } from '@fortawesome/free-solid-svg-icons';

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
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <div>
              <h2 id="senaAttendanceApp.trimester.home.createOrEditLabel" data-cy="TrimesterCreateUpdateHeading">
                Editar Trimestre
              </h2>
              <p>
                {' '}
                Actualice la información y el rango de frachas lectivas del trimestre académico en el canlendario formativo
                institucional{' '}
              </p>
            </div>
          )}
          {isNew && (
            <div>
              <h2 id="senaAttendanceApp.trimester.home.createOrEditLabel" data-cy="TrimesterCreateUpdateHeading">
                Crear Nuevo Trimestre
              </h2>
              <p>
                {' '}
                Ingrese la información requerida para registrar y programar un nuevo trimestre académico en el calendario formativo
                institucional.{' '}
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
                  label={translate('senaAttendanceApp.trimester.name')}
                  id="trimester-name"
                  name="name"
                  data-cy="name"
                  disabled={!isNew && !isEditing}
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 30, message: translate('entity.validation.maxlength', { max: 30 }) },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.trimester.startDate')}
                  id="trimester-startDate"
                  name="startDate"
                  data-cy="startDate"
                  disabled={!isNew && !isEditing}
                  type="date"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.trimester.endDate')}
                  id="trimester-endDate"
                  name="endDate"
                  data-cy="endDate"
                  disabled={!isNew && !isEditing}
                  type="date"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                {!isNew && (
                  <ValidatedField
                    label={translate('senaAttendanceApp.trimester.state')}
                    id="trimester-status"
                    name="status"
                    data-cy="status"
                    type="text"
                    disabled
                  />
                )}
                {isNew ? (
                  <div>
                    <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/trimester" replace variant="info">
                      <FontAwesomeIcon icon="arrow-left" />
                      &nbsp;
                      <span className="d-none d-md-inline"> Cancelar </span>
                    </Button>
                    &nbsp;
                    <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                      <FontAwesomeIcon icon="save" />
                      &nbsp;
                      <Translate contentKey="entity.action.save"> Save</Translate>
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
                    <Button as={Link as any} to="/trimester" replace variant="info" data-cy="entityCreateCancelButton">
                      <FontAwesomeIcon icon={faArrowLeft} />
                      &nbsp;
                      <Translate contentKey="entity.action.back">Back</Translate>
                    </Button>
                    <Button type="button" variant="primary" data-cy="entityCreateCancelButton" onClick={handleEditClick}>
                      <span className="d-none d-md-inline">Editar</span>
                    </Button>
                  </div>
                )}
              </ValidatedForm>
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default TrimesterUpdate;

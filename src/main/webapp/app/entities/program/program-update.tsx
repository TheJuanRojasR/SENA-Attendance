import React, { useEffect, useState } from 'react';
import { Button, Card, Col, FormLabel, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './program.reducer';
import { faArrowLeft, faSave } from '@fortawesome/free-solid-svg-icons';

export const ProgramUpdate = () => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const programEntity = useAppSelector(state => state.program.entity);
  const loading = useAppSelector(state => state.program.loading);
  const updating = useAppSelector(state => state.program.updating);
  const updateSuccess = useAppSelector(state => state.program.updateSuccess);

  const handleEditClick = () => setIsEditing(true);
  const handleCancelClick = () => setIsEditing(false);
  const handleClose = () => {
    navigate(`/program${location.search}`);
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
    if (values.trimesters !== undefined && typeof values.trimesters !== 'number') {
      values.trimesters = Number(values.trimesters);
    }

    const entity = {
      ...programEntity,
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
          ...programEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <div>
              <h2 id="senaAttendanceApp.program.home.createOrEditLabel" data-cy="ProgramCreateUpdateHeading">
                Editar Programa de Formación
              </h2>
              <p> Actualice la informacion del programa de formacion seleccionado.</p>
            </div>
          )}
          {isNew && (
            <div>
              <h2 id="senaAttendanceApp.program.home.createOrEditLabel" data-cy="ProgramCreateUpdateHeading">
                Crear Nuevo Programa de Formación
              </h2>
              <p>
                Complete el siguiente formulario para registrar un nuevo programa en el catálogo institucional. Asegurese de que informaicon
                sea exacta antes de guardar.
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
                {isNew ? (
                  <FormLabel className="formTitles"> Informacion General del Programa</FormLabel>
                ) : (
                  <FormLabel className="formTitles"> Detalles del Programa</FormLabel>
                )}
                <ValidatedField
                  label={translate('senaAttendanceApp.program.name')}
                  id="program-name"
                  name="name"
                  data-cy="name"
                  disabled={!isNew && !isEditing}
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 200, message: translate('entity.validation.maxlength', { max: 200 }) },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.program.initials')}
                  id="program-initials"
                  name="initials"
                  data-cy="initials"
                  disabled={!isNew && !isEditing}
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 10, message: translate('entity.validation.maxlength', { max: 10 }) },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.program.code')}
                  id="program-code"
                  name="code"
                  data-cy="code"
                  disabled={!isNew && !isEditing}
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 30, message: translate('entity.validation.maxlength', { max: 30 }) },
                  }}
                />
                <ValidatedField
                  label={translate('senaAttendanceApp.program.trimesters')}
                  id="program-trimesters"
                  name="trimesters"
                  data-cy="trimesters"
                  disabled={!isNew && !isEditing}
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    validate: v => isNumber(v) || translate('entity.validation.number'),
                  }}
                />
                {isNew ? (
                  <div>
                    <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/program" replace variant="info">
                      <FontAwesomeIcon icon="arrow-left" />
                      &nbsp;
                      <span className="d-none d-md-inline"> Cancelar </span>
                    </Button>
                    &nbsp;
                    <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                      <FontAwesomeIcon icon="save" />
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
                    <Button as={Link as any} to="/program" replace variant="info" data-cy="entityCreateCancelButton">
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

export default ProgramUpdate;

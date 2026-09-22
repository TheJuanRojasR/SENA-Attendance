import React, { useEffect, useState } from 'react';
import { Button, Card, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getActiveEntities as getActiveModalities } from 'app/entities/modality/modality.reducer';
import { getActiveEntities as getActivePrograms } from 'app/entities/program/program.reducer';
import { getActiveEntities as getActiveTimeSlots } from 'app/entities/time-slot/time-slot.reducer';

import { cancelGrade, createEntity, getEntity, postponeGrade, resumeGrade, reset, updateEntity } from './grade.reducer';

export const GradeUpdate = () => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const programs = useAppSelector(state => state.program.entities);
  const modalities = useAppSelector(state => state.modality.entities);
  const timeSlots = useAppSelector(state => state.timeSlot.entities);
  const gradeEntity = useAppSelector(state => state.grade.entity);
  const loading = useAppSelector(state => state.grade.loading);
  const updating = useAppSelector(state => state.grade.updating);
  const updateSuccess = useAppSelector(state => state.grade.updateSuccess);

  const handleEditClick = () => setIsEditing(true);
  const handleCancelClick = () => setIsEditing(false);
  const handleClose = () => {
    navigate(`/grade${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getActivePrograms());
    dispatch(getActiveModalities({}));
    dispatch(getActiveTimeSlots({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  // Reglas de edición por estado (UC007): FINALIZADA no admite cambios; ACTIVA solo
  // endDate/program/code; APLAZADA solo endDate; PENDIENTE y CANCELADA admiten todo.
  const isFieldEditable = (field: 'code' | 'startDate' | 'endDate' | 'program' | 'modality' | 'timeSlot') => {
    if (isNew) return true;
    if (!isEditing) return false;
    switch (gradeEntity.state) {
      case 'FINALIZADA':
        return false;
      case 'ACTIVA':
        return field === 'endDate' || field === 'program' || field === 'code';
      case 'APLAZADA':
        return field === 'endDate';
      default: // PENDIENTE, CANCELADA
        return true;
    }
  };

  const isFinalized = !isNew && gradeEntity.state === 'FINALIZADA';
  const canPostpone = !isNew && (gradeEntity.state === 'PENDIENTE' || gradeEntity.state === 'ACTIVA');
  const canResume = !isNew && gradeEntity.state === 'APLAZADA';
  const canCancel = !isNew && gradeEntity.state !== 'CANCELADA';

  const handlePostpone = () => dispatch(postponeGrade(gradeEntity.id));
  const handleResume = () => dispatch(resumeGrade(gradeEntity.id));
  const handleCancelGrade = () => {
    if (globalThis.confirm('¿Confirma cancelar esta ficha? Esta acción es definitiva.')) {
      dispatch(cancelGrade(gradeEntity.id));
    }
  };

  const saveEntity = values => {
    const entity = {
      ...gradeEntity,
      ...values,
      program: programs.find(it => it.id.toString() === values.program?.toString()),
      modality: modalities.find(it => it.id.toString() === values.modality?.toString()),
      timeSlot: timeSlots.find(it => it.id.toString() === values.timeSlot?.toString()),
    };
    // El backend calcula state por fechas (o por las acciones dedicadas) y lo ignora en POST/PUT/PATCH; no se envía.
    delete entity.state;

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
          ...gradeEntity,
          program: gradeEntity?.program?.id,
          modality: gradeEntity?.modality?.id,
          timeSlot: gradeEntity?.timeSlot?.id,
          state: translate(`senaAttendanceApp.StateGrade.${gradeEntity.state}`),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <div>
              <h2 id="senaAttendanceApp.grade.home.createOrEditLabel" data-cy="GradeCreateUpdateHeading">
                Editar Ficha
              </h2>
              <p> Actualice y modifique la información oficial, período lectivo y estado operative de la ficha formativa en el sistema. </p>
            </div>
          )}
          {isNew && (
            <div>
              <h2 id="senaAttendanceApp.grade.home.createOrEditLabel" data-cy="GradeCreateUpdateHeading">
                Crear Nueva Ficha
              </h2>
              <p> Ingrese la información requerida para registrar y aperturar una nueva ficha en el sistema de gestión formativa. </p>
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
                  label={translate('senaAttendanceApp.grade.code')}
                  id="grade-code"
                  name="code"
                  data-cy="code"
                  disabled={!isFieldEditable('code')}
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 20, message: translate('entity.validation.maxlength', { max: 20 }) },
                    validate: v => /^\d+$/.test(v) || translate('entity.validation.number'),
                  }}
                />
                <FormText> Número identificador único de la ficha o grupo formativo. </FormText>
                <ValidatedField
                  label={translate('senaAttendanceApp.grade.startDate')}
                  id="grade-startDate"
                  name="startDate"
                  data-cy="startDate"
                  disabled={!isFieldEditable('startDate')}
                  type="date"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <FormText> Fecha de inicio de la etapa lectiva. </FormText>
                <ValidatedField
                  label={translate('senaAttendanceApp.grade.endDate')}
                  id="grade-endDate"
                  name="endDate"
                  data-cy="endDate"
                  disabled={!isFieldEditable('endDate')}
                  type="date"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <FormText> Fecha estimada de culminación de formación. </FormText>
                <ValidatedField
                  id="grade-program"
                  name="program"
                  data-cy="program"
                  disabled={!isFieldEditable('program')}
                  label={translate('senaAttendanceApp.grade.program')}
                  type="select"
                  required
                >
                  <option value="" key="0" />
                  {programs
                    ? programs.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </ValidatedField>
                <FormText> Programa curricular asociado a la ficha. </FormText>
                <ValidatedField
                  id="grade-timeSlot"
                  name="timeSlot"
                  data-cy="timeSlot"
                  disabled={!isFieldEditable('timeSlot')}
                  label={translate('senaAttendanceApp.grade.timeSlot')}
                  type="select"
                  required
                >
                  <option value="" key="0" />
                  {timeSlots
                    ? timeSlots.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </ValidatedField>
                <FormText> Horario en el que se impartirá la formación </FormText>
                <ValidatedField
                  id="grade-modality"
                  name="modality"
                  data-cy="modality"
                  disabled={!isFieldEditable('modality')}
                  label={translate('senaAttendanceApp.grade.modality')}
                  type="select"
                  required
                >
                  <option value="" key="0" />
                  {modalities
                    ? modalities.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </ValidatedField>
                <FormText> Modalida pedagógica de impartición </FormText>
                {!isNew && (
                  <ValidatedField
                    label={translate('senaAttendanceApp.grade.state')}
                    id="grade-state"
                    name="state"
                    data-cy="state"
                    type="text"
                    disabled
                  />
                )}
                {!isNew && <FormText> Estado de operacion académica actual. </FormText>}
                {!isNew && (canPostpone || canResume || canCancel) && (
                  <div className="mb-3 d-flex justify-content-between">
                    <div className="mb-3 d-flex gap-2">
                      {canPostpone && (
                        <Button type="button" variant="warning" onClick={handlePostpone} disabled={updating}>
                          Aplazar ficha
                        </Button>
                      )}
                      {canResume && (
                        <Button type="button" variant="success" onClick={handleResume} disabled={updating}>
                          Reanudar ficha
                        </Button>
                      )}
                      {canCancel && (
                        <Button type="button" variant="danger" onClick={handleCancelGrade} disabled={updating}>
                          Cancelar ficha
                        </Button>
                      )}
                    </div>
                    {isNew ? (
                      <div className="mb-3 d-flex gap-2">
                        <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/grade" replace variant="info">
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
                      <div className="mb-3 d-flex gap-2">
                        <Button type="button" variant="info" onClick={handleCancelClick} data-cy="entityCreateCancelButton">
                          <FontAwesomeIcon icon="arrow-left" />
                          &nbsp;
                          <span className="d-none d-md-inline"> Cancelar </span>
                        </Button>
                        &nbsp;
                        <Button variant="primary" type="submit" disabled={updating} data-cy="entityCreateSaveButton">
                          <FontAwesomeIcon icon="save" />
                          &nbsp;
                          <Translate contentKey="entity.action.save">Save</Translate>
                        </Button>
                      </div>
                    ) : (
                      <div className="mb-3 d-flex gap-2">
                        <Button as={Link as any} to="/grade" replace variant="info" data-cy="entityCreateCancelButton">
                          <FontAwesomeIcon icon="arrow-left" />
                          &nbsp;
                          <Translate contentKey="entity.action.back">Back</Translate>
                        </Button>
                        <Button
                          type="button"
                          variant="primary"
                          data-cy="entityCreateEditButton"
                          onClick={handleEditClick}
                          disabled={isFinalized}
                          title={isFinalized ? translate('error.noteditable') : undefined}
                        >
                          <span className="d-none d-md-inline">Editar</span>
                        </Button>
                      </div>
                    )}
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

export default GradeUpdate;

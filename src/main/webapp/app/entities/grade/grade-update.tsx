import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getActiveEntities as getActiveModalities } from 'app/entities/modality/modality.reducer';
import { getEntities as getPrograms } from 'app/entities/program/program.reducer';
import { getActiveEntities as getActiveTimeSlots } from 'app/entities/time-slot/time-slot.reducer';
import { StateGrade } from 'app/shared/model/enumerations/state-grade.model';

import { createEntity, getEntity, reset, updateEntity } from './grade.reducer';

export const GradeUpdate = () => {
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
  const stateGradeValues = Object.keys(StateGrade);

  const handleClose = () => {
    navigate(`/grade${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPrograms({}));
    dispatch(getActiveModalities({}));
    dispatch(getActiveTimeSlots({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...gradeEntity,
      ...values,
      program: programs.find(it => it.id.toString() === values.program?.toString()),
      modality: modalities.find(it => it.id.toString() === values.modality?.toString()),
      timeSlot: timeSlots.find(it => it.id.toString() === values.timeSlot?.toString()),
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
          state: 'ACTIVA',
          ...gradeEntity,
          program: gradeEntity?.program?.id,
          modality: gradeEntity?.modality?.id,
          timeSlot: gradeEntity?.timeSlot?.id,
        };

  return (
    <div>
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> FICHAS <span className="separator">/</span>{' '}
            <span className="current" style={{ color: '#6bc120', fontWeight: 'bold' }}>
              {isNew ? 'CREAR NUEVA FICHA' : 'EDITAR FICHA'}
            </span>
          </div>
          <h2 id="grade-heading" data-cy="GradeCreateUpdateHeading" className="page-title">
            {isNew ? 'Crear Nueva Ficha' : 'Editar Ficha'}
          </h2>
          <p className="page-description">
            {isNew
              ? 'Ingrese la información requerida para registrar y aperturar una nueva ficha en el sistema de gestión formativa SENA.'
              : 'Actualice los datos generales, de programa o fechas de la ficha en el sistema de gestión.'}
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
                <FontAwesomeIcon icon="users" className="me-2" style={{ color: '#6bc120' }} />
                Información de la Ficha
              </h5>
              <span className="text-muted" style={{ fontSize: '0.85rem' }}>
                * Campos obligatorios
              </span>
            </div>

            <ValidatedField
              label="CÓDIGO DE FICHA *"
              id="grade-code"
              name="code"
              data-cy="code"
              type="text"
              placeholder="Ej. 2894120"
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 20, message: translate('entity.validation.maxlength', { max: 20 }) },
              }}
            />
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Número identificador único de la ficha o grupo formativo.
            </small>

            <Row>
              <Col md={6}>
                <ValidatedField
                  label="FECHA DE INICIO *"
                  id="grade-startDate"
                  name="startDate"
                  data-cy="startDate"
                  type="date"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Fecha de inicio de la etapa lectiva.
                </small>
              </Col>
              <Col md={6}>
                <ValidatedField
                  label="FECHA DE FIN *"
                  id="grade-endDate"
                  name="endDate"
                  data-cy="endDate"
                  type="date"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Fecha estimada de culminación de formación.
                </small>
              </Col>
            </Row>

            <ValidatedField id="grade-program" name="program" data-cy="program" label="PROGRAMA DE FORMACIÓN *" type="select" required>
              <option value="" key="0">
                Seleccione un programa de formación...
              </option>
              {programs
                ? programs.map(otherEntity => (
                    <option value={otherEntity.id} key={otherEntity.id}>
                      {otherEntity.name}
                    </option>
                  ))
                : null}
            </ValidatedField>
            <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
              Programa curricular asociado a la ficha.
            </small>

            <Row>
              <Col md={6}>
                <ValidatedField id="grade-timeSlot" name="timeSlot" data-cy="timeSlot" label="JORNADA *" type="select" required>
                  <option value="" key="0">
                    Seleccione una jornada...
                  </option>
                  {timeSlots
                    ? timeSlots.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </ValidatedField>
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Horario en el que se impartirá la formación.
                </small>
              </Col>
              <Col md={6}>
                <ValidatedField id="grade-modality" name="modality" data-cy="modality" label="MODALIDAD *" type="select" required>
                  <option value="" key="0">
                    Seleccione una modalidad...
                  </option>
                  {modalities
                    ? modalities.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </ValidatedField>
                <small className="form-text text-muted mb-4 d-block" style={{ marginTop: '-12px' }}>
                  Modalidad pedagógica de impartición.
                </small>
              </Col>
            </Row>

            {!isNew && (
              <ValidatedField label="ESTADO" id="grade-state" name="state" data-cy="state" type="select">
                {stateGradeValues.map(stateGrade => (
                  <option value={stateGrade} key={stateGrade}>
                    {translate(`senaAttendanceApp.StateGrade.${stateGrade}`)}
                  </option>
                ))}
              </ValidatedField>
            )}

            <div className="d-flex justify-content-end mt-4 gap-2">
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/grade"
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
                {isNew ? 'Crear Ficha' : 'Guardar Cambios'}
              </Button>
            </div>
          </ValidatedForm>
        )}
      </div>
    </div>
  );
};

export default GradeUpdate;

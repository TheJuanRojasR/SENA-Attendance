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
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.grade.home.createOrEditLabel" data-cy="GradeCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.grade.home.createOrEditLabel">Create or edit a Grade</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="grade-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.grade.code')}
                id="grade-code"
                name="code"
                data-cy="code"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 20, message: translate('entity.validation.maxlength', { max: 20 }) },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.grade.state')}
                id="grade-state"
                name="state"
                data-cy="state"
                type="select"
              >
                {stateGradeValues.map(stateGrade => (
                  <option value={stateGrade} key={stateGrade}>
                    {translate(`senaAttendanceApp.StateGrade.${stateGrade}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('senaAttendanceApp.grade.startDate')}
                id="grade-startDate"
                name="startDate"
                data-cy="startDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.grade.endDate')}
                id="grade-endDate"
                name="endDate"
                data-cy="endDate"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="grade-program"
                name="program"
                data-cy="program"
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
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <ValidatedField
                id="grade-modality"
                name="modality"
                data-cy="modality"
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
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <ValidatedField
                id="grade-timeSlot"
                name="timeSlot"
                data-cy="timeSlot"
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
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/grade" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default GradeUpdate;

import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getClassSections } from 'app/entities/class-section/class-section.reducer';
import { getEntities as getTrimesters } from 'app/entities/trimester/trimester.reducer';
import { DayOfWeek } from 'app/shared/model/enumerations/day-of-week.model';

import { createEntity, getEntity, reset, updateEntity } from './class-schedule.reducer';

export const ClassScheduleUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const trimesters = useAppSelector(state => state.trimester.entities);
  const classSections = useAppSelector(state => state.classSection.entities);
  const classScheduleEntity = useAppSelector(state => state.classSchedule.entity);
  const loading = useAppSelector(state => state.classSchedule.loading);
  const updating = useAppSelector(state => state.classSchedule.updating);
  const updateSuccess = useAppSelector(state => state.classSchedule.updateSuccess);
  const dayOfWeekValues = Object.keys(DayOfWeek);

  const handleClose = () => {
    navigate(`/class-schedule${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getTrimesters({}));
    dispatch(getClassSections({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...classScheduleEntity,
      ...values,
      trimester: trimesters.find(it => it.id.toString() === values.trimester?.toString()),
      classSection: classSections.find(it => it.id.toString() === values.classSection?.toString()),
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
          dayOfWeek: 'LUNES',
          ...classScheduleEntity,
          trimester: classScheduleEntity?.trimester?.id,
          classSection: classScheduleEntity?.classSection?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.classSchedule.home.createOrEditLabel" data-cy="ClassScheduleCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.classSchedule.home.createOrEditLabel">Create or edit a ClassSchedule</Translate>
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
                  id="class-schedule-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.classSchedule.dayOfWeek')}
                id="class-schedule-dayOfWeek"
                name="dayOfWeek"
                data-cy="dayOfWeek"
                type="select"
              >
                {dayOfWeekValues.map(dayOfWeek => (
                  <option value={dayOfWeek} key={dayOfWeek}>
                    {translate(`senaAttendanceApp.DayOfWeek.${dayOfWeek}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('senaAttendanceApp.classSchedule.startTime')}
                id="class-schedule-startTime"
                name="startTime"
                data-cy="startTime"
                type="time"
                placeholder="HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.classSchedule.endTime')}
                id="class-schedule-endTime"
                name="endTime"
                data-cy="endTime"
                type="time"
                placeholder="HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="class-schedule-trimester"
                name="trimester"
                data-cy="trimester"
                label={translate('senaAttendanceApp.classSchedule.trimester')}
                type="select"
                required
              >
                <option value="" key="0" />
                {trimesters
                  ? trimesters.map(otherEntity => (
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
                id="class-schedule-classSection"
                name="classSection"
                data-cy="classSection"
                label={translate('senaAttendanceApp.classSchedule.classSection')}
                type="select"
                required
              >
                <option value="" key="0" />
                {classSections
                  ? classSections.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.subjectName}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/class-schedule" replace variant="info">
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

export default ClassScheduleUpdate;

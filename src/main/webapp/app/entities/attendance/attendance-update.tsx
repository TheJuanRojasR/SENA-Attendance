import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getClassSections } from 'app/entities/class-section/class-section.reducer';
import { getEntities as getJustifications } from 'app/entities/justification/justification.reducer';
import { getEntities as getUserProfiles } from 'app/entities/user-profile/user-profile.reducer';
import { StateAttendance } from 'app/shared/model/enumerations/state-attendance.model';

import { createEntity, getEntity, reset, updateEntity } from './attendance.reducer';

export const AttendanceUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const classSections = useAppSelector(state => state.classSection.entities);
  const userProfiles = useAppSelector(state => state.userProfile.entities);
  const justifications = useAppSelector(state => state.justification.entities);
  const attendanceEntity = useAppSelector(state => state.attendance.entity);
  const loading = useAppSelector(state => state.attendance.loading);
  const updating = useAppSelector(state => state.attendance.updating);
  const updateSuccess = useAppSelector(state => state.attendance.updateSuccess);
  const stateAttendanceValues = Object.keys(StateAttendance);

  const handleClose = () => {
    navigate(`/attendance${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getClassSections({}));
    dispatch(getUserProfiles({}));
    dispatch(getJustifications({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...attendanceEntity,
      ...values,
      classSection: classSections.find(it => it.id.toString() === values.classSection?.toString()),
      student: userProfiles.find(it => it.id.toString() === values.student?.toString()),
      modifiedByJustification: justifications.find(it => it.id.toString() === values.modifiedByJustification?.toString()),
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
          stateAttendance: 'PRESENTE',
          ...attendanceEntity,
          classSection: attendanceEntity?.classSection?.id,
          student: attendanceEntity?.student?.id,
          modifiedByJustification: attendanceEntity?.modifiedByJustification?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.attendance.home.createOrEditLabel" data-cy="AttendanceCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.attendance.home.createOrEditLabel">Create or edit a Attendance</Translate>
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
                  id="attendance-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.attendance.date')}
                id="attendance-date"
                name="date"
                data-cy="date"
                type="date"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.attendance.stateAttendance')}
                id="attendance-stateAttendance"
                name="stateAttendance"
                data-cy="stateAttendance"
                type="select"
              >
                {stateAttendanceValues.map(stateAttendance => (
                  <option value={stateAttendance} key={stateAttendance}>
                    {translate(`senaAttendanceApp.StateAttendance.${stateAttendance}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="attendance-classSection"
                name="classSection"
                data-cy="classSection"
                label={translate('senaAttendanceApp.attendance.classSection')}
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
              <ValidatedField
                id="attendance-student"
                name="student"
                data-cy="student"
                label={translate('senaAttendanceApp.attendance.student')}
                type="select"
                required
              >
                <option value="" key="0" />
                {userProfiles
                  ? userProfiles.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.documentNumber}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <ValidatedField
                id="attendance-modifiedByJustification"
                name="modifiedByJustification"
                data-cy="modifiedByJustification"
                label={translate('senaAttendanceApp.attendance.modifiedByJustification')}
                type="select"
              >
                <option value="" key="0" />
                {justifications
                  ? justifications.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/attendance" replace variant="info">
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

export default AttendanceUpdate;

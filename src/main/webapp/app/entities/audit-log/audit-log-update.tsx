import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getAttendances } from 'app/entities/attendance/attendance.reducer';
import { getEntities as getUserProfiles } from 'app/entities/user-profile/user-profile.reducer';
import { StateAttendance } from 'app/shared/model/enumerations/state-attendance.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './audit-log.reducer';

export const AuditLogUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const userProfiles = useAppSelector(state => state.userProfile.entities);
  const attendances = useAppSelector(state => state.attendance.entities);
  const auditLogEntity = useAppSelector(state => state.auditLog.entity);
  const loading = useAppSelector(state => state.auditLog.loading);
  const updating = useAppSelector(state => state.auditLog.updating);
  const updateSuccess = useAppSelector(state => state.auditLog.updateSuccess);
  const stateAttendanceValues = Object.keys(StateAttendance);

  const handleClose = () => {
    navigate(`/audit-log${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUserProfiles({}));
    dispatch(getAttendances({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.editDate = convertDateTimeToServer(values.editDate);

    const entity = {
      ...auditLogEntity,
      ...values,
      modifiedBy: userProfiles.find(it => it.id.toString() === values.modifiedBy?.toString()),
      attendance: attendances.find(it => it.id.toString() === values.attendance?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          editDate: displayDefaultDateTime(),
        }
      : {
          previousState: 'PRESENTE',
          newState: 'PRESENTE',
          ...auditLogEntity,
          editDate: convertDateTimeFromServer(auditLogEntity.editDate),
          modifiedBy: auditLogEntity?.modifiedBy?.id,
          attendance: auditLogEntity?.attendance?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.auditLog.home.createOrEditLabel" data-cy="AuditLogCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.auditLog.home.createOrEditLabel">Create or edit a AuditLog</Translate>
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
                  id="audit-log-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.auditLog.previousState')}
                id="audit-log-previousState"
                name="previousState"
                data-cy="previousState"
                type="select"
              >
                {stateAttendanceValues.map(stateAttendance => (
                  <option value={stateAttendance} key={stateAttendance}>
                    {translate(`senaAttendanceApp.StateAttendance.${stateAttendance}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('senaAttendanceApp.auditLog.newState')}
                id="audit-log-newState"
                name="newState"
                data-cy="newState"
                type="select"
              >
                {stateAttendanceValues.map(stateAttendance => (
                  <option value={stateAttendance} key={stateAttendance}>
                    {translate(`senaAttendanceApp.StateAttendance.${stateAttendance}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('senaAttendanceApp.auditLog.editDate')}
                id="audit-log-editDate"
                name="editDate"
                data-cy="editDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="audit-log-modifiedBy"
                name="modifiedBy"
                data-cy="modifiedBy"
                label={translate('senaAttendanceApp.auditLog.modifiedBy')}
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
                id="audit-log-attendance"
                name="attendance"
                data-cy="attendance"
                label={translate('senaAttendanceApp.auditLog.attendance')}
                type="select"
                required
              >
                <option value="" key="0" />
                {attendances
                  ? attendances.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.date}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/audit-log" replace variant="info">
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

export default AuditLogUpdate;

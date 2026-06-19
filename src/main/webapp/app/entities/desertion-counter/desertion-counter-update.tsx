import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getTrimesters } from 'app/entities/trimester/trimester.reducer';
import { getEntities as getUserProfiles } from 'app/entities/user-profile/user-profile.reducer';

import { createEntity, getEntity, reset, updateEntity } from './desertion-counter.reducer';

export const DesertionCounterUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const userProfiles = useAppSelector(state => state.userProfile.entities);
  const trimesters = useAppSelector(state => state.trimester.entities);
  const desertionCounterEntity = useAppSelector(state => state.desertionCounter.entity);
  const loading = useAppSelector(state => state.desertionCounter.loading);
  const updating = useAppSelector(state => state.desertionCounter.updating);
  const updateSuccess = useAppSelector(state => state.desertionCounter.updateSuccess);

  const handleClose = () => {
    navigate(`/desertion-counter${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUserProfiles({}));
    dispatch(getTrimesters({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.totalGlobalAbsences !== undefined && typeof values.totalGlobalAbsences !== 'number') {
      values.totalGlobalAbsences = Number(values.totalGlobalAbsences);
    }
    if (values.accumulatedLateArrivals !== undefined && typeof values.accumulatedLateArrivals !== 'number') {
      values.accumulatedLateArrivals = Number(values.accumulatedLateArrivals);
    }
    if (values.workJustificationsUsed !== undefined && typeof values.workJustificationsUsed !== 'number') {
      values.workJustificationsUsed = Number(values.workJustificationsUsed);
    }

    const entity = {
      ...desertionCounterEntity,
      ...values,
      student: userProfiles.find(it => it.id.toString() === values.student?.toString()),
      trimester: trimesters.find(it => it.id.toString() === values.trimester?.toString()),
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
          ...desertionCounterEntity,
          student: desertionCounterEntity?.student?.id,
          trimester: desertionCounterEntity?.trimester?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.desertionCounter.home.createOrEditLabel" data-cy="DesertionCounterCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.desertionCounter.home.createOrEditLabel">Create or edit a DesertionCounter</Translate>
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
                  id="desertion-counter-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.desertionCounter.totalGlobalAbsences')}
                id="desertion-counter-totalGlobalAbsences"
                name="totalGlobalAbsences"
                data-cy="totalGlobalAbsences"
                type="text"
              />
              <ValidatedField
                label={translate('senaAttendanceApp.desertionCounter.accumulatedLateArrivals')}
                id="desertion-counter-accumulatedLateArrivals"
                name="accumulatedLateArrivals"
                data-cy="accumulatedLateArrivals"
                type="text"
              />
              <ValidatedField
                label={translate('senaAttendanceApp.desertionCounter.workJustificationsUsed')}
                id="desertion-counter-workJustificationsUsed"
                name="workJustificationsUsed"
                data-cy="workJustificationsUsed"
                type="text"
              />
              <ValidatedField
                label={translate('senaAttendanceApp.desertionCounter.alertsSent')}
                id="desertion-counter-alertsSent"
                name="alertsSent"
                data-cy="alertsSent"
                check
                type="checkbox"
              />
              <ValidatedField
                id="desertion-counter-student"
                name="student"
                data-cy="student"
                label={translate('senaAttendanceApp.desertionCounter.student')}
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
                id="desertion-counter-trimester"
                name="trimester"
                data-cy="trimester"
                label={translate('senaAttendanceApp.desertionCounter.trimester')}
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/desertion-counter" replace variant="info">
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

export default DesertionCounterUpdate;

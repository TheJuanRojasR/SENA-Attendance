import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getGrades } from 'app/entities/grade/grade.reducer';
import { getEntities as getUserProfiles } from 'app/entities/user-profile/user-profile.reducer';
import { StateAcademic } from 'app/shared/model/enumerations/state-academic.model';

import { createEntity, getEntity, reset, updateEntity } from './apprentice.reducer';

export const ApprenticeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const userProfiles = useAppSelector(state => state.userProfile.entities);
  const grades = useAppSelector(state => state.grade.entities);
  const apprenticeEntity = useAppSelector(state => state.apprentice.entity);
  const loading = useAppSelector(state => state.apprentice.loading);
  const updating = useAppSelector(state => state.apprentice.updating);
  const updateSuccess = useAppSelector(state => state.apprentice.updateSuccess);
  const stateAcademicValues = Object.keys(StateAcademic);

  const handleClose = () => {
    navigate(`/apprentice${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUserProfiles({}));
    dispatch(getGrades({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...apprenticeEntity,
      ...values,
      student: userProfiles.find(it => it.id.toString() === values.student?.toString()),
      grade: grades.find(it => it.id.toString() === values.grade?.toString()),
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
          stateAcademic: 'RETIRO_VOLUNTARIO',
          ...apprenticeEntity,
          student: apprenticeEntity?.student?.id,
          grade: apprenticeEntity?.grade?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.apprentice.home.createOrEditLabel" data-cy="ApprenticeCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.apprentice.home.createOrEditLabel">Create or edit a Apprentice</Translate>
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
                  id="apprentice-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.apprentice.stateAcademic')}
                id="apprentice-stateAcademic"
                name="stateAcademic"
                data-cy="stateAcademic"
                type="select"
              >
                {stateAcademicValues.map(stateAcademic => (
                  <option value={stateAcademic} key={stateAcademic}>
                    {translate(`senaAttendanceApp.StateAcademic.${stateAcademic}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="apprentice-student"
                name="student"
                data-cy="student"
                label={translate('senaAttendanceApp.apprentice.student')}
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
                id="apprentice-grade"
                name="grade"
                data-cy="grade"
                label={translate('senaAttendanceApp.apprentice.grade')}
                type="select"
                required
              >
                <option value="" key="0" />
                {grades
                  ? grades.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.code}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/apprentice" replace variant="info">
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

export default ApprenticeUpdate;

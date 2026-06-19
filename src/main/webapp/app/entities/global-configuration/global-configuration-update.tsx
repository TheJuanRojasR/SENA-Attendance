import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './global-configuration.reducer';

export const GlobalConfigurationUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const globalConfigurationEntity = useAppSelector(state => state.globalConfiguration.entity);
  const loading = useAppSelector(state => state.globalConfiguration.loading);
  const updating = useAppSelector(state => state.globalConfiguration.updating);
  const updateSuccess = useAppSelector(state => state.globalConfiguration.updateSuccess);

  const handleClose = () => {
    navigate('/global-configuration');
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
    if (values.studentJustificationDays !== undefined && typeof values.studentJustificationDays !== 'number') {
      values.studentJustificationDays = Number(values.studentJustificationDays);
    }
    if (values.instructorResponseDays !== undefined && typeof values.instructorResponseDays !== 'number') {
      values.instructorResponseDays = Number(values.instructorResponseDays);
    }
    if (values.lateArrivalsToFail !== undefined && typeof values.lateArrivalsToFail !== 'number') {
      values.lateArrivalsToFail = Number(values.lateArrivalsToFail);
    }
    if (values.maxPostponementJustifications !== undefined && typeof values.maxPostponementJustifications !== 'number') {
      values.maxPostponementJustifications = Number(values.maxPostponementJustifications);
    }
    if (values.standardTrimesterMonths !== undefined && typeof values.standardTrimesterMonths !== 'number') {
      values.standardTrimesterMonths = Number(values.standardTrimesterMonths);
    }

    const entity = {
      ...globalConfigurationEntity,
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
          ...globalConfigurationEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.globalConfiguration.home.createOrEditLabel" data-cy="GlobalConfigurationCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.globalConfiguration.home.createOrEditLabel">
              Create or edit a GlobalConfiguration
            </Translate>
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
                  id="global-configuration-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.globalConfiguration.studentJustificationDays')}
                id="global-configuration-studentJustificationDays"
                name="studentJustificationDays"
                data-cy="studentJustificationDays"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.globalConfiguration.instructorResponseDays')}
                id="global-configuration-instructorResponseDays"
                name="instructorResponseDays"
                data-cy="instructorResponseDays"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.globalConfiguration.lateArrivalsToFail')}
                id="global-configuration-lateArrivalsToFail"
                name="lateArrivalsToFail"
                data-cy="lateArrivalsToFail"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.globalConfiguration.maxPostponementJustifications')}
                id="global-configuration-maxPostponementJustifications"
                name="maxPostponementJustifications"
                data-cy="maxPostponementJustifications"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.globalConfiguration.standardTrimesterMonths')}
                id="global-configuration-standardTrimesterMonths"
                name="standardTrimesterMonths"
                data-cy="standardTrimesterMonths"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/global-configuration"
                replace
                variant="info"
              >
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

export default GlobalConfigurationUpdate;

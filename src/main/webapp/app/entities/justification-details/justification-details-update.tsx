import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedBlobField, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getClassSections } from 'app/entities/class-section/class-section.reducer';
import { getEntities as getJustifications } from 'app/entities/justification/justification.reducer';
import { StateJustification } from 'app/shared/model/enumerations/state-justification.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './justification-details.reducer';

export const JustificationDetailsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const classSections = useAppSelector(state => state.classSection.entities);
  const justifications = useAppSelector(state => state.justification.entities);
  const justificationDetailsEntity = useAppSelector(state => state.justificationDetails.entity);
  const loading = useAppSelector(state => state.justificationDetails.loading);
  const updating = useAppSelector(state => state.justificationDetails.updating);
  const updateSuccess = useAppSelector(state => state.justificationDetails.updateSuccess);
  const stateJustificationValues = Object.keys(StateJustification);

  const handleClose = () => {
    navigate(`/justification-details${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getClassSections({}));
    dispatch(getJustifications({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.responseDate = convertDateTimeToServer(values.responseDate);

    const entity = {
      ...justificationDetailsEntity,
      ...values,
      classSection: classSections.find(it => it.id.toString() === values.classSection?.toString()),
      justification: justifications.find(it => it.id.toString() === values.justification?.toString()),
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
          responseDate: displayDefaultDateTime(),
        }
      : {
          stateJustification: 'ACEPTADA',
          ...justificationDetailsEntity,
          responseDate: convertDateTimeFromServer(justificationDetailsEntity.responseDate),
          classSection: justificationDetailsEntity?.classSection?.id,
          justification: justificationDetailsEntity?.justification?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.justificationDetails.home.createOrEditLabel" data-cy="JustificationDetailsCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.justificationDetails.home.createOrEditLabel">
              Create or edit a JustificationDetails
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
                  id="justification-details-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.justificationDetails.stateJustification')}
                id="justification-details-stateJustification"
                name="stateJustification"
                data-cy="stateJustification"
                type="select"
              >
                {stateJustificationValues.map(stateJustification => (
                  <option value={stateJustification} key={stateJustification}>
                    {translate(`senaAttendanceApp.StateJustification.${stateJustification}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('senaAttendanceApp.justificationDetails.rejectionReason')}
                id="justification-details-rejectionReason"
                name="rejectionReason"
                data-cy="rejectionReason"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 300, message: translate('entity.validation.maxlength', { max: 300 }) },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.justificationDetails.correctionText')}
                id="justification-details-correctionText"
                name="correctionText"
                data-cy="correctionText"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 300, message: translate('entity.validation.maxlength', { max: 300 }) },
                }}
              />
              <ValidatedBlobField
                label={translate('senaAttendanceApp.justificationDetails.correctionFileUrl')}
                id="justification-details-correctionFileUrl"
                name="correctionFileUrl"
                data-cy="correctionFileUrl"
                openActionLabel={translate('entity.action.open')}
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.justificationDetails.responseDate')}
                id="justification-details-responseDate"
                name="responseDate"
                data-cy="responseDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="justification-details-classSection"
                name="classSection"
                data-cy="classSection"
                label={translate('senaAttendanceApp.justificationDetails.classSection')}
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
                id="justification-details-justification"
                name="justification"
                data-cy="justification"
                label={translate('senaAttendanceApp.justificationDetails.justification')}
                type="select"
                required
              >
                <option value="" key="0" />
                {justifications
                  ? justifications.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.description}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>
                <Translate contentKey="entity.validation.required">This field is required.</Translate>
              </FormText>
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to="/justification-details"
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

export default JustificationDetailsUpdate;

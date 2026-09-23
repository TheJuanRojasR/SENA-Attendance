import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getClassSections, getMine as getMyClassSections } from 'app/entities/class-section/class-section.reducer';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

import { createEntity, getEntity, reset, updateEntity } from './class-exception.reducer';

export const ClassExceptionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  // UC015-item10: las excepciones no lectivas las gestiona el Admin (cualquier materia, vía el
  // listado genérico /api/class-sections) o el instructor asignado (solo las suyas, vía /mine —
  // el genérico le responde 403). El origen del selector depende de qué rol está autenticado.
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.ADMIN]));
  const adminClassSections = useAppSelector(state => state.classSection.entities);
  const myClassSections = useAppSelector(state => state.classSection.mine);
  const classSections = isAdmin ? adminClassSections : myClassSections;

  const classSectionIdParam = new URLSearchParams(location.search).get('classSectionId');

  const classExceptionEntity = useAppSelector(state => state.classException.entity);
  const loading = useAppSelector(state => state.classException.loading);
  const updating = useAppSelector(state => state.classException.updating);
  const updateSuccess = useAppSelector(state => state.classException.updateSuccess);

  // UC009-A4: una fecha no lectiva pasada es un precedente; el backend solo permite
  // cambiar el motivo (error.pastExceptionLocked si se intenta mover la fecha o la materia).
  const todayIso = new Date().toISOString().slice(0, 10);
  const isPastException = !isNew && classExceptionEntity?.date != null && `${classExceptionEntity.date}` < todayIso;

  const handleClose = () => {
    if (classSectionIdParam) {
      navigate(`/attendance/session`);
    } else {
      navigate(`/class-exception${location.search}`);
    }
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    if (isAdmin) {
      dispatch(getClassSections({}));
    } else {
      dispatch(getMyClassSections());
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...classExceptionEntity,
      ...values,
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
      ? { classSection: classSectionIdParam ?? '' }
      : {
          ...classExceptionEntity,
          classSection: classExceptionEntity?.classSection?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.classException.home.createOrEditLabel" data-cy="ClassExceptionCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.classException.home.createOrEditLabel">Create or edit a ClassException</Translate>
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
                  id="class-exception-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.classException.date')}
                id="class-exception-date"
                name="date"
                data-cy="date"
                type="date"
                disabled={isPastException}
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              {isPastException && <FormText>{translate('error.pastExceptionLocked')}</FormText>}
              <ValidatedField
                label={translate('senaAttendanceApp.classException.reason')}
                id="class-exception-reason"
                name="reason"
                data-cy="reason"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 200, message: translate('entity.validation.maxlength', { max: 200 }) },
                }}
              />
              <ValidatedField
                id="class-exception-classSection"
                name="classSection"
                data-cy="classSection"
                label={translate('senaAttendanceApp.classException.classSection')}
                type="select"
                disabled={isPastException || !!classSectionIdParam}
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
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to={classSectionIdParam ? '/attendance/session' : '/class-exception'}
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

export default ClassExceptionUpdate;

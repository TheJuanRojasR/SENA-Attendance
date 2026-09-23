import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getGrades } from 'app/entities/grade/grade.reducer';

import { enrollApprentice, reset } from './apprentice.reducer';

// UC008: esta pantalla solo vincula (no existe edición: no hay PUT/PATCH genérico para
// Apprentice). El aprendiz se identifica por documento, no por su perfil.
export const ApprenticeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const grades = useAppSelector(state => state.grade.entities);
  const updating = useAppSelector(state => state.apprentice.updating);
  const updateSuccess = useAppSelector(state => state.apprentice.updateSuccess);

  const handleClose = () => {
    navigate('/apprentice');
  };

  useEffect(() => {
    dispatch(reset());
    dispatch(getGrades({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    dispatch(enrollApprentice({ documentNumber: values.documentNumber, gradeId: values.grade }));
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.apprentice.home.createOrEditLabel" data-cy="ApprenticeCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.apprentice.home.createOrEditLabel">Vincular aprendiz</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          <ValidatedForm onSubmit={saveEntity}>
            <ValidatedField
              label={translate('senaAttendanceApp.apprentice.documentNumber')}
              id="apprentice-documentNumber"
              name="documentNumber"
              data-cy="documentNumber"
              type="text"
              validate={{
                required: { value: true, message: translate('entity.validation.required') },
                maxLength: { value: 30, message: translate('entity.validation.maxlength', { max: 30 }) },
                validate: v => /^\d+$/.test(v) || translate('entity.validation.number'),
              }}
            />
            <FormText>Número de documento del aprendiz ya registrado (UC001). Solo dígitos.</FormText>
            <ValidatedField
              id="apprentice-grade"
              name="grade"
              data-cy="grade"
              label={translate('senaAttendanceApp.apprentice.grade')}
              type="select"
              required
              validate={{ required: { value: true, message: translate('entity.validation.required') } }}
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
        </Col>
      </Row>
    </div>
  );
};

export default ApprenticeUpdate;

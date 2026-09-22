import React, { useEffect } from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, partialUpdateEntity } from './modality.reducer';

export const ModalityUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const modalityEntity = useAppSelector(state => state.modality.entity);
  const loading = useAppSelector(state => state.modality.loading);
  const updating = useAppSelector(state => state.modality.updating);
  const updateSuccess = useAppSelector(state => state.modality.updateSuccess);

  const handleClose = () => {
    navigate('/modality');
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
    const entity = {
      ...modalityEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(partialUpdateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...modalityEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="12">
          {!isNew && (
            <h2 id="senaAttendanceApp.modality.home.createOrEditLabel" data-cy="ModalityCreateUpdateHeading">
              Editar Modalidad
            </h2>
          )}
          {isNew && (
            <div>
              <h2 id="senaAttendanceApp.modality.home.createOrEditLabel" data-cy="ModalityCreateUpdateHeading">
                Crear Modalidad
              </h2>
              <p>Complete el siguiente formulario para registrar una nueva modalidad formativa instucional en el centro de formacion</p>
            </div>
          )}
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="12">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <Card className="top-border-card">
              <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
                <ValidatedField
                  label={translate('senaAttendanceApp.modality.name')}
                  id="modality-name"
                  name="name"
                  data-cy="name"
                  type="text"
                  validate={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 50, message: translate('entity.validation.maxlength', { max: 50 }) },
                  }}
                />
                {!isNew && (
                  <ValidatedField
                    label={translate('senaAttendanceApp.modality.isActive')}
                    id="modality-isActive"
                    name="isActive"
                    data-cy="isActive"
                    check
                    type="checkbox"
                  />
                )}
                <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/modality" replace variant="info">
                  <FontAwesomeIcon icon="arrow-left" />
                  &nbsp;
                  <span className="d-none d-md-inline">Canelar</span>
                </Button>
                &nbsp;
                <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                  <FontAwesomeIcon icon="save" />
                  &nbsp;
                  <Translate contentKey="entity.action.save">Save</Translate>
                </Button>
              </ValidatedForm>
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ModalityUpdate;

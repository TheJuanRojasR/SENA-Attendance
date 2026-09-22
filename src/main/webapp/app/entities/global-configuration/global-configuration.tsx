import './global-configuration.scss';

import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { type FieldError, useForm } from 'react-hook-form';
import { Translate, ValidatedInput } from 'react-jhipster';
import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getConfigurations, partialUpdateEntity } from './global-configuration.reducer';

export const GlobalConfiguration = () => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useAppDispatch();

  const entity = useAppSelector(state => state.globalConfiguration.entity);
  const updating = useAppSelector(state => state.globalConfiguration.updating);
  const updateSuccess = useAppSelector(state => state.globalConfiguration.updateSuccess);

  const {
    handleSubmit,
    register,
    reset: resetForm,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched', defaultValues: entity });

  useEffect(() => {
    dispatch(getConfigurations());
  }, []);

  useEffect(() => {
    if (entity) {
      resetForm(entity);
    }
  }, [entity, resetForm]);

  useEffect(() => {
    if (updateSuccess) {
      toast.success('Configuración actualizada correctamente');
    }
  }, [updateSuccess]);

  const handleValidSubmit = values => {
    dispatch(partialUpdateEntity(values));
  };
  const handleEditClick = () => setIsEditing(true);
  const handleCancelClick = () => setIsEditing(false);

  const dayRangeValidation = (label: string) => ({
    required: { value: true, message: `${label} es obligatorio` },
    valueAsNumber: true,
    min: { value: 1, message: `${label} debe ser mayor o igual a 1` },
    max: { value: 30, message: `${label} no puede ser mayor a 30` },
    validate: (v: number) => Number.isInteger(v) || `${label} debe ser un número entero`,
  });

  const thresholdValidation = (label: string) => ({
    required: { value: true, message: `${label} es obligatorio` },
    valueAsNumber: true,
    min: { value: 1, message: `${label} debe ser mayor o igual a 1` },
    validate: (v: number) => Number.isInteger(v) || `${label} debe ser un número entero`,
  });

  return (
    <div>
      <Row>
        <h2 id="global-configuration-heading" data-cy="GlobalConfigurationHeading">
          <Translate contentKey="senaAttendanceApp.globalConfiguration.home.title">Global Configurations</Translate>
        </h2>
        <p>Gestiona los parámetros y umbrales operativos del sistema de control de asistencia institucional.</p>
      </Row>
      <Card className="top-border-card">
        <Form className="d-flex flex-column" onSubmit={handleSubmit(handleValidSubmit)}>
          <Col md="12">
            <h6 className="formTitles">Tiempos y Plazos de Justificación</h6>
            <p>Defina la vigencia legal y los tiempos hábiles para la radicacion y respuesta institucional.</p>
          </Col>
          <Col md="12" className="formbody">
            <div>
              <p>Dias para Justificar</p>
              <span> Días que tiene el aprendiz para presentar una justificación tras una inasistencia </span>
              <ValidatedInput
                name="studentJustificationDays"
                type="text"
                disabled={!isEditing}
                data-cy="studentJustificationDays"
                register={register}
                error={errors.studentJustificationDays as FieldError}
                isTouched={touchedFields.studentJustificationDays}
                validate={dayRangeValidation('Los días para justificar')}
              />
            </div>
            <div>
              <p> Dias de Respuesta del Instructor </p>
              <span> Plazo máximo para que el instructor revise y valide o rechace la justificación </span>
              <ValidatedInput
                name="instructorResponseDays"
                type="text"
                disabled={!isEditing}
                data-cy="instructorResponseDays"
                register={register}
                error={errors.instructorResponseDays as FieldError}
                isTouched={touchedFields.instructorResponseDays}
                validate={dayRangeValidation('Los días de respuesta del instructor')}
              />
            </div>
          </Col>
          <Col md="12">
            <h6 className="formTitles"> Umbrales y Alestras de Inasistencias</h6>
            <p>Cantidad de inasistencias continuas que disparan reporte preventivo y alerta</p>
          </Col>
          <Col md="12" className="formbody">
            <div>
              <h6>Umbral de fallas consecutivas</h6>
              <span> Cantidad de insasistencias continuas que disparan reporte preventivo y alerta</span>
              <ValidatedInput
                name="consecutiveAbsenceAlertThreshold"
                type="text"
                disabled={!isEditing}
                data-cy="consecutiveAbsenceAlertThreshold"
                register={register}
                error={errors.consecutiveAbsenceAlertThreshold as FieldError}
                isTouched={touchedFields.consecutiveAbsenceAlertThreshold}
                validate={thresholdValidation('El umbral de fallas consecutivas')}
              />
            </div>
            <div>
              <h6>Umbral de fallas acumuladas</h6>
              <span> Número máximo de fallas acumuladas en el trimestre que alertan comité o condicionalidad </span>
              <ValidatedInput
                name="accumulatedAbsenceAlertThreshold"
                type="text"
                disabled={!isEditing}
                data-cy="accumulatedAbsenceAlertThreshold"
                register={register}
                error={errors.accumulatedAbsenceAlertThreshold as FieldError}
                isTouched={touchedFields.accumulatedAbsenceAlertThreshold}
                validate={thresholdValidation('El umbral de fallas acumuladas')}
              />
            </div>
          </Col>
          <div>
            {isEditing ? (
              <div className="d-flex">
                <Button variant="primary" type="submit" disabled={updating} data-cy="submit">
                  {' '}
                  Guardar{' '}
                </Button>
                <Button variant="primary" type="button" onClick={handleCancelClick} data-cy="edit">
                  {' '}
                  Cancelar{' '}
                </Button>
              </div>
            ) : (
              <Button variant="primary" type="button" onClick={handleEditClick} data-cy="edit">
                {' '}
                Editar{' '}
              </Button>
            )}
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default GlobalConfiguration;

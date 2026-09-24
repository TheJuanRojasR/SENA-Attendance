import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getGrades, getEntity as getGrade } from 'app/entities/grade/grade.reducer';
import { getInstructors } from 'app/entities/user-profile/user-profile.reducer';

import ClassSectionSchedules from './class-section-schedules';
import { createEntity, getEntity, reset, updateEntity } from './class-section.reducer';

export const ClassSectionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  // Cuando se llega desde la pestaña "Competencias" de una ficha (grade-class-sections-tab.tsx),
  // la URL trae ?gradeId=<id>: la ficha queda fija y no se ofrece el selector genérico.
  const gradeIdParam = new URLSearchParams(location.search).get('gradeId');

  const userProfiles = useAppSelector(state => state.userProfile.entities);
  const grades = useAppSelector(state => state.grade.entities);
  const contextGrade = useAppSelector(state => state.grade.entity);
  const classSectionEntity = useAppSelector(state => state.classSection.entity);
  const loading = useAppSelector(state => state.classSection.loading);
  const updating = useAppSelector(state => state.classSection.updating);
  const updateSuccess = useAppSelector(state => state.classSection.updateSuccess);

  const handleClose = () => {
    if (gradeIdParam) {
      navigate(`/grade/${gradeIdParam}/edit?tab=competencias`);
    } else {
      navigate(`/class-section${location.search}`);
    }
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getInstructors());
    if (gradeIdParam) {
      dispatch(getGrade(gradeIdParam));
    } else {
      dispatch(getGrades({}));
    }
  }, []);

  // Al crear (no al editar), se navega al edit de la competencia recién creada en vez de volver
  // a la ficha: así el horario (UC015, paso 2 del flujo básico) queda disponible de inmediato,
  // en la misma pantalla, sin tener que reabrir "Editar" para encontrarlo.
  useEffect(() => {
    if (updateSuccess) {
      if (isNew && classSectionEntity.id) {
        navigate(`/class-section/${classSectionEntity.id}/edit${gradeIdParam ? `?gradeId=${gradeIdParam}` : ''}`, { replace: true });
      } else {
        handleClose();
      }
    }
  }, [updateSuccess]);

  // UC015: "toda materia nace vinculada a una única ficha y no puede moverse a otra". Al editar,
  // la ficha siempre se conserva tal como está persistida, sin importar qué traiga el formulario.
  const saveEntity = async values => {
    const entity = {
      ...classSectionEntity,
      ...values,
      instructor: userProfiles.find(it => it.id.toString() === values.instructor?.toString()),
      grade: isNew
        ? gradeIdParam
          ? { id: gradeIdParam }
          : grades.find(it => it.id.toString() === values.grade?.toString())
        : classSectionEntity.grade,
    };

    const resultAction = isNew ? await dispatch(createEntity(entity)) : await dispatch(updateEntity(entity));
    // UC015-E7: si el guardado falla (p.ej. el instructor fue desactivado mientras se editaba el
    // formulario), se refresca la lista de instructores para que la selección vuelva a ser válida.
    if ((isNew ? createEntity : updateEntity).rejected.match(resultAction)) {
      dispatch(getInstructors());
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...classSectionEntity,
          instructor: classSectionEntity?.instructor?.id,
          grade: classSectionEntity?.grade?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="senaAttendanceApp.classSection.home.createOrEditLabel" data-cy="ClassSectionCreateUpdateHeading">
            <Translate contentKey="senaAttendanceApp.classSection.home.createOrEditLabel">Create or edit a ClassSection</Translate>
          </h2>
          {gradeIdParam && (
            <p>
              {contextGrade?.code ? `Ficha #${contextGrade.code}` : ''}
              {' — '}
              <Link to={`/grade/${gradeIdParam}/edit?tab=competencias`}>Volver a Detalles de la Ficha</Link>
            </p>
          )}
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
                  id="class-section-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              )}
              <ValidatedField
                label={translate('senaAttendanceApp.classSection.subjectName')}
                id="class-section-subjectName"
                name="subjectName"
                data-cy="subjectName"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 200, message: translate('entity.validation.maxlength', { max: 200 }) },
                }}
              />
              <ValidatedField
                label={translate('senaAttendanceApp.classSection.isActive')}
                id="class-section-isActive"
                name="isActive"
                data-cy="isActive"
                check
                type="checkbox"
              />
              <ValidatedField
                id="class-section-instructor"
                name="instructor"
                data-cy="instructor"
                label={translate('senaAttendanceApp.classSection.instructor')}
                type="select"
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
              <FormText>Instructor opcional: la materia puede crearse sin instructor y asignarlo después.</FormText>
              {isNew && !gradeIdParam && (
                <>
                  <ValidatedField
                    id="class-section-grade"
                    name="grade"
                    data-cy="grade"
                    label={translate('senaAttendanceApp.classSection.grade')}
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
                </>
              )}
              {!isNew && (
                <p className="text-muted">
                  Ficha: {classSectionEntity.grade?.code} (una materia no puede moverse a otra ficha una vez creada).
                </p>
              )}
              <Button
                as={Link as any}
                id="cancel-save"
                data-cy="entityCreateCancelButton"
                to={gradeIdParam ? `/grade/${gradeIdParam}/edit?tab=competencias` : '/class-section'}
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
          {!isNew && classSectionEntity.id && (
            <ClassSectionSchedules classSectionId={classSectionEntity.id} timeSlot={gradeIdParam ? contextGrade?.timeSlot : undefined} />
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ClassSectionUpdate;

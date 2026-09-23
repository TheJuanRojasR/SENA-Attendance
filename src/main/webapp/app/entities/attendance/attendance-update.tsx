import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity, reset, updateAttendanceState } from './attendance.reducer';

// UC009-A2: edición puntual de un registro ya guardado. No existe PUT/DELETE genérico
// (405): lo único editable es el estado, y solo entre PRESENTE y FALLA.
export const AttendanceUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();

  const attendanceEntity = useAppSelector(state => state.attendance.entity);
  const loading = useAppSelector(state => state.attendance.loading);
  const updating = useAppSelector(state => state.attendance.updating);
  const updateSuccess = useAppSelector(state => state.attendance.updateSuccess);

  const handleClose = () => {
    navigate('/attendance');
  };

  useEffect(() => {
    dispatch(getEntity(id!));
    return () => {
      dispatch(reset());
    };
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const setState = (stateAttendance: 'PRESENTE' | 'FALLA') => {
    dispatch(updateAttendanceState({ id: id!, stateAttendance }));
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 data-cy="AttendanceCreateUpdateHeading">Editar asistencia</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <>
              <dl className="jh-entity-details">
                <dt>
                  <Translate contentKey="senaAttendanceApp.attendance.date">Date</Translate>
                </dt>
                <dd>{attendanceEntity.date ? attendanceEntity.date.toString() : ''}</dd>
                <dt>
                  <Translate contentKey="senaAttendanceApp.attendance.classSection">Class Section</Translate>
                </dt>
                <dd>{attendanceEntity.classSection?.subjectName}</dd>
                <dt>
                  <Translate contentKey="senaAttendanceApp.attendance.student">Student</Translate>
                </dt>
                <dd>
                  {attendanceEntity.student?.firstName} {attendanceEntity.student?.firstLastName}
                </dd>
                <dt>
                  <Translate contentKey="senaAttendanceApp.attendance.stateAttendance">State</Translate>
                </dt>
                <dd>{attendanceEntity.stateAttendance}</dd>
              </dl>
              <div className="d-flex gap-2 mb-3">
                <Button
                  variant="success"
                  disabled={updating || attendanceEntity.stateAttendance === 'PRESENTE'}
                  onClick={() => setState('PRESENTE')}
                >
                  Marcar Asistió
                </Button>
                <Button
                  variant="danger"
                  disabled={updating || attendanceEntity.stateAttendance === 'FALLA'}
                  onClick={() => setState('FALLA')}
                >
                  Marcar Falla
                </Button>
              </div>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/attendance" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
            </>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default AttendanceUpdate;

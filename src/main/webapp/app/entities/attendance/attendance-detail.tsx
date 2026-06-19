import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './attendance.reducer';

export const AttendanceDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const attendanceEntity = useAppSelector(state => state.attendance.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="attendanceDetailsHeading">
          <Translate contentKey="senaAttendanceApp.attendance.detail.title">Attendance</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{attendanceEntity.id}</dd>
          <dt>
            <span id="date">
              <Translate contentKey="senaAttendanceApp.attendance.date">Date</Translate>
            </span>
          </dt>
          <dd>{attendanceEntity.date ? <TextFormat value={attendanceEntity.date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="stateAttendance">
              <Translate contentKey="senaAttendanceApp.attendance.stateAttendance">State Attendance</Translate>
            </span>
          </dt>
          <dd>{attendanceEntity.stateAttendance}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.attendance.classSection">Class Section</Translate>
          </dt>
          <dd>{attendanceEntity.classSection ? attendanceEntity.classSection.subjectName : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.attendance.student">Student</Translate>
          </dt>
          <dd>{attendanceEntity.student ? attendanceEntity.student.documentNumber : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.attendance.modifiedByJustification">Modified By Justification</Translate>
          </dt>
          <dd>{attendanceEntity.modifiedByJustification ? attendanceEntity.modifiedByJustification.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/attendance" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/attendance/${attendanceEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AttendanceDetail;

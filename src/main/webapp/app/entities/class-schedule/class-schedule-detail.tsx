import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './class-schedule.reducer';

export const ClassScheduleDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const classScheduleEntity = useAppSelector(state => state.classSchedule.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="classScheduleDetailsHeading">
          <Translate contentKey="senaAttendanceApp.classSchedule.detail.title">ClassSchedule</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{classScheduleEntity.id}</dd>
          <dt>
            <span id="dayOfWeek">
              <Translate contentKey="senaAttendanceApp.classSchedule.dayOfWeek">Day Of Week</Translate>
            </span>
          </dt>
          <dd>{classScheduleEntity.dayOfWeek}</dd>
          <dt>
            <span id="startTime">
              <Translate contentKey="senaAttendanceApp.classSchedule.startTime">Start Time</Translate>
            </span>
          </dt>
          <dd>{classScheduleEntity.startTime}</dd>
          <dt>
            <span id="endTime">
              <Translate contentKey="senaAttendanceApp.classSchedule.endTime">End Time</Translate>
            </span>
          </dt>
          <dd>{classScheduleEntity.endTime}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.classSchedule.trimester">Trimester</Translate>
          </dt>
          <dd>{classScheduleEntity.trimester ? classScheduleEntity.trimester.name : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.classSchedule.classSection">Class Section</Translate>
          </dt>
          <dd>{classScheduleEntity.classSection ? classScheduleEntity.classSection.subjectName : ''}</dd>
        </dl>
        <Button as={Link as any} to="/class-schedule" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/class-schedule/${classScheduleEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ClassScheduleDetail;

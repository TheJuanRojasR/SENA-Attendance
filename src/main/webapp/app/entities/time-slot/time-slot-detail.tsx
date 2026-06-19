import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './time-slot.reducer';

export const TimeSlotDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const timeSlotEntity = useAppSelector(state => state.timeSlot.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="timeSlotDetailsHeading">
          <Translate contentKey="senaAttendanceApp.timeSlot.detail.title">TimeSlot</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{timeSlotEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="senaAttendanceApp.timeSlot.name">Name</Translate>
            </span>
          </dt>
          <dd>{timeSlotEntity.name}</dd>
          <dt>
            <span id="isActive">
              <Translate contentKey="senaAttendanceApp.timeSlot.isActive">Is Active</Translate>
            </span>
          </dt>
          <dd>{timeSlotEntity.isActive ? 'true' : 'false'}</dd>
          <dt>
            <span id="startTime">
              <Translate contentKey="senaAttendanceApp.timeSlot.startTime">Start Time</Translate>
            </span>
          </dt>
          <dd>{timeSlotEntity.startTime}</dd>
          <dt>
            <span id="endTime">
              <Translate contentKey="senaAttendanceApp.timeSlot.endTime">End Time</Translate>
            </span>
          </dt>
          <dd>{timeSlotEntity.endTime}</dd>
        </dl>
        <Button as={Link as any} to="/time-slot" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/time-slot/${timeSlotEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TimeSlotDetail;

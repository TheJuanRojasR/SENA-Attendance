import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './desertion-counter.reducer';

export const DesertionCounterDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const desertionCounterEntity = useAppSelector(state => state.desertionCounter.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="desertionCounterDetailsHeading">
          <Translate contentKey="senaAttendanceApp.desertionCounter.detail.title">DesertionCounter</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{desertionCounterEntity.id}</dd>
          <dt>
            <span id="totalGlobalAbsences">
              <Translate contentKey="senaAttendanceApp.desertionCounter.totalGlobalAbsences">Total Global Absences</Translate>
            </span>
          </dt>
          <dd>{desertionCounterEntity.totalGlobalAbsences}</dd>
          <dt>
            <span id="accumulatedLateArrivals">
              <Translate contentKey="senaAttendanceApp.desertionCounter.accumulatedLateArrivals">Accumulated Late Arrivals</Translate>
            </span>
          </dt>
          <dd>{desertionCounterEntity.accumulatedLateArrivals}</dd>
          <dt>
            <span id="workJustificationsUsed">
              <Translate contentKey="senaAttendanceApp.desertionCounter.workJustificationsUsed">Work Justifications Used</Translate>
            </span>
          </dt>
          <dd>{desertionCounterEntity.workJustificationsUsed}</dd>
          <dt>
            <span id="alertsSent">
              <Translate contentKey="senaAttendanceApp.desertionCounter.alertsSent">Alerts Sent</Translate>
            </span>
          </dt>
          <dd>{desertionCounterEntity.alertsSent ? 'true' : 'false'}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.desertionCounter.student">Student</Translate>
          </dt>
          <dd>{desertionCounterEntity.student ? desertionCounterEntity.student.documentNumber : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.desertionCounter.trimester">Trimester</Translate>
          </dt>
          <dd>{desertionCounterEntity.trimester ? desertionCounterEntity.trimester.name : ''}</dd>
        </dl>
        <Button as={Link as any} to="/desertion-counter" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/desertion-counter/${desertionCounterEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default DesertionCounterDetail;

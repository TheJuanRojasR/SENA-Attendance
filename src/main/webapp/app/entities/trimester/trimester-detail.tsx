import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './trimester.reducer';

export const TrimesterDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const trimesterEntity = useAppSelector(state => state.trimester.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="trimesterDetailsHeading">
          <Translate contentKey="senaAttendanceApp.trimester.detail.title">Trimester</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{trimesterEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="senaAttendanceApp.trimester.name">Name</Translate>
            </span>
          </dt>
          <dd>{trimesterEntity.name}</dd>
          <dt>
            <span id="startDate">
              <Translate contentKey="senaAttendanceApp.trimester.startDate">Start Date</Translate>
            </span>
          </dt>
          <dd>
            {trimesterEntity.startDate ? <TextFormat value={trimesterEntity.startDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="endDate">
              <Translate contentKey="senaAttendanceApp.trimester.endDate">End Date</Translate>
            </span>
          </dt>
          <dd>
            {trimesterEntity.endDate ? <TextFormat value={trimesterEntity.endDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="state">
              <Translate contentKey="senaAttendanceApp.trimester.state">State</Translate>
            </span>
          </dt>
          <dd>{trimesterEntity.state}</dd>
        </dl>
        <Button as={Link as any} to="/trimester" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/trimester/${trimesterEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TrimesterDetail;

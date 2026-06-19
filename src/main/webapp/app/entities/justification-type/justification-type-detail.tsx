import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './justification-type.reducer';

export const JustificationTypeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const justificationTypeEntity = useAppSelector(state => state.justificationType.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="justificationTypeDetailsHeading">
          <Translate contentKey="senaAttendanceApp.justificationType.detail.title">JustificationType</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{justificationTypeEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="senaAttendanceApp.justificationType.name">Name</Translate>
            </span>
          </dt>
          <dd>{justificationTypeEntity.name}</dd>
          <dt>
            <span id="limitPerTrimester">
              <Translate contentKey="senaAttendanceApp.justificationType.limitPerTrimester">Limit Per Trimester</Translate>
            </span>
          </dt>
          <dd>{justificationTypeEntity.limitPerTrimester}</dd>
          <dt>
            <span id="state">
              <Translate contentKey="senaAttendanceApp.justificationType.state">State</Translate>
            </span>
          </dt>
          <dd>{justificationTypeEntity.state}</dd>
        </dl>
        <Button as={Link as any} to="/justification-type" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/justification-type/${justificationTypeEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default JustificationTypeDetail;

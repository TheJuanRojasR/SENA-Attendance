import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './program.reducer';

export const ProgramDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const programEntity = useAppSelector(state => state.program.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="programDetailsHeading">
          <Translate contentKey="senaAttendanceApp.program.detail.title">Program</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{programEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="senaAttendanceApp.program.name">Name</Translate>
            </span>
          </dt>
          <dd>{programEntity.name}</dd>
          <dt>
            <span id="initials">
              <Translate contentKey="senaAttendanceApp.program.initials">Initials</Translate>
            </span>
          </dt>
          <dd>{programEntity.initials}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="senaAttendanceApp.program.code">Code</Translate>
            </span>
          </dt>
          <dd>{programEntity.code}</dd>
          <dt>
            <span id="trimesters">
              <Translate contentKey="senaAttendanceApp.program.trimesters">Trimesters</Translate>
            </span>
          </dt>
          <dd>{programEntity.trimesters}</dd>
        </dl>
        <Button as={Link as any} to="/program" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/program/${programEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ProgramDetail;

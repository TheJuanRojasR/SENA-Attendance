import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './apprentice.reducer';

export const ApprenticeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const apprenticeEntity = useAppSelector(state => state.apprentice.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="apprenticeDetailsHeading">
          <Translate contentKey="senaAttendanceApp.apprentice.detail.title">Apprentice</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{apprenticeEntity.id}</dd>
          <dt>
            <span id="stateAcademic">
              <Translate contentKey="senaAttendanceApp.apprentice.stateAcademic">State Academic</Translate>
            </span>
          </dt>
          <dd>{apprenticeEntity.stateAcademic}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.apprentice.student">Student</Translate>
          </dt>
          <dd>{apprenticeEntity.student ? apprenticeEntity.student.documentNumber : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.apprentice.grade">Grade</Translate>
          </dt>
          <dd>{apprenticeEntity.grade ? apprenticeEntity.grade.code : ''}</dd>
        </dl>
        <Button as={Link as any} to="/apprentice" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/apprentice/${apprenticeEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ApprenticeDetail;

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
          <dd>
            <Translate contentKey={`senaAttendanceApp.StateAcademic.${apprenticeEntity.stateAcademic}`} />
          </dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.apprentice.student">Student</Translate>
          </dt>
          <dd>
            {apprenticeEntity.student ? (
              <Link to={`/user-profile/${apprenticeEntity.student.id}`}>
                {apprenticeEntity.student.firstName} {apprenticeEntity.student.firstLastName} ({apprenticeEntity.student.documentNumber})
              </Link>
            ) : (
              ''
            )}
          </dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.apprentice.grade">Grade</Translate>
          </dt>
          <dd>{apprenticeEntity.grade ? <Link to={`/grade/${apprenticeEntity.grade.id}`}>{apprenticeEntity.grade.code}</Link> : ''}</dd>
        </dl>
        <Button as={Link as any} to="/apprentice" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/apprentice/${apprenticeEntity.id}/unlink`} replace variant="danger">
          <FontAwesomeIcon icon="right-from-bracket" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="senaAttendanceApp.apprentice.unlink.confirm">Desvincular</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ApprenticeDetail;

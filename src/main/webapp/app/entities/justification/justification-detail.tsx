import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate, byteSize, openFile } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './justification.reducer';

export const JustificationDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const justificationEntity = useAppSelector(state => state.justification.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="justificationDetailsHeading">
          <Translate contentKey="senaAttendanceApp.justification.detail.title">Justification</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{justificationEntity.id}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="senaAttendanceApp.justification.description">Description</Translate>
            </span>
          </dt>
          <dd>{justificationEntity.description}</dd>
          <dt>
            <span id="startDate">
              <Translate contentKey="senaAttendanceApp.justification.startDate">Start Date</Translate>
            </span>
          </dt>
          <dd>
            {justificationEntity.startDate ? (
              <TextFormat value={justificationEntity.startDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="endDate">
              <Translate contentKey="senaAttendanceApp.justification.endDate">End Date</Translate>
            </span>
          </dt>
          <dd>
            {justificationEntity.endDate ? (
              <TextFormat value={justificationEntity.endDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="evidence">
              <Translate contentKey="senaAttendanceApp.justification.evidence">Evidence</Translate>
            </span>
          </dt>
          <dd>
            {justificationEntity.evidence ? (
              <div>
                {justificationEntity.evidenceContentType ? (
                  <a onClick={openFile(justificationEntity.evidenceContentType, justificationEntity.evidence)}>
                    <Translate contentKey="entity.action.open">Open</Translate>&nbsp;
                  </a>
                ) : null}
                <span>
                  {justificationEntity.evidenceContentType}, {byteSize(justificationEntity.evidence)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.justification.justificationType">Justification Type</Translate>
          </dt>
          <dd>{justificationEntity.justificationType ? justificationEntity.justificationType.name : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.justification.student">Student</Translate>
          </dt>
          <dd>{justificationEntity.student ? justificationEntity.student.documentNumber : ''}</dd>
        </dl>
        <Button as={Link as any} to="/justification" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/justification/${justificationEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default JustificationDetail;

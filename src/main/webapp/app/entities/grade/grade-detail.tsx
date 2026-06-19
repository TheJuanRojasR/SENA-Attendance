import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './grade.reducer';

export const GradeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const gradeEntity = useAppSelector(state => state.grade.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="gradeDetailsHeading">
          <Translate contentKey="senaAttendanceApp.grade.detail.title">Grade</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{gradeEntity.id}</dd>
          <dt>
            <span id="code">
              <Translate contentKey="senaAttendanceApp.grade.code">Code</Translate>
            </span>
          </dt>
          <dd>{gradeEntity.code}</dd>
          <dt>
            <span id="state">
              <Translate contentKey="senaAttendanceApp.grade.state">State</Translate>
            </span>
          </dt>
          <dd>{gradeEntity.state}</dd>
          <dt>
            <span id="startDate">
              <Translate contentKey="senaAttendanceApp.grade.startDate">Start Date</Translate>
            </span>
          </dt>
          <dd>{gradeEntity.startDate ? <TextFormat value={gradeEntity.startDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="endDate">
              <Translate contentKey="senaAttendanceApp.grade.endDate">End Date</Translate>
            </span>
          </dt>
          <dd>{gradeEntity.endDate ? <TextFormat value={gradeEntity.endDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.grade.program">Program</Translate>
          </dt>
          <dd>{gradeEntity.program ? gradeEntity.program.name : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.grade.modality">Modality</Translate>
          </dt>
          <dd>{gradeEntity.modality ? gradeEntity.modality.name : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.grade.timeSlot">Time Slot</Translate>
          </dt>
          <dd>{gradeEntity.timeSlot ? gradeEntity.timeSlot.name : ''}</dd>
        </dl>
        <Button as={Link as any} to="/grade" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/grade/${gradeEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default GradeDetail;

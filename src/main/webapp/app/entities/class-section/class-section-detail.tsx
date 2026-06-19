import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './class-section.reducer';

export const ClassSectionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const classSectionEntity = useAppSelector(state => state.classSection.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="classSectionDetailsHeading">
          <Translate contentKey="senaAttendanceApp.classSection.detail.title">ClassSection</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{classSectionEntity.id}</dd>
          <dt>
            <span id="subjectName">
              <Translate contentKey="senaAttendanceApp.classSection.subjectName">Subject Name</Translate>
            </span>
          </dt>
          <dd>{classSectionEntity.subjectName}</dd>
          <dt>
            <span id="isActive">
              <Translate contentKey="senaAttendanceApp.classSection.isActive">Is Active</Translate>
            </span>
          </dt>
          <dd>{classSectionEntity.isActive ? 'true' : 'false'}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.classSection.instructor">Instructor</Translate>
          </dt>
          <dd>{classSectionEntity.instructor ? classSectionEntity.instructor.documentNumber : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.classSection.grade">Grade</Translate>
          </dt>
          <dd>{classSectionEntity.grade ? classSectionEntity.grade.code : ''}</dd>
        </dl>
        <Button as={Link as any} to="/class-section" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/class-section/${classSectionEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ClassSectionDetail;

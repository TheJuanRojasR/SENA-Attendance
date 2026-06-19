import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './class-exception.reducer';

export const ClassExceptionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const classExceptionEntity = useAppSelector(state => state.classException.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="classExceptionDetailsHeading">
          <Translate contentKey="senaAttendanceApp.classException.detail.title">ClassException</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{classExceptionEntity.id}</dd>
          <dt>
            <span id="date">
              <Translate contentKey="senaAttendanceApp.classException.date">Date</Translate>
            </span>
          </dt>
          <dd>
            {classExceptionEntity.date ? <TextFormat value={classExceptionEntity.date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="reason">
              <Translate contentKey="senaAttendanceApp.classException.reason">Reason</Translate>
            </span>
          </dt>
          <dd>{classExceptionEntity.reason}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.classException.classSection">Class Section</Translate>
          </dt>
          <dd>{classExceptionEntity.classSection ? classExceptionEntity.classSection.subjectName : ''}</dd>
        </dl>
        <Button as={Link as any} to="/class-exception" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/class-exception/${classExceptionEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ClassExceptionDetail;

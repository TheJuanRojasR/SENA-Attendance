import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './audit-log.reducer';

export const AuditLogDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const auditLogEntity = useAppSelector(state => state.auditLog.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="auditLogDetailsHeading">
          <Translate contentKey="senaAttendanceApp.auditLog.detail.title">AuditLog</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{auditLogEntity.id}</dd>
          <dt>
            <span id="previousState">
              <Translate contentKey="senaAttendanceApp.auditLog.previousState">Previous State</Translate>
            </span>
          </dt>
          <dd>{auditLogEntity.previousState}</dd>
          <dt>
            <span id="newState">
              <Translate contentKey="senaAttendanceApp.auditLog.newState">New State</Translate>
            </span>
          </dt>
          <dd>{auditLogEntity.newState}</dd>
          <dt>
            <span id="editDate">
              <Translate contentKey="senaAttendanceApp.auditLog.editDate">Edit Date</Translate>
            </span>
          </dt>
          <dd>{auditLogEntity.editDate ? <TextFormat value={auditLogEntity.editDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.auditLog.modifiedBy">Modified By</Translate>
          </dt>
          <dd>{auditLogEntity.modifiedBy ? auditLogEntity.modifiedBy.documentNumber : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.auditLog.attendance">Attendance</Translate>
          </dt>
          <dd>{auditLogEntity.attendance ? auditLogEntity.attendance.date : ''}</dd>
        </dl>
        <Button as={Link as any} to="/audit-log" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/audit-log/${auditLogEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AuditLogDetail;

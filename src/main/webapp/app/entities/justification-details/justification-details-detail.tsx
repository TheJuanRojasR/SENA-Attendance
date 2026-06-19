import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat, Translate, byteSize, openFile } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './justification-details.reducer';

export const JustificationDetailsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const justificationDetailsEntity = useAppSelector(state => state.justificationDetails.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="justificationDetailsDetailsHeading">
          <Translate contentKey="senaAttendanceApp.justificationDetails.detail.title">JustificationDetails</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{justificationDetailsEntity.id}</dd>
          <dt>
            <span id="stateJustification">
              <Translate contentKey="senaAttendanceApp.justificationDetails.stateJustification">State Justification</Translate>
            </span>
          </dt>
          <dd>{justificationDetailsEntity.stateJustification}</dd>
          <dt>
            <span id="rejectionReason">
              <Translate contentKey="senaAttendanceApp.justificationDetails.rejectionReason">Rejection Reason</Translate>
            </span>
          </dt>
          <dd>{justificationDetailsEntity.rejectionReason}</dd>
          <dt>
            <span id="correctionText">
              <Translate contentKey="senaAttendanceApp.justificationDetails.correctionText">Correction Text</Translate>
            </span>
          </dt>
          <dd>{justificationDetailsEntity.correctionText}</dd>
          <dt>
            <span id="correctionFileUrl">
              <Translate contentKey="senaAttendanceApp.justificationDetails.correctionFileUrl">Correction File Url</Translate>
            </span>
          </dt>
          <dd>
            {justificationDetailsEntity.correctionFileUrl ? (
              <div>
                {justificationDetailsEntity.correctionFileUrlContentType ? (
                  <a
                    onClick={openFile(
                      justificationDetailsEntity.correctionFileUrlContentType,
                      justificationDetailsEntity.correctionFileUrl,
                    )}
                  >
                    <Translate contentKey="entity.action.open">Open</Translate>&nbsp;
                  </a>
                ) : null}
                <span>
                  {justificationDetailsEntity.correctionFileUrlContentType}, {byteSize(justificationDetailsEntity.correctionFileUrl)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <span id="responseDate">
              <Translate contentKey="senaAttendanceApp.justificationDetails.responseDate">Response Date</Translate>
            </span>
          </dt>
          <dd>
            {justificationDetailsEntity.responseDate ? (
              <TextFormat value={justificationDetailsEntity.responseDate} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.justificationDetails.classSection">Class Section</Translate>
          </dt>
          <dd>{justificationDetailsEntity.classSection ? justificationDetailsEntity.classSection.subjectName : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.justificationDetails.justification">Justification</Translate>
          </dt>
          <dd>{justificationDetailsEntity.justification ? justificationDetailsEntity.justification.description : ''}</dd>
        </dl>
        <Button as={Link as any} to="/justification-details" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/justification-details/${justificationDetailsEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default JustificationDetailsDetail;

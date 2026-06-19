import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './global-configuration.reducer';

export const GlobalConfigurationDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const globalConfigurationEntity = useAppSelector(state => state.globalConfiguration.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="globalConfigurationDetailsHeading">
          <Translate contentKey="senaAttendanceApp.globalConfiguration.detail.title">GlobalConfiguration</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{globalConfigurationEntity.id}</dd>
          <dt>
            <span id="studentJustificationDays">
              <Translate contentKey="senaAttendanceApp.globalConfiguration.studentJustificationDays">Student Justification Days</Translate>
            </span>
          </dt>
          <dd>{globalConfigurationEntity.studentJustificationDays}</dd>
          <dt>
            <span id="instructorResponseDays">
              <Translate contentKey="senaAttendanceApp.globalConfiguration.instructorResponseDays">Instructor Response Days</Translate>
            </span>
          </dt>
          <dd>{globalConfigurationEntity.instructorResponseDays}</dd>
          <dt>
            <span id="lateArrivalsToFail">
              <Translate contentKey="senaAttendanceApp.globalConfiguration.lateArrivalsToFail">Late Arrivals To Fail</Translate>
            </span>
          </dt>
          <dd>{globalConfigurationEntity.lateArrivalsToFail}</dd>
          <dt>
            <span id="maxPostponementJustifications">
              <Translate contentKey="senaAttendanceApp.globalConfiguration.maxPostponementJustifications">
                Max Postponement Justifications
              </Translate>
            </span>
          </dt>
          <dd>{globalConfigurationEntity.maxPostponementJustifications}</dd>
          <dt>
            <span id="standardTrimesterMonths">
              <Translate contentKey="senaAttendanceApp.globalConfiguration.standardTrimesterMonths">Standard Trimester Months</Translate>
            </span>
          </dt>
          <dd>{globalConfigurationEntity.standardTrimesterMonths}</dd>
        </dl>
        <Button as={Link as any} to="/global-configuration" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/global-configuration/${globalConfigurationEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default GlobalConfigurationDetail;

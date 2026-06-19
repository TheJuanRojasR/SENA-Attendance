import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './document-type.reducer';

export const DocumentTypeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const documentTypeEntity = useAppSelector(state => state.documentType.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="documentTypeDetailsHeading">
          <Translate contentKey="senaAttendanceApp.documentType.detail.title">DocumentType</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{documentTypeEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="senaAttendanceApp.documentType.name">Name</Translate>
            </span>
          </dt>
          <dd>{documentTypeEntity.name}</dd>
          <dt>
            <span id="initials">
              <Translate contentKey="senaAttendanceApp.documentType.initials">Initials</Translate>
            </span>
          </dt>
          <dd>{documentTypeEntity.initials}</dd>
        </dl>
        <Button as={Link as any} to="/document-type" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/document-type/${documentTypeEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default DocumentTypeDetail;

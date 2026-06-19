import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './user-profile.reducer';

export const UserProfileDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const userProfileEntity = useAppSelector(state => state.userProfile.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="userProfileDetailsHeading">
          <Translate contentKey="senaAttendanceApp.userProfile.detail.title">UserProfile</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.id}</dd>
          <dt>
            <span id="firstName">
              <Translate contentKey="senaAttendanceApp.userProfile.firstName">First Name</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.firstName}</dd>
          <dt>
            <span id="middleName">
              <Translate contentKey="senaAttendanceApp.userProfile.middleName">Middle Name</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.middleName}</dd>
          <dt>
            <span id="firstLastName">
              <Translate contentKey="senaAttendanceApp.userProfile.firstLastName">First Last Name</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.firstLastName}</dd>
          <dt>
            <span id="secondLastName">
              <Translate contentKey="senaAttendanceApp.userProfile.secondLastName">Second Last Name</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.secondLastName}</dd>
          <dt>
            <span id="documentNumber">
              <Translate contentKey="senaAttendanceApp.userProfile.documentNumber">Document Number</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.documentNumber}</dd>
          <dt>
            <span id="phoneNumber">
              <Translate contentKey="senaAttendanceApp.userProfile.phoneNumber">Phone Number</Translate>
            </span>
          </dt>
          <dd>{userProfileEntity.phoneNumber}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.userProfile.user">User</Translate>
          </dt>
          <dd>{userProfileEntity.user ? userProfileEntity.user.login : ''}</dd>
          <dt>
            <Translate contentKey="senaAttendanceApp.userProfile.documentType">Document Type</Translate>
          </dt>
          <dd>{userProfileEntity.documentType ? userProfileEntity.documentType.name : ''}</dd>
        </dl>
        <Button as={Link as any} to="/user-profile" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/user-profile/${userProfileEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default UserProfileDetail;

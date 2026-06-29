import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { Translate, getSortState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities } from './global-configuration.reducer';

export const GlobalConfiguration = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const globalConfigurationList = useAppSelector(state => state.globalConfiguration.entities);
  const loading = useAppSelector(state => state.globalConfiguration.loading);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        sort: `${sortState.sort},${sortState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const { order } = sortState;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="global-configuration-heading" data-cy="GlobalConfigurationHeading">
        <Translate contentKey="senaAttendanceApp.globalConfiguration.home.title">Global Configurations</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="senaAttendanceApp.globalConfiguration.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link
            to="/global-configuration/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="senaAttendanceApp.globalConfiguration.home.createLabel">Create new Global Configuration</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {globalConfigurationList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="senaAttendanceApp.globalConfiguration.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('studentJustificationDays')}>
                  <Translate contentKey="senaAttendanceApp.globalConfiguration.studentJustificationDays">
                    Student Justification Days
                  </Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('studentJustificationDays')} />
                </th>
                <th className="hand" onClick={sort('instructorResponseDays')}>
                  <Translate contentKey="senaAttendanceApp.globalConfiguration.instructorResponseDays">Instructor Response Days</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('instructorResponseDays')} />
                </th>
                <th className="hand" onClick={sort('lateArrivalsToFail')}>
                  <Translate contentKey="senaAttendanceApp.globalConfiguration.lateArrivalsToFail">Late Arrivals To Fail</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('lateArrivalsToFail')} />
                </th>
                <th className="hand" onClick={sort('maxPostponementJustifications')}>
                  <Translate contentKey="senaAttendanceApp.globalConfiguration.maxPostponementJustifications">
                    Max Postponement Justifications
                  </Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('maxPostponementJustifications')} />
                </th>
                <th className="hand" onClick={sort('standardTrimesterMonths')}>
                  <Translate contentKey="senaAttendanceApp.globalConfiguration.standardTrimesterMonths">
                    Standard Trimester Months
                  </Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('standardTrimesterMonths')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {globalConfigurationList.map(globalConfiguration => (
                <tr key={`entity-${globalConfiguration.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/global-configuration/${globalConfiguration.id}`} variant="link" size="sm">
                      {globalConfiguration.id}
                    </Button>
                  </td>
                  <td>{globalConfiguration.studentJustificationDays}</td>
                  <td>{globalConfiguration.instructorResponseDays}</td>
                  <td>{globalConfiguration.lateArrivalsToFail}</td>
                  <td>{globalConfiguration.maxPostponementJustifications}</td>
                  <td>{globalConfiguration.standardTrimesterMonths}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/global-configuration/${globalConfiguration.id}`}
                        variant="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        as={Link as any}
                        to={`/global-configuration/${globalConfiguration.id}/edit`}
                        variant="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() => (globalThis.location.href = `/global-configuration/${globalConfiguration.id}/delete`)}
                        variant="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-success">
              <Translate contentKey="senaAttendanceApp.globalConfiguration.home.notFound">No Global Configurations found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default GlobalConfiguration;

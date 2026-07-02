import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, Translate, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './desertion-counter.reducer';

export const DesertionCounter = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const desertionCounterList = useAppSelector(state => state.desertionCounter.entities);
  const loading = useAppSelector(state => state.desertionCounter.loading);
  const totalItems = useAppSelector(state => state.desertionCounter.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const { order } = paginationState;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="desertion-counter-heading" data-cy="DesertionCounterHeading">
        <Translate contentKey="senaAttendanceApp.desertionCounter.home.title">Desertion Counters</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="senaAttendanceApp.desertionCounter.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/desertion-counter/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="senaAttendanceApp.desertionCounter.home.createLabel">Create new Desertion Counter</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {desertionCounterList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('totalGlobalAbsences')}>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.totalGlobalAbsences">Total Global Absences</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('totalGlobalAbsences')} />
                </th>
                <th className="hand" onClick={sort('accumulatedLateArrivals')}>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.accumulatedLateArrivals">Accumulated Late Arrivals</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('accumulatedLateArrivals')} />
                </th>
                <th className="hand" onClick={sort('workJustificationsUsed')}>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.workJustificationsUsed">Work Justifications Used</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('workJustificationsUsed')} />
                </th>
                <th className="hand" onClick={sort('alertsSent')}>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.alertsSent">Alerts Sent</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('alertsSent')} />
                </th>
                <th>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.student">Student</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="senaAttendanceApp.desertionCounter.trimester">Trimester</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {desertionCounterList.map(desertionCounter => (
                <tr key={`entity-${desertionCounter.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/desertion-counter/${desertionCounter.id}`} variant="link" size="sm">
                      {desertionCounter.id}
                    </Button>
                  </td>
                  <td>{desertionCounter.totalGlobalAbsences}</td>
                  <td>{desertionCounter.accumulatedLateArrivals}</td>
                  <td>{desertionCounter.workJustificationsUsed}</td>
                  <td>{desertionCounter.alertsSent ? 'true' : 'false'}</td>
                  <td>
                    {desertionCounter.student ? (
                      <Link to={`/user-profile/${desertionCounter.student.id}`}>{desertionCounter.student.documentNumber}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>
                    {desertionCounter.trimester ? (
                      <Link to={`/trimester/${desertionCounter.trimester.id}`}>{desertionCounter.trimester.name}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/desertion-counter/${desertionCounter.id}`}
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
                        to={`/desertion-counter/${desertionCounter.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                        onClick={() =>
                          (globalThis.location.href = `/desertion-counter/${desertionCounter.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
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
              <Translate contentKey="senaAttendanceApp.desertionCounter.home.notFound">No Desertion Counters found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={desertionCounterList && desertionCounterList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default DesertionCounter;

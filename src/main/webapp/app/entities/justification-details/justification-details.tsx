import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, byteSize, getPaginationState, openFile } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './justification-details.reducer';

export const JustificationDetails = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const justificationDetailsList = useAppSelector(state => state.justificationDetails.entities);
  const loading = useAppSelector(state => state.justificationDetails.loading);
  const totalItems = useAppSelector(state => state.justificationDetails.totalItems);

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
      <h2 id="justification-details-heading" data-cy="JustificationDetailsHeading">
        <Translate contentKey="senaAttendanceApp.justificationDetails.home.title">Justification Details</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="senaAttendanceApp.justificationDetails.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link
            to="/justification-details/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="senaAttendanceApp.justificationDetails.home.createLabel">Create new Justification Details</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {justificationDetailsList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('stateJustification')}>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.stateJustification">State Justification</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('stateJustification')} />
                </th>
                <th className="hand" onClick={sort('rejectionReason')}>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.rejectionReason">Rejection Reason</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('rejectionReason')} />
                </th>
                <th className="hand" onClick={sort('correctionText')}>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.correctionText">Correction Text</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('correctionText')} />
                </th>
                <th className="hand" onClick={sort('correctionFileUrl')}>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.correctionFileUrl">Correction File Url</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('correctionFileUrl')} />
                </th>
                <th className="hand" onClick={sort('responseDate')}>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.responseDate">Response Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('responseDate')} />
                </th>
                <th>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.classSection">Class Section</Translate>{' '}
                  <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="senaAttendanceApp.justificationDetails.justification">Justification</Translate>{' '}
                  <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {justificationDetailsList.map(justificationDetails => (
                <tr key={`entity-${justificationDetails.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/justification-details/${justificationDetails.id}`} variant="link" size="sm">
                      {justificationDetails.id}
                    </Button>
                  </td>
                  <td>
                    <Translate contentKey={`senaAttendanceApp.StateJustification.${justificationDetails.stateJustification}`} />
                  </td>
                  <td>{justificationDetails.rejectionReason}</td>
                  <td>{justificationDetails.correctionText}</td>
                  <td>
                    {justificationDetails.correctionFileUrl ? (
                      <div>
                        {justificationDetails.correctionFileUrlContentType ? (
                          <a onClick={openFile(justificationDetails.correctionFileUrlContentType, justificationDetails.correctionFileUrl)}>
                            <Translate contentKey="entity.action.open">Open</Translate>
                            &nbsp;
                          </a>
                        ) : null}
                        <span>
                          {justificationDetails.correctionFileUrlContentType}, {byteSize(justificationDetails.correctionFileUrl)}
                        </span>
                      </div>
                    ) : null}
                  </td>
                  <td>
                    {justificationDetails.responseDate ? (
                      <TextFormat type="date" value={justificationDetails.responseDate} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>
                    {justificationDetails.classSection ? (
                      <Link to={`/class-section/${justificationDetails.classSection.id}`}>
                        {justificationDetails.classSection.subjectName}
                      </Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>
                    {justificationDetails.justification ? (
                      <Link to={`/justification/${justificationDetails.justification.id}`}>
                        {justificationDetails.justification.description}
                      </Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/justification-details/${justificationDetails.id}`}
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
                        to={`/justification-details/${justificationDetails.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (globalThis.location.href = `/justification-details/${justificationDetails.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="senaAttendanceApp.justificationDetails.home.notFound">No Justification Details found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={justificationDetailsList && justificationDetailsList.length > 0 ? '' : 'd-none'}>
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

export default JustificationDetails;

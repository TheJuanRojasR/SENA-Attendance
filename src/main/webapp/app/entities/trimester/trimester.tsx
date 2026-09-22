import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities, searchEntities } from './trimester.reducer';
import LinkButton from 'app/shared/components/link-button';

const SEARCH_DEBOUNCE_MS = 300;

export const Trimester = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'FUTURO' | 'ACTIVO' | 'CERRADO'

  const trimesterList = useAppSelector(state => state.trimester.entities);
  const loading = useAppSelector(state => state.trimester.loading);
  const totalItems = useAppSelector(state => state.trimester.totalItems);

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedSearch(search.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [search]);

  // Vuelve a la primera página cuando cambian los filtros, para no quedar en una página vacía.
  useEffect(() => {
    if (paginationState.activePage !== 1) {
      setPaginationState({ ...paginationState, activePage: 1 });
    }
  }, [debouncedSearch, stateFilter]);

  const statusParam = stateFilter !== 'ALL' ? stateFilter : undefined;

  const getAllEntities = () => {
    if (debouncedSearch || statusParam) {
      dispatch(
        searchEntities({
          search: debouncedSearch || undefined,
          status: statusParam,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    } else {
      dispatch(
        getEntities({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    }
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
  }, [paginationState.activePage, paginationState.order, paginationState.sort, stateFilter, debouncedSearch]);

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
      <h2 id="trimester-heading" data-cy="TrimesterHeading">
        <Translate contentKey="senaAttendanceApp.trimester.home.title">Trimesters</Translate>
      </h2>
      <p>
        Administre y programe los trimestres académicos, rangos de fechas lectivas y estado operativo para la formacion institucional SENA.
      </p>
      <Col className="d-flex justify-content-between align-items-center" md="12">
        <div className="d-flex align-items-center entitiesSearchBar">
          <div className="d-flex align-items-center w-50">
            <FontAwesomeIcon icon={faSearch}></FontAwesomeIcon>
            <ValidatedInput
              name="search"
              placeholder="Buscar por nombre o año..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
          <ValidatedInput type="select" name="state" className="w-25" value={stateFilter} onChange={e => setStateFilter(e.target.value)}>
            <option value="ALL">Todas</option>
            <option value="FUTURO">Futuras</option>
            <option value="ACTIVO">Activas</option>
            <option value="CERRADO">Cerradas</option>
          </ValidatedInput>
        </div>
        <LinkButton to="/trimester/new" data-cy="entityCreateButton">
          <FontAwesomeIcon icon="plus" />
          &nbsp;
          <Translate contentKey="senaAttendanceApp.trimester.home.createLabel">Create new Trimester</Translate>
        </LinkButton>
      </Col>
      <div className="table-responsive">
        {trimesterList?.length > 0 ? (
          <Card>
            <Table responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    <Translate contentKey="senaAttendanceApp.trimester.name">Name</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('startDate')}>
                    <Translate contentKey="senaAttendanceApp.trimester.startDate">Start Date</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('startDate')} />
                  </th>
                  <th className="hand" onClick={sort('endDate')}>
                    <Translate contentKey="senaAttendanceApp.trimester.endDate">End Date</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('endDate')} />
                  </th>
                  <th className="hand" onClick={sort('status')}>
                    <Translate contentKey="senaAttendanceApp.trimester.state">State</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {trimesterList.map(trimester => (
                  <tr key={`entity-${trimester.id}`} data-cy="entityTable">
                    <td>{trimester.name}</td>
                    <td>
                      {trimester.startDate ? <TextFormat type="date" value={trimester.startDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </td>
                    <td>
                      {trimester.endDate ? <TextFormat type="date" value={trimester.endDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </td>
                    <td>
                      <Translate contentKey={`senaAttendanceApp.StateTrimester.${trimester.status}`} />
                    </td>
                    <td className="text-end">
                      <div className="btn-group flex-btn-group-container">
                        <Button
                          as={Link as any}
                          to={`/trimester/${trimester.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                            (globalThis.location.href = `/trimester/${trimester.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
          </Card>
        ) : (
          !loading && (
            <div className="alert alert-success">
              <Translate contentKey="senaAttendanceApp.trimester.home.notFound">No Trimesters found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={trimesterList && trimesterList.length > 0 ? '' : 'd-none'}>
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

export default Trimester;

import React, { useEffect, useState } from 'react';
import { Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities, searchEntities } from './trimester.reducer';

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
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> <span className="current">TRIMESTRES</span>
          </div>
          <h2 id="trimester-heading" data-cy="TrimesterHeading" className="page-title">
            Gestión de Trimestres
          </h2>
          <p className="page-description">
            Administre y programe los trimestres académicos, rangos de fechas lectivas y estado operativo para la formacion institucional
            SENA.
          </p>
        </div>
        <div>
          <Link
            to="/trimester/new"
            className="btn btn-success fw-bold"
            style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
          >
            <FontAwesomeIcon icon="plus" className="me-2" />
            Crear Nuevo Trimestre
          </Link>
        </div>
      </div>

      <div className="entity-card">
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-3">
          <div className="search-input-wrapper w-100">
            <FontAwesomeIcon icon="search" />
            <ValidatedInput
              name="search"
              placeholder="Buscar por nombre o año..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
          <div className="d-flex align-items-center w-100 w-md-auto">
            <span className="me-2 text-muted fw-bold" style={{ fontSize: '0.85rem' }}>
              ESTADO:
            </span>
            <ValidatedInput
              type="select"
              name="state"
              className="w-100 mb-0"
              value={stateFilter}
              onChange={e => setStateFilter(e.target.value)}
            >
              <option value="ALL">Todos los trimestres</option>
              <option value="FUTURO">Futuros</option>
              <option value="ACTIVO">Activos</option>
              <option value="CERRADO">Cerrados</option>
            </ValidatedInput>
          </div>
        </div>

        <div className="table-responsive">
          {trimesterList?.length > 0 ? (
            <Table className="custom-table" hover responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    NOMBRE <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('startDate')}>
                    FECHA INICIO <FontAwesomeIcon icon={getSortIconByFieldName('startDate')} />
                  </th>
                  <th className="hand" onClick={sort('endDate')}>
                    FECHA FIN <FontAwesomeIcon icon={getSortIconByFieldName('endDate')} />
                  </th>
                  <th className="hand" onClick={sort('status')}>
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {trimesterList.map(trimester => (
                  <tr key={`entity-${trimester.id}`} data-cy="entityTable">
                    <td className="fw-bold">{trimester.name}</td>
                    <td>
                      {trimester.startDate ? <TextFormat type="date" value={trimester.startDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </td>
                    <td>
                      {trimester.endDate ? <TextFormat type="date" value={trimester.endDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </td>
                    <td>
                      {trimester.status === 'ACTIVO' ? (
                        <span className="badge-status active">
                          <span className="dot"></span> Activo
                        </span>
                      ) : trimester.status === 'FUTURO' ? (
                        <span className="badge-status pending">
                          <span className="dot" style={{ backgroundColor: '#ffb74d' }}></span> Futuro
                        </span>
                      ) : (
                        <span className="badge-status inactive">
                          <span className="dot"></span> Cerrado
                        </span>
                      )}
                    </td>
                    <td className="text-end">
                      <div className="action-icons">
                        <Link
                          to={`/trimester/${trimester.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                          title="Editar"
                        >
                          <FontAwesomeIcon icon="pencil-alt" />
                        </Link>
                        <Link
                          to={`/trimester/${trimester.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                          className="delete"
                          title="Eliminar"
                        >
                          <FontAwesomeIcon icon="trash" />
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
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
            <div className="justify-content-center d-flex mt-4 mb-2 text-muted" style={{ fontSize: '0.85rem' }}>
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
    </div>
  );
};

export default Trimester;

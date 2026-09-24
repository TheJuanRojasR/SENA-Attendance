import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, Translate, getPaginationState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { IProgram } from 'app/shared/model/program.model';

import { clearActivationWarning, getActiveEntities, getEntities, searchEntities, setActivated } from './program.reducer';
import LinkButton from 'app/shared/components/link-button';

const SEARCH_DEBOUNCE_MS = 300;

export const Program = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const programList = useAppSelector(state => state.program.entities);
  const loading = useAppSelector(state => state.program.loading);
  const totalItems = useAppSelector(state => state.program.totalItems);
  const activationWarning = useAppSelector(state => state.program.activationWarning);

  useEffect(() => {
    if (activationWarning) {
      toast.warning(activationWarning);
      dispatch(clearActivationWarning());
    }
  }, [activationWarning]);

  const toggleActive = (program: IProgram) => () => {
    const nextStatus = !program.status;
    if (!nextStatus) {
      const confirmed = window.confirm(
        `¿Deseas desactivar el programa "${program.name}"? Las fichas existentes, sus materias y asistencias no se verán afectadas.`,
      );
      if (!confirmed) {
        return;
      }
    }
    dispatch(setActivated({ id: program.id!, status: nextStatus }));
  };

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

  const filteredProgramList = programList
    ?.filter(program => program.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(program => (stateFilter === 'INACTIVE' ? program.status === false : true));

  const statusParam = stateFilter === 'ACTIVE' ? true : stateFilter === 'INACTIVE' ? false : undefined;

  const getAllEntities = () => {
    if (debouncedSearch) {
      dispatch(
        searchEntities({
          search: debouncedSearch,
          status: statusParam,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    } else if (stateFilter === 'ACTIVE') {
      dispatch(getActiveEntities());
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
            INICIO <span className="separator">/</span> <span className="current">PROGRAMAS</span>
          </div>
          <h2 id="program-heading" data-cy="ProgramHeading" className="page-title">
            Gestión de Programas
          </h2>
          <p className="page-description">
            Administre el catálogo de programas ofrecidos. Puede crear nuevos programas, modificar sus caracteristicas o gestionar su estado
            de disponibilidad.
          </p>
        </div>
        <div>
          <Link
            to="/program/new"
            className="btn btn-success fw-bold"
            style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
          >
            <FontAwesomeIcon icon="plus" className="me-2" />
            Crear Nuevo Programa
          </Link>
        </div>
      </div>

      <div className="entity-card">
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-3">
          <div className="search-input-wrapper w-100">
            <FontAwesomeIcon icon="search" />
            <ValidatedInput name="search" placeholder="Buscar por nombre..." value={search} onChange={e => setSearch(e.target.value)} />
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
              <option value="ALL">Todos los programas</option>
              <option value="ACTIVE">Activos</option>
              <option value="INACTIVE">Inactivos</option>
            </ValidatedInput>
          </div>
        </div>

        <div className="table-responsive">
          {filteredProgramList?.length > 0 ? (
            <Table className="custom-table" hover responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('code')}>
                    CÓDIGO <FontAwesomeIcon icon={getSortIconByFieldName('code')} />
                  </th>
                  <th className="hand" onClick={sort('name')}>
                    NOMBRE <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('status')}>
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredProgramList.map(program => (
                  <tr key={`entity-${program.id}`} data-cy="entityTable">
                    <td>
                      <span className="badge-initials">{program.code}</span>
                    </td>
                    <td className="fw-bold">{program.name}</td>
                    <td>
                      {program.status ? (
                        <span className="badge-status active">
                          <span className="dot"></span> Activo
                        </span>
                      ) : (
                        <span className="badge-status inactive">
                          <span className="dot"></span> Inactivo
                        </span>
                      )}
                    </td>
                    <td className="text-end">
                      <div className="action-icons">
                        <Link
                          to={`/program/${program.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                          title="Editar"
                        >
                          <FontAwesomeIcon icon="pencil-alt" />
                        </Link>
                        <button
                          type="button"
                          className="power"
                          onClick={toggleActive(program)}
                          title={program.status ? 'Desactivar' : 'Reactivar'}
                        >
                          <FontAwesomeIcon icon="power-off" />
                        </button>
                        <Link
                          to={`/program/${program.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                <Translate contentKey="senaAttendanceApp.program.home.notFound">No Programs found</Translate>
              </div>
            )
          )}
        </div>
        {totalItems ? (
          <div className={filteredProgramList && filteredProgramList.length > 0 ? '' : 'd-none'}>
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

export default Program;

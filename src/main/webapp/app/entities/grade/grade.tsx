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

import { getActiveEntities, getEntities } from './grade.reducer';

export const Grade = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'PENDIENTE' | 'ACTIVA' | 'FINALIZADA' | 'APLAZADA' | 'CANCELADA'

  const gradeList = useAppSelector(state => state.grade.entities);
  const loading = useAppSelector(state => state.grade.loading);
  const totalItems = useAppSelector(state => state.grade.totalItems);

  // No existe un endpoint de búsqueda para fichas: se filtra en cliente sobre la página cargada.
  const filteredGradeList = gradeList
    ?.filter(grade => grade.code?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(grade => (stateFilter === 'ALL' ? true : grade.state === stateFilter));

  const getAllEntities = () => {
    if (stateFilter === 'ACTIVA') {
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
  }, [paginationState.activePage, paginationState.order, paginationState.sort, stateFilter]);

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
            INICIO <span className="separator">/</span> <span className="current">FICHAS</span>
          </div>
          <h2 id="grade-heading" data-cy="GradeHeading" className="page-title">
            Gestión de Fichas
          </h2>
          <p className="page-description">
            Administre las fichas (grupos) de formación. Gestione su estado, fechas de inicio y fin, programa asociado y demás
            configuraciones.
          </p>
        </div>
        <div className="d-flex align-items-center entitiesSearchBar">
          <div className="d-flex align-items-center w-50">
            <FontAwesomeIcon icon={faSearch}></FontAwesomeIcon>
            <ValidatedInput name="search" placeholder="Buscar por código..." value={search} onChange={e => setSearch(e.target.value)} />
          </div>
          <ValidatedInput type="select" name="state" className="w-25" value={stateFilter} onChange={e => setStateFilter(e.target.value)}>
            <option value="ALL">Todas</option>
            <option value="PENDIENTE">Pendientes</option>
            <option value="ACTIVA">Activas</option>
            <option value="FINALIZADA">Finalizadas</option>
            <option value="APLAZADA">Aplazadas</option>
            <option value="CANCELADA">Canceladas</option>
          </ValidatedInput>
        </div>
        <Link
          to="/grade/new"
          className="btn btn-success fw-bold"
          style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
        >
          <FontAwesomeIcon icon="plus" className="me-2" />
          Crear Nueva Ficha
        </Link>
      </div>
      <div className="entity-card">
        <div className="table-responsive">
          {gradeList?.length > 0 ? (
            <Table className="custom-table" hover responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('code')}>
                    CÓDIGO <FontAwesomeIcon icon={getSortIconByFieldName('code')} />
                  </th>
                  <th className="hand" onClick={sort('state')}>
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('state')} />
                  </th>
                  <th className="hand" onClick={sort('startDate')}>
                    FECHA INICIO <FontAwesomeIcon icon={getSortIconByFieldName('startDate')} />
                  </th>
                  <th className="hand" onClick={sort('endDate')}>
                    FECHA FIN <FontAwesomeIcon icon={getSortIconByFieldName('endDate')} />
                  </th>
                  <th>
                    PROGRAMA <FontAwesomeIcon icon="sort" />
                  </th>
                  <th>
                    MODALIDAD <FontAwesomeIcon icon="sort" />
                  </th>
                  <th>
                    JORNADA <FontAwesomeIcon icon="sort" />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {gradeList.map(grade => (
                  <tr key={`entity-${grade.id}`} data-cy="entityTable">
                    <td>
                      <span className="badge-initials">{grade.code}</span>
                    </td>
                    <td>
                      <Translate contentKey={`senaAttendanceApp.StateGrade.${grade.state}`} />
                    </td>
                    <td>{grade.startDate ? <TextFormat type="date" value={grade.startDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                    <td>{grade.endDate ? <TextFormat type="date" value={grade.endDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                    <td>{grade.program ? <Link to={`/program/${grade.program.id}`}>{grade.program.name}</Link> : ''}</td>
                    <td>{grade.modality ? <Link to={`/modality/${grade.modality.id}`}>{grade.modality.name}</Link> : ''}</td>
                    <td>{grade.timeSlot ? <Link to={`/time-slot/${grade.timeSlot.id}`}>{grade.timeSlot.name}</Link> : ''}</td>
                    <td className="text-end">
                      <div className="action-icons">
                        <Link to={`/grade/${grade.id}`} title="Ver">
                          <FontAwesomeIcon icon="eye" />
                        </Link>
                        <Link
                          to={`/grade/${grade.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                          title="Editar"
                        >
                          <FontAwesomeIcon icon="pencil-alt" />
                        </Link>
                        <Link
                          to={`/grade/${grade.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                <Translate contentKey="senaAttendanceApp.grade.home.notFound">No Grades found</Translate>
              </div>
            )
          )}
        </div>
      </div>
      {totalItems ? (
        <div className={filteredGradeList && filteredGradeList.length > 0 ? '' : 'd-none'}>
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
  );
};

export default Grade;

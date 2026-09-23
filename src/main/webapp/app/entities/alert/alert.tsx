import React, { useEffect, useState } from 'react';
import { Badge, Button, Col, Row, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './alert.reducer';

const stateVariant: Record<string, string> = {
  NO_LEIDA: 'warning',
  LEIDA: 'info',
  ATENDIDA: 'success',
  RESUELTA_AUTOMATICAMENTE: 'secondary',
};

const stateLabel: Record<string, string> = {
  NO_LEIDA: 'No leída',
  LEIDA: 'Leída',
  ATENDIDA: 'Atendida',
  RESUELTA_AUTOMATICAMENTE: 'Resuelta automáticamente',
};

// UC013, flujo A1: bandeja de alertas de inasistencia, acotada por rol en el backend (el
// instructor ve las CONSECUTIVAS de sus materias y las ACUMULADAS de sus fichas; el Admin ve
// todas). El sistema genera y resuelve las alertas; aquí solo se consultan y atienden.
export const Alert = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'generatedAt'), pageLocation.search),
  );
  const [typeFilter, setTypeFilter] = useState('');
  const [stateFilter, setStateFilter] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');

  const alertList = useAppSelector(state => state.alert.entities);
  const loading = useAppSelector(state => state.alert.loading);
  const totalItems = useAppSelector(state => state.alert.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
        type: typeFilter || undefined,
        state: stateFilter || undefined,
        from: from ? `${from}T00:00:00Z` : undefined,
        to: to ? `${to}T23:59:59Z` : undefined,
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
  }, [paginationState.activePage, paginationState.order, paginationState.sort, typeFilter, stateFilter, from, to]);

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
      <h2 id="alert-heading" data-cy="AlertHeading">
        Alertas de inasistencia
      </h2>
      <p>Fallas consecutivas por materia y fallas acumuladas por ficha que superaron el umbral configurado.</p>
      <Row className="mb-3">
        <Col md="3">
          <label htmlFor="type-filter">Tipo</label>
          <select id="type-filter" className="form-select" value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
            <option value="">Todos</option>
            <option value="CONSECUTIVAS">Consecutivas</option>
            <option value="ACUMULADAS">Acumuladas</option>
          </select>
        </Col>
        <Col md="3">
          <label htmlFor="state-filter">Estado</label>
          <select id="state-filter" className="form-select" value={stateFilter} onChange={e => setStateFilter(e.target.value)}>
            <option value="">Todos</option>
            <option value="NO_LEIDA">No leída</option>
            <option value="LEIDA">Leída</option>
            <option value="ATENDIDA">Atendida</option>
            <option value="RESUELTA_AUTOMATICAMENTE">Resuelta automáticamente</option>
          </select>
        </Col>
        <Col md="3">
          <label htmlFor="from-filter">Generadas desde</label>
          <input id="from-filter" type="date" className="form-control" value={from} onChange={e => setFrom(e.target.value)} />
        </Col>
        <Col md="3">
          <label htmlFor="to-filter">Generadas hasta</label>
          <input id="to-filter" type="date" className="form-control" value={to} onChange={e => setTo(e.target.value)} />
        </Col>
      </Row>
      <div className="table-responsive">
        {alertList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('generatedAt')}>
                  Generada <FontAwesomeIcon icon={getSortIconByFieldName('generatedAt')} />
                </th>
                <th>Aprendiz</th>
                <th>Tipo</th>
                <th>Materia / Ficha</th>
                <th>Conteo</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {alertList.map(alerta => (
                <tr key={`entity-${alerta.id}`} data-cy="entityTable">
                  <td>{alerta.generatedAt ? <TextFormat type="date" value={alerta.generatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>
                    {alerta.student?.firstName} {alerta.student?.firstLastName} ({alerta.student?.documentNumber})
                  </td>
                  <td>{alerta.type === 'CONSECUTIVAS' ? 'Consecutivas' : 'Acumuladas'}</td>
                  <td>{alerta.type === 'CONSECUTIVAS' ? alerta.classSection?.subjectName : `Ficha ${alerta.grade?.code ?? ''}`}</td>
                  <td>
                    {alerta.absenceCount} / {alerta.threshold}
                  </td>
                  <td>
                    <Badge bg={stateVariant[alerta.state ?? ''] ?? 'secondary'}>{stateLabel[alerta.state ?? ''] ?? alerta.state}</Badge>
                  </td>
                  <td className="text-end">
                    <Button as={Link as any} to={`/alert/${alerta.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                      <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">Revisar</span>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-success">No hay alertas que coincidan con estos filtros.</div>
        )}
      </div>
      {totalItems ? (
        <div className={alertList && alertList.length > 0 ? '' : 'd-none'}>
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

export default Alert;

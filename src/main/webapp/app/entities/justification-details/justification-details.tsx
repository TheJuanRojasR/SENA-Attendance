import React, { useEffect, useState } from 'react';
import { Badge, Button, Col, Row, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getClassSections, getMine as getMyClassSections } from 'app/entities/class-section/class-section.reducer';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getPending } from './justification-details.reducer';

const stateVariant: Record<string, string> = {
  PENDIENTE: 'warning',
  ACEPTADA: 'success',
  RECHAZADA: 'danger',
  CANCELADA: 'secondary',
};

// UC010, flujo básico: bandeja de partes pendientes de las materias del instructor (o de todas,
// si es Admin). Sin filtro de estado se listan solo las PENDIENTE; con otro estado (A1) se
// consulta el histórico de decisiones.
export const JustificationDetails = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id', 'desc'), pageLocation.search),
  );
  const [stateFilter, setStateFilter] = useState('');
  const [classSectionId, setClassSectionId] = useState('');
  const [createdFrom, setCreatedFrom] = useState('');
  const [createdTo, setCreatedTo] = useState('');

  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.ADMIN]));
  const classSections = useAppSelector(state => (isAdmin ? state.classSection.entities : state.classSection.mine));

  const justificationDetailsList = useAppSelector(state => state.justificationDetails.entities);
  const loading = useAppSelector(state => state.justificationDetails.loading);
  const totalItems = useAppSelector(state => state.justificationDetails.totalItems);

  useEffect(() => {
    if (isAdmin) {
      dispatch(getClassSections({}));
    } else {
      dispatch(getMyClassSections());
    }
  }, [isAdmin]);

  const getAllEntities = () => {
    dispatch(
      getPending({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
        stateJustification: stateFilter || undefined,
        classSectionId: classSectionId || undefined,
        createdFrom: createdFrom || undefined,
        createdTo: createdTo || undefined,
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
  }, [paginationState.activePage, paginationState.order, paginationState.sort, stateFilter, classSectionId, createdFrom, createdTo]);

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
      <h2 id="justification-details-heading" data-cy="JustificationDetailsHeading">
        Justificaciones
      </h2>
      <p>Revisa y decide las partes de las justificaciones de las materias que dictas.</p>
      <Row className="mb-3">
        <Col md="3">
          <label htmlFor="state-filter">Estado</label>
          <select id="state-filter" className="form-select" value={stateFilter} onChange={e => setStateFilter(e.target.value)}>
            <option value="">Pendientes</option>
            <option value="ACEPTADA">Aprobadas</option>
            <option value="RECHAZADA">Rechazadas</option>
            <option value="CANCELADA">Canceladas</option>
          </select>
        </Col>
        <Col md="3">
          <label htmlFor="class-section-filter">Materia</label>
          <select
            id="class-section-filter"
            className="form-select"
            value={classSectionId}
            onChange={e => setClassSectionId(e.target.value)}
          >
            <option value="">Todas</option>
            {classSections.map(cs => (
              <option value={cs.id} key={cs.id}>
                {cs.subjectName}
              </option>
            ))}
          </select>
        </Col>
        <Col md="3">
          <label htmlFor="created-from">Solicitadas desde</label>
          <input
            id="created-from"
            type="date"
            className="form-control"
            value={createdFrom}
            onChange={e => setCreatedFrom(e.target.value)}
          />
        </Col>
        <Col md="3">
          <label htmlFor="created-to">Solicitadas hasta</label>
          <input id="created-to" type="date" className="form-control" value={createdTo} onChange={e => setCreatedTo(e.target.value)} />
        </Col>
      </Row>
      <div className="table-responsive">
        {justificationDetailsList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  Solicitud <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th>Aprendiz</th>
                <th>Materia</th>
                <th>Período</th>
                <th>Tipo</th>
                <th>Plazo</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {justificationDetailsList.map(detail => (
                <tr key={`entity-${detail.id}`} data-cy="entityTable">
                  <td>
                    {detail.requestDate ? <TextFormat type="date" value={detail.requestDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                  </td>
                  <td>{detail.justification?.student?.documentNumber}</td>
                  <td>{detail.classSection?.subjectName}</td>
                  <td>
                    {detail.justification?.startDate ? (
                      <TextFormat type="date" value={detail.justification.startDate} format={APP_LOCAL_DATE_FORMAT} />
                    ) : null}
                    {' – '}
                    {detail.justification?.endDate ? (
                      <TextFormat type="date" value={detail.justification.endDate} format={APP_LOCAL_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{detail.justification?.justificationType?.name}</td>
                  <td>
                    {detail.justification?.onTime === undefined ? null : (
                      <Badge bg={detail.justification.onTime ? 'success' : 'danger'}>
                        {detail.justification.onTime ? 'En tiempo' : 'Fuera de tiempo'}
                      </Badge>
                    )}
                  </td>
                  <td>
                    <Badge bg={stateVariant[detail.stateJustification ?? ''] ?? 'secondary'}>{detail.stateJustification}</Badge>
                  </td>
                  <td className="text-end">
                    <Button
                      as={Link as any}
                      to={`/justification-details/${detail.id}`}
                      variant="info"
                      size="sm"
                      data-cy="entityDetailsButton"
                    >
                      <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">Revisar</span>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-success">No hay partes que coincidan con estos filtros.</div>
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

import React, { useEffect, useState } from 'react';
import { Badge, Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './justification.reducer';

const stateVariant: Record<string, string> = {
  PENDIENTE: 'warning',
  ACEPTADA: 'success',
  RECHAZADA: 'danger',
  CANCELADA: 'secondary',
};

export const Justification = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'startDate'), pageLocation.search),
  );

  const justificationList = useAppSelector(state => state.justification.entities);
  const loading = useAppSelector(state => state.justification.loading);
  const totalItems = useAppSelector(state => state.justification.totalItems);

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
      <h2 id="justification-heading" data-cy="JustificationHeading">
        Mis justificaciones
        <div className="d-flex justify-content-end">
          <Link to="/justification/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;Justificar fallas
          </Link>
        </div>
      </h2>
      <p>Consulta el estado de tus justificaciones de inasistencia. Cada materia afectada se decide por separado.</p>
      <div className="table-responsive">
        {justificationList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('startDate')}>
                  Período <FontAwesomeIcon icon={getSortIconByFieldName('startDate')} />
                </th>
                <th>Materias afectadas</th>
                <th>Tipo</th>
                <th>Plazo</th>
                <th>Estado de las partes</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {justificationList.map(justification => (
                <tr key={`entity-${justification.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/justification/${justification.id}`} variant="link" size="sm">
                      {justification.startDate ? (
                        <TextFormat type="date" value={justification.startDate} format={APP_LOCAL_DATE_FORMAT} />
                      ) : null}
                      {' – '}
                      {justification.endDate ? (
                        <TextFormat type="date" value={justification.endDate} format={APP_LOCAL_DATE_FORMAT} />
                      ) : null}
                    </Button>
                  </td>
                  <td>{(justification.detailses ?? []).map(d => d.classSection?.subjectName).join(', ')}</td>
                  <td>{justification.justificationType?.name}</td>
                  <td>
                    {justification.onTime === undefined ? null : (
                      <Badge bg={justification.onTime ? 'success' : 'danger'}>
                        {justification.onTime ? 'En tiempo' : 'Fuera de tiempo'}
                      </Badge>
                    )}
                  </td>
                  <td>
                    {(justification.detailses ?? []).map(d => (
                      <Badge key={d.id} bg={stateVariant[d.stateJustification ?? ''] ?? 'secondary'} className="me-1">
                        {d.classSection?.subjectName}: {d.stateJustification}
                      </Badge>
                    ))}
                  </td>
                  <td className="text-end">
                    <Button
                      as={Link as any}
                      to={`/justification/${justification.id}`}
                      variant="info"
                      size="sm"
                      data-cy="entityDetailsButton"
                    >
                      <FontAwesomeIcon icon="eye" />{' '}
                      <span className="d-none d-md-inline">
                        <Translate contentKey="entity.action.view">View</Translate>
                      </span>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-success">
              Aún no has presentado justificaciones. Si tienes fallas registradas, puedes justificarlas con el botón "Justificar fallas".
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={justificationList && justificationList.length > 0 ? '' : 'd-none'}>
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

export default Justification;

import React, { useEffect, useState } from 'react';
import { Table } from 'react-bootstrap';
import { Translate, getSortState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities, getActiveEntities } from './justification-type.reducer';

export const JustificationType = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const justificationTypeList = useAppSelector(state => state.justificationType.entities);
  const loading = useAppSelector(state => state.justificationType.loading);

  const filteredJustificationTypeList = justificationTypeList
    ?.filter(justificationType => justificationType.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(justificationType => (stateFilter === 'INACTIVE' ? justificationType.status === 'INACTIVO' : true));

  const getAllEntities = () => {
    if (stateFilter === 'ACTIVE') {
      dispatch(getActiveEntities());
    } else {
      dispatch(getEntities({ sort: `${sortState.sort},${sortState.order}` }));
    }
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
  }, [sortState.order, sortState.sort, stateFilter]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
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
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> <span className="current">TIPOS DE JUSTIFICACIÓN</span>
          </div>
          <h2 id="justification-type-heading" data-cy="JustificationTypeHeading" className="page-title">
            Gestión de Tipos de Justificación
          </h2>
          <p className="page-description">
            Administre las categorías y motivos válidos para la justificación de inasistencias en los procesos de formación.
          </p>
        </div>
        <div>
          <Link
            to="/justification-type/new"
            className="btn btn-success fw-bold"
            style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
          >
            <FontAwesomeIcon icon="plus" className="me-2" />
            Crear Nuevo Tipo
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
              <option value="ALL">Todos</option>
              <option value="ACTIVE">Activos</option>
              <option value="INACTIVE">Inactivos</option>
            </ValidatedInput>
          </div>
        </div>

        <div className="table-responsive">
          {filteredJustificationTypeList?.length > 0 ? (
            <Table className="custom-table" hover responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    NOMBRE <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('limitPerTrimester')}>
                    LÍMITE TRIMESTRAL <FontAwesomeIcon icon={getSortIconByFieldName('limitPerTrimester')} />
                  </th>
                  <th className="hand" onClick={sort('status')}>
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredJustificationTypeList.map(justificationType => (
                  <tr key={`entity-${justificationType.id}`} data-cy="entityTable">
                    <td className="fw-bold">{justificationType.name}</td>
                    <td>{justificationType.limitPerTrimester}</td>
                    <td>
                      {justificationType.status === 'ACTIVO' ? (
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
                        <Link to={`/justification-type/${justificationType.id}/edit`} title="Editar">
                          <FontAwesomeIcon icon="pencil-alt" />
                        </Link>
                        <Link to={`/justification-type/${justificationType.id}/delete`} className="delete" title="Eliminar">
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
                <Translate contentKey="senaAttendanceApp.justificationType.home.notFound">No Justification Types found</Translate>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
};

export default JustificationType;

import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { Translate, getSortState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faPlus, faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities, getActiveEntities } from './time-slot.reducer';
import LinkButton from 'app/shared/components/link-button';

export const TimeSlot = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const timeSlotList = useAppSelector(state => state.timeSlot.entities);
  const loading = useAppSelector(state => state.timeSlot.loading);

  const filteredTimeSlotList = timeSlotList
    ?.filter(timeSlot => timeSlot.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(timeSlot => (stateFilter === 'INACTIVE' ? !timeSlot.isActive : true));

  const getAllEntities = () => {
    if (stateFilter === 'ACTIVE') {
      dispatch(getActiveEntities({ sort: `${sortState.sort},${sortState.order}` }));
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

  const handleSyncList = () => {
    sortEntities();
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
      {/* 1. HEADER REUTILIZABLE */}
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> <span className="current">JORNADAS</span>
          </div>
          <h2 id="time-slot-heading" data-cy="TimeSlotHeading" className="page-title">
            Gestión de Jornadas
          </h2>
          <p className="page-description">
            Administre jornadas académicas y franjas horarias de formación del centro formativo. Configure horarios, habilite o deshabilite
            turnos según la disponibilidad.
          </p>
        </div>
        <div>
          <Link
            to="/time-slot/new"
            className="btn btn-success fw-bold"
            style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
          >
            <FontAwesomeIcon icon="plus" className="me-2" />
            Crear Nueva Jornada
          </Link>
        </div>
      </div>

      {/* 2. CONTENEDOR CARD Y TABLA */}
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
              <option value="ALL">Todas las jornadas</option>
              <option value="ACTIVE">Activas</option>
              <option value="INACTIVE">Inactivas</option>
            </ValidatedInput>
          </div>
        </div>

        <div className="table-responsive">
          {filteredTimeSlotList?.length > 0 ? (
            <Table className="custom-table" hover responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    NOMBRE <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('startTime')}>
                    HORA INICIO <FontAwesomeIcon icon={getSortIconByFieldName('startTime')} />
                  </th>
                  <th className="hand" onClick={sort('endTime')}>
                    HORA FIN <FontAwesomeIcon icon={getSortIconByFieldName('endTime')} />
                  </th>
                  <th className="hand" onClick={sort('isActive')}>
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('isActive')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredTimeSlotList.map(timeSlot => (
                  <tr key={`entity-${timeSlot.id}`} data-cy="entityTable">
                    <td className="fw-bold">{timeSlot.name}</td>
                    <td>{timeSlot.startTime}</td>
                    <td>{timeSlot.endTime}</td>
                    <td>
                      {timeSlot.isActive ? (
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
                        <Link to={`/time-slot/${timeSlot.id}/edit`} title="Editar">
                          <FontAwesomeIcon icon="pencil-alt" />
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
                <Translate contentKey="senaAttendanceApp.timeSlot.home.notFound">No Time Slots found</Translate>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
};

export default TimeSlot;

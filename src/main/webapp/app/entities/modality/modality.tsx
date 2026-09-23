import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { Translate, getSortState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities, getActiveEntities } from './modality.reducer';
import LinkButton from 'app/shared/components/link-button';

export const Modality = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const modalityList = useAppSelector(state => state.modality.entities);
  const loading = useAppSelector(state => state.modality.loading);

  const filteredModalityList = modalityList
    ?.filter(modality => modality.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(modality => (stateFilter === 'INACTIVE' ? !modality.isActive : true));

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
            INICIO <span className="separator">/</span> <span className="current">MODALIDADES</span>
          </div>
          <h2 id="modality-heading" data-cy="ModalityHeading" className="page-title">
            Gestión de Modalidades
          </h2>
          <p className="page-description">
            Administre los tipos y modalidades de formacion ofertadas en el centro institucional (Presencial, Virtual, Mixta, etc.).
            Configure disponibilidad y estados.
          </p>
        </div>
        <div>
          <Link
            to="/modality/new"
            className="btn btn-success fw-bold"
            style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
          >
            <FontAwesomeIcon icon="plus" className="me-2" />
            Crear Nueva Modalidad
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
              <option value="ALL">Todas las modalidades</option>
              <option value="ACTIVE">Activas</option>
              <option value="INACTIVE">Inactivas</option>
            </ValidatedInput>
          </div>
        </div>

        <div className="table-responsive">
          {filteredModalityList?.length > 0 ? (
            <Table className="custom-table" hover responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    NOMBRE <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('isActive')}>
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('isActive')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredModalityList.map(modality => (
                  <tr key={`entity-${modality.id}`} data-cy="entityTable">
                    <td className="fw-bold">{modality.name}</td>
                    <td>
                      {modality.isActive ? (
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
                        <Link to={`/modality/${modality.id}/edit`} title="Editar">
                          <FontAwesomeIcon icon="pencil-alt" />
                        </Link>
                        <Link to={`/modality/${modality.id}/delete`} className="delete" title="Eliminar">
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
                <Translate contentKey="senaAttendanceApp.modality.home.notFound">No Modalities found</Translate>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
};

export default Modality;

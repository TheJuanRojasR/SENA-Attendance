import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { Translate, getSortState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities, getActiveEntities } from './document-type.reducer';
import LinkButton from 'app/shared/components/link-button';

export const DocumentType = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const documentTypeList = useAppSelector(state => state.documentType.entities);
  const loading = useAppSelector(state => state.documentType.loading);

  const filteredDocumentTypeList = documentTypeList
    ?.filter(documentType => documentType.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(documentType => (stateFilter === 'INACTIVE' ? !documentType.isActive : true));

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
      {/* 1. HEADER REUTILIZABLE */}
      <div className="entity-page-header">
        <div>
          <div className="breadcrumb-text">
            INICIO <span className="separator">/</span> <span className="current">TIPOS DE DOCUMENTO</span>
          </div>
          <h2 id="document-type-heading" data-cy="DocumentTypeHeading" className="page-title">
            Gestión de Tipos de Documento
          </h2>
          <p className="page-description">
            Administre los tipos de documento válidos para la identificación y registro institucional de usuarios, instructores y aprendices
            en la plataforma SENA.
          </p>
        </div>
        <div>
          <Link
            to="/document-type/new"
            className="btn btn-success fw-bold"
            style={{ backgroundColor: '#388e3c', borderColor: '#388e3c', borderRadius: '8px', padding: '10px 20px' }}
          >
            <FontAwesomeIcon icon="plus" className="me-2" />
            Crear Nuevo Tipo de Documento
          </Link>
        </div>
      </div>

      {/* 2. CONTENEDOR CARD Y TABLA */}
      <div className="entity-card">
        {/* Barra de Herramientas (Buscador y Filtro) */}
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-3">
          <div className="search-input-wrapper">
            <FontAwesomeIcon icon="search" />
            <input
              type="text"
              className="form-control"
              placeholder="Buscar por nombre o iniciales de documento..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
          <div className="d-flex align-items-center">
            <span className="me-2 text-muted fw-bold" style={{ fontSize: '0.80rem', letterSpacing: '0.5px' }}>
              ESTADO:
            </span>
            <select
              className="form-control"
              style={{ width: 'auto', borderRadius: '8px', height: '42px' }}
              value={stateFilter}
              onChange={e => setStateFilter(e.target.value)}
            >
              <option value="ALL">Todos los estados</option>
              <option value="ACTIVE">Activos</option>
              <option value="INACTIVE">Inactivos</option>
            </select>
          </div>
        </div>

        {/* Tabla */}
        <div className="table-responsive">
          {filteredDocumentTypeList && filteredDocumentTypeList.length > 0 ? (
            <Table className="custom-table" hover>
              <thead>
                <tr>
                  <th onClick={sort('name')} className="hand">
                    NOMBRE DEL DOCUMENTO <FontAwesomeIcon icon={getSortIconByFieldName('name')} className="ms-1" />
                  </th>
                  <th onClick={sort('initials')} className="hand text-center">
                    INICIALES <FontAwesomeIcon icon={getSortIconByFieldName('initials')} className="ms-1" />
                  </th>
                  <th onClick={sort('isActive')} className="hand text-center">
                    ESTADO <FontAwesomeIcon icon={getSortIconByFieldName('isActive')} className="ms-1" />
                  </th>
                  <th className="text-end">ACCIONES</th>
                </tr>
              </thead>
              <tbody>
                {filteredDocumentTypeList.map(documentType => (
                  <tr key={`entity-${documentType.id}`} data-cy="entityTable">
                    <td className="fw-bold">{documentType.name}</td>
                    <td className="text-center">
                      <span className="badge-initials">{documentType.initials}</span>
                    </td>
                    <td className="text-center">
                      {documentType.isActive ? (
                        <span className="badge-status active">
                          <span className="dot"></span> Activo
                        </span>
                      ) : (
                        <span className="badge-status inactive">
                          <span className="dot"></span> Inactivo
                        </span>
                      )}
                    </td>
                    <td>
                      <div className="action-icons">
                        <Link to={`/document-type/${documentType.id}/edit`} title="Editar">
                          <FontAwesomeIcon icon="pencil-alt" />
                        </Link>
                        {/* Botón de apagar/encender */}
                        <button className="power" title={documentType.isActive ? 'Desactivar' : 'Activar'}>
                          <FontAwesomeIcon icon="power-off" />
                        </button>
                        <Link to={`/document-type/${documentType.id}/delete`} className="delete" title="Eliminar">
                          <FontAwesomeIcon icon="trash" />
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : (
            !loading && <div className="alert alert-warning mt-4">No se encontraron Tipos de Documento</div>
          )}
        </div>

        {/* Footer (Paginador Visual) */}
        <div className="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
          <span className="text-muted" style={{ fontSize: '0.85rem' }}>
            Mostrando 1 a {filteredDocumentTypeList?.length || 0} de {filteredDocumentTypeList?.length || 0} registros
          </span>
          <ul className="pagination mb-0" style={{ gap: '5px' }}>
            <li className="page-item disabled">
              <span className="page-link border-0 text-muted">&lt;</span>
            </li>
            <li className="page-item active">
              <span
                className="page-link"
                style={{ backgroundColor: '#fff', color: '#2e7d32', borderColor: '#43a047', borderRadius: '4px', fontWeight: 'bold' }}
              >
                1
              </span>
            </li>
            <li className="page-item disabled">
              <span className="page-link border-0 text-muted">&gt;</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default DocumentType;

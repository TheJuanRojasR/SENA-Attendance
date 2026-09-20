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
      <h2 id="document-type-heading" data-cy="DocumentTypeHeading">
        <Translate contentKey="senaAttendanceApp.documentType.home.title">Document Types</Translate>
      </h2>
      <p>
        {' '}
        Administre los tipos de documento válidos para la identificación y registro institucional de usuarios, instructores y aprendices en
        la plataforma.{' '}
      </p>
      <Col className="d-flex justify-content-between align-items-center" md="12">
        <div className="d-flex align-items-center entitiesSearchBar">
          <div className="d-flex align-items-center w-50">
            <FontAwesomeIcon icon={faSearch}></FontAwesomeIcon>
            <ValidatedInput name="search" placeholder="Buscar por nombre..." value={search} onChange={e => setSearch(e.target.value)} />
          </div>
          <ValidatedInput type="select" name="state" className="w-25" value={stateFilter} onChange={e => setStateFilter(e.target.value)}>
            <option value="ALL">Todas</option>
            <option value="ACTIVE">Activas</option>
            <option value="INACTIVE">Inactivas</option>
          </ValidatedInput>
        </div>
        <LinkButton to="/document-type/new" data-cy="entityCreateButton">
          <FontAwesomeIcon icon="plus" />
          <Translate contentKey="senaAttendanceApp.documentType.home.createLabel">Create new Document Type</Translate>
        </LinkButton>
      </Col>
      <div className="table-responsive">
        {filteredDocumentTypeList?.length > 0 ? (
          <Card>
            <Table responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    <Translate contentKey="senaAttendanceApp.documentType.name">Name</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('initials')}>
                    <Translate contentKey="senaAttendanceApp.documentType.initials">Initials</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('initials')} />
                  </th>
                  <th className="hand" onClick={sort('id')}>
                    <Translate contentKey="senaAttendanceApp.documentType.isActive">State</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('isActive')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredDocumentTypeList.map(documentType => (
                  <tr key={`entity-${documentType.id}`} data-cy="entityTable">
                    <td>{documentType.name}</td>
                    <td>{documentType.initials}</td>
                    <td>{documentType.isActive ? 'true' : 'false'}</td>
                    <td className="text-end">
                      <div className="btn-group flex-btn-group-container">
                        <Button
                          as={Link as any}
                          to={`/document-type/${documentType.id}/edit`}
                          variant="primary"
                          size="sm"
                          data-cy="entityEditButton"
                        >
                          <FontAwesomeIcon icon="pencil-alt" />{' '}
                          <span className="d-none d-md-inline">
                            <Translate contentKey="entity.action.edit">Edit</Translate>
                          </span>
                        </Button>
                        <Button
                          onClick={() => (globalThis.location.href = `/document-type/${documentType.id}/delete`)}
                          variant="danger"
                          size="sm"
                          data-cy="entityDeleteButton"
                        >
                          <FontAwesomeIcon icon="trash" />{' '}
                          <span className="d-none d-md-inline">
                            <Translate contentKey="entity.action.delete">Delete</Translate>
                          </span>
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card>
        ) : (
          !loading && (
            <div className="alert alert-success">
              <Translate contentKey="senaAttendanceApp.documentType.home.notFound">No Document Types found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default DocumentType;

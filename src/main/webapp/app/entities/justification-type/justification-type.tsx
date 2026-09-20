import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { Translate, getSortState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC } from 'app/shared/util/pagination.constants';

import { getEntities, getActiveEntities } from './justification-type.reducer';
import LinkButton from 'app/shared/components/link-button';

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
      <h2 id="justification-type-heading" data-cy="JustificationTypeHeading">
        <Translate contentKey="senaAttendanceApp.justificationType.home.title">Justification Types</Translate>
      </h2>
      <p>
        Administre las categorías y motivos válidos para la justificación de inasistencias en los procesos de formación. Configure los
        estados y criterios aplicables para el soporte de las ausencias de los aprendices.
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
        <LinkButton to="/justification-type/new" data-cy="entityCreateButton">
          <FontAwesomeIcon icon="plus" />
          <Translate contentKey="senaAttendanceApp.justificationType.home.createLabel">Create new Justification Type</Translate>
        </LinkButton>
      </Col>
      <div className="table-responsive">
        {filteredJustificationTypeList?.length > 0 ? (
          <Card>
            <Table responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    <Translate contentKey="senaAttendanceApp.justificationType.name">Name</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('limitPerTrimester')}>
                    <Translate contentKey="senaAttendanceApp.justificationType.limitPerTrimester">Limit Per Trimester</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('limitPerTrimester')} />
                  </th>
                  <th className="hand" onClick={sort('status')}>
                    <Translate contentKey="senaAttendanceApp.justificationType.status">State</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('status')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredJustificationTypeList.map(justificationType => (
                  <tr key={`entity-${justificationType.id}`} data-cy="entityTable">
                    <td>{justificationType.name}</td>
                    <td>{justificationType.limitPerTrimester}</td>
                    <td>
                      <Translate contentKey={`senaAttendanceApp.State.${justificationType.status}`} />
                    </td>
                    <td className="text-end">
                      <div className="btn-group flex-btn-group-container">
                        <Button
                          as={Link as any}
                          to={`/justification-type/${justificationType.id}/edit`}
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
                          onClick={() => (globalThis.location.href = `/justification-type/${justificationType.id}/delete`)}
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
              <Translate contentKey="senaAttendanceApp.justificationType.home.notFound">No Justification Types found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default JustificationType;

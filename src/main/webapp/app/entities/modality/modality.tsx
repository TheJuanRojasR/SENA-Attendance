import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { Translate, getSortState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch } from '@fortawesome/free-solid-svg-icons';
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

  return (
    <div>
      <h2 id="modality-heading" data-cy="ModalityHeading">
        <Translate contentKey="senaAttendanceApp.modality.home.title">Modalities</Translate>
      </h2>
      <p>
        Administre los tipos y modalidades de formacion ofertadas en el centro institucional (Presencial, Virtual, Mixta, etc.). Configure
        disponibilidad y estados según los programas formativos
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
        <LinkButton to="/modality/new" data-cy="entityCreateButton">
          <FontAwesomeIcon icon="plus" />{' '}
          <Translate contentKey="senaAttendanceApp.modality.home.createLabel">Create new Modality</Translate>
        </LinkButton>
      </Col>
      <div className="table-responsive">
        {filteredModalityList?.length > 0 ? (
          <Card>
            <Table responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    <Translate contentKey="senaAttendanceApp.modality.name">Name</Translate>{' '}
                  </th>
                  <th className="hand" onClick={sort('isActive')}>
                    <Translate contentKey="senaAttendanceApp.modality.isActive">Is Active</Translate>{' '}
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredModalityList.map(modality => (
                  <tr key={`entity-${modality.id}`} data-cy="entityTable">
                    <td>{modality.name}</td>
                    <td>{modality.isActive ? 'true' : 'false'}</td>
                    <td className="text-end">
                      <div className="btn-group flex-btn-group-container">
                        <Button
                          as={Link as any}
                          to={`/modality/${modality.id}/edit`}
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
                          onClick={() => (globalThis.location.href = `/modality/${modality.id}/delete`)}
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
              <Translate contentKey="senaAttendanceApp.modality.home.notFound">No Modalities found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Modality;

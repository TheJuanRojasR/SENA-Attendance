import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, Translate, ValidatedInput, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities, getActiveEntities } from './modality.reducer';
import LinkButton from 'app/shared/components/link-button';

export const Modality = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const modalityList = useAppSelector(state => state.modality.entities);
  const loading = useAppSelector(state => state.modality.loading);
  const totalItems = useAppSelector(state => state.modality.totalItems);

  // GET /api/modalities no admite búsqueda por servidor: el nombre se filtra sobre la página
  // actual, ya cargada y paginada de verdad (page/size/sort van al backend).
  const filteredModalityList = modalityList
    ?.filter(modality => modality.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(modality => (stateFilter === 'INACTIVE' ? !modality.isActive : true));

  const getAllEntities = () => {
    if (stateFilter === 'ACTIVE') {
      dispatch(getActiveEntities({ sort: `${paginationState.sort},${paginationState.order}` }));
    } else {
      dispatch(
        getEntities({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    }
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
  }, [paginationState.activePage, paginationState.order, paginationState.sort, stateFilter]);

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
                          to={`/modality/${modality.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          onClick={() =>
                            (globalThis.location.href = `/modality/${modality.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                          }
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
      {totalItems && stateFilter !== 'ACTIVE' ? (
        <div className={filteredModalityList && filteredModalityList.length > 0 ? '' : 'd-none'}>
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

export default Modality;

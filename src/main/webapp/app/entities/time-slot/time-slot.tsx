import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, Translate, ValidatedInput, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faPlus, faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getActiveEntities, getEntities } from './time-slot.reducer';
import LinkButton from 'app/shared/components/link-button';

export const TimeSlot = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  const timeSlotList = useAppSelector(state => state.timeSlot.entities);
  const loading = useAppSelector(state => state.timeSlot.loading);
  const totalItems = useAppSelector(state => state.timeSlot.totalItems);

  // GET /api/time-slots no admite búsqueda por servidor: el nombre se filtra sobre la página
  // actual, ya cargada y paginada de verdad (page/size/sort van al backend).
  const filteredTimeSlotList = timeSlotList
    ?.filter(timeSlot => timeSlot.name?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(timeSlot => (stateFilter === 'INACTIVE' ? !timeSlot.isActive : true));

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

  const handleSyncList = () => {
    sortEntities();
  };

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
      <h2 id="time-slot-heading" data-cy="TimeSlotHeading">
        <Translate contentKey="senaAttendanceApp.timeSlot.home.title">Time Slots</Translate>
      </h2>
      <p>
        Administre jornadas académicas y franjas horarias de formación del centro formativo. Configure horarios, habilite o deshabilite
        turnos según la disponibilidad
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
        <LinkButton to="new" data-cy="entityCreateButton">
          <FontAwesomeIcon icon={faPlus} /> <Translate contentKey="userManagement.home.createLabel">Create a new user</Translate>
        </LinkButton>
      </Col>
      <div className="table-responsive">
        {filteredTimeSlotList?.length > 0 ? (
          <Card>
            <Table responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('name')}>
                    <Translate contentKey="senaAttendanceApp.timeSlot.name">Name</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                  </th>
                  <th className="hand" onClick={sort('startTime')}>
                    <Translate contentKey="senaAttendanceApp.timeSlot.startTime">Start Time</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('startTime')} />
                  </th>
                  <th className="hand" onClick={sort('endTime')}>
                    <Translate contentKey="senaAttendanceApp.timeSlot.endTime">End Time</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('endTime')} />
                  </th>
                  <th className="hand" onClick={sort('isActive')}>
                    <Translate contentKey="senaAttendanceApp.timeSlot.isActive">Is Active</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('isActive')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredTimeSlotList.map(timeSlot => (
                  <tr key={`entity-${timeSlot.id}`} data-cy="entityTable">
                    <td>{timeSlot.name}</td>
                    <td>{timeSlot.startTime}</td>
                    <td>{timeSlot.endTime}</td>
                    <td>{timeSlot.isActive ? 'true' : 'false'}</td>
                    <td className="text-end">
                      <div className="btn-group flex-btn-group-container">
                        <Button
                          as={Link as any}
                          to={`/time-slot/${timeSlot.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                          variant="primary"
                          size="sm"
                          data-cy="entityEditButton"
                        >
                          <FontAwesomeIcon icon="pencil-alt" />{' '}
                          <span className="d-none d-md-inline">
                            <Translate contentKey="entity.action.edit">Edit</Translate>
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
              <Translate contentKey="senaAttendanceApp.timeSlot.home.notFound">No Time Slots found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems && stateFilter !== 'ACTIVE' ? (
        <div className={filteredTimeSlotList && filteredTimeSlotList.length > 0 ? '' : 'd-none'}>
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

export default TimeSlot;

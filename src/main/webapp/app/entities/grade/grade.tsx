import React, { useEffect, useState } from 'react';
import { Button, Card, Col, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSearch, faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getActiveEntities, getEntities } from './grade.reducer';

export const Grade = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL'); // 'ALL' | 'PENDIENTE' | 'ACTIVA' | 'FINALIZADA' | 'APLAZADA' | 'CANCELADA'

  const gradeList = useAppSelector(state => state.grade.entities);
  const loading = useAppSelector(state => state.grade.loading);
  const totalItems = useAppSelector(state => state.grade.totalItems);

  // No existe un endpoint de búsqueda para fichas: se filtra en cliente sobre la página cargada.
  const filteredGradeList = gradeList
    ?.filter(grade => grade.code?.toLowerCase().includes(search.trim().toLowerCase()))
    .filter(grade => (stateFilter === 'ALL' ? true : grade.state === stateFilter));

  const getAllEntities = () => {
    if (stateFilter === 'ACTIVA') {
      dispatch(getActiveEntities());
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
      <h2 id="grade-heading" data-cy="GradeHeading">
        <Translate contentKey="senaAttendanceApp.grade.home.title">Grades</Translate>
      </h2>
      <p> Administre las fichas de formación, asigne programas asociados y controle el estado operativo en el centro de formación. </p>
      <Col className="d-flex justify-content-between align-items-center" md="12">
        <div className="d-flex align-items-center entitiesSearchBar">
          <div className="d-flex align-items-center w-50">
            <FontAwesomeIcon icon={faSearch}></FontAwesomeIcon>
            <ValidatedInput name="search" placeholder="Buscar por código..." value={search} onChange={e => setSearch(e.target.value)} />
          </div>
          <ValidatedInput type="select" name="state" className="w-25" value={stateFilter} onChange={e => setStateFilter(e.target.value)}>
            <option value="ALL">Todas</option>
            <option value="PENDIENTE">Pendientes</option>
            <option value="ACTIVA">Activas</option>
            <option value="FINALIZADA">Finalizadas</option>
            <option value="APLAZADA">Aplazadas</option>
            <option value="CANCELADA">Canceladas</option>
          </ValidatedInput>
        </div>
        <Link to="/grade/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
          <FontAwesomeIcon icon="plus" />
          &nbsp;
          <Translate contentKey="senaAttendanceApp.grade.home.createLabel">Create new Grade</Translate>
        </Link>
      </Col>
      <div className="table-responsive">
        {filteredGradeList?.length > 0 ? (
          <Card>
            <Table responsive>
              <thead>
                <tr>
                  <th className="hand" onClick={sort('code')}>
                    <Translate contentKey="senaAttendanceApp.grade.code">Code</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('code')} />
                  </th>
                  <th>
                    <Translate contentKey="senaAttendanceApp.grade.program">Program</Translate> <FontAwesomeIcon icon="sort" />
                  </th>
                  <th className="hand" onClick={sort('state')}>
                    <Translate contentKey="senaAttendanceApp.grade.state">State</Translate>{' '}
                    <FontAwesomeIcon icon={getSortIconByFieldName('state')} />
                  </th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filteredGradeList.map(grade => (
                  <tr key={`entity-${grade.id}`} data-cy="entityTable">
                    <td>{grade.code}</td>
                    <td>{grade.program.name}</td>
                    <td>
                      <Translate contentKey={`senaAttendanceApp.StateGrade.${grade.state}`} />
                    </td>
                    <td className="text-end">
                      <div className="btn-group flex-btn-group-container">
                        <Button
                          as={Link as any}
                          to={`/grade/${grade.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                            (globalThis.location.href = `/grade/${grade.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="senaAttendanceApp.grade.home.notFound">No Grades found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={filteredGradeList && filteredGradeList.length > 0 ? '' : 'd-none'}>
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

export default Grade;

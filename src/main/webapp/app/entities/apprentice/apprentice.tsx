import React, { useEffect, useState } from 'react';
import { Button, Col, Row, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, Translate, ValidatedInput, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getGrades } from 'app/entities/grade/grade.reducer';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';
import { StateAcademic } from 'app/shared/model/enumerations/state-academic.model';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './apprentice.reducer';

export const Apprentice = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );
  const [documentNumber, setDocumentNumber] = useState('');
  const [name, setName] = useState('');
  const [stateAcademic, setStateAcademic] = useState('');
  const [gradeId, setGradeId] = useState('');

  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.ADMIN]));
  const apprenticeList = useAppSelector(state => state.apprentice.entities);
  const grades = useAppSelector(state => state.grade.entities);
  const loading = useAppSelector(state => state.apprentice.loading);
  const totalItems = useAppSelector(state => state.apprentice.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
        documentNumber: documentNumber || undefined,
        name: name || undefined,
        stateAcademic: stateAcademic || undefined,
        gradeId: gradeId || undefined,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    dispatch(getGrades({}));
  }, []);

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, documentNumber, name, stateAcademic, gradeId]);

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

  const resetFilterPage = () => setPaginationState({ ...paginationState, activePage: 1 });

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
      <h2 id="apprentice-heading" data-cy="ApprenticeHeading">
        <Translate contentKey="senaAttendanceApp.apprentice.home.title">Apprentices</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" variant="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="senaAttendanceApp.apprentice.home.refreshListLabel">Refresh List</Translate>
          </Button>
          {isAdmin && (
            <Link to="/apprentice/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
              <FontAwesomeIcon icon="plus" />
              &nbsp;
              <Translate contentKey="senaAttendanceApp.apprentice.home.createLabel">Vincular aprendiz</Translate>
            </Link>
          )}
        </div>
      </h2>
      <Row className="mb-3">
        <Col md="3">
          <ValidatedInput
            name="documentNumber"
            placeholder="Buscar por documento..."
            value={documentNumber}
            onChange={e => {
              setDocumentNumber(e.target.value);
              resetFilterPage();
            }}
          />
        </Col>
        <Col md="3">
          <ValidatedInput
            name="name"
            placeholder="Buscar por nombre..."
            value={name}
            onChange={e => {
              setName(e.target.value);
              resetFilterPage();
            }}
          />
        </Col>
        <Col md="3">
          <ValidatedInput
            type="select"
            name="stateAcademic"
            value={stateAcademic}
            onChange={e => {
              setStateAcademic(e.target.value);
              resetFilterPage();
            }}
          >
            <option value="">Todos los estados</option>
            {Object.keys(StateAcademic).map(value => (
              <option value={value} key={value}>
                <Translate contentKey={`senaAttendanceApp.StateAcademic.${value}`}>{value}</Translate>
              </option>
            ))}
          </ValidatedInput>
        </Col>
        <Col md="3">
          <ValidatedInput
            type="select"
            name="gradeId"
            value={gradeId}
            onChange={e => {
              setGradeId(e.target.value);
              resetFilterPage();
            }}
          >
            <option value="">Todas las fichas</option>
            {grades.map(grade => (
              <option value={grade.id} key={grade.id}>
                {grade.code}
              </option>
            ))}
          </ValidatedInput>
        </Col>
      </Row>
      <div className="table-responsive">
        {apprenticeList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="senaAttendanceApp.apprentice.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('stateAcademic')}>
                  <Translate contentKey="senaAttendanceApp.apprentice.stateAcademic">State Academic</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('stateAcademic')} />
                </th>
                <th>
                  <Translate contentKey="senaAttendanceApp.apprentice.student">Student</Translate>
                </th>
                <th>
                  <Translate contentKey="senaAttendanceApp.apprentice.grade">Grade</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {apprenticeList.map(apprentice => (
                <tr key={`entity-${apprentice.id}`} data-cy="entityTable">
                  <td>
                    <Button as={Link as any} to={`/apprentice/${apprentice.id}`} variant="link" size="sm">
                      {apprentice.id}
                    </Button>
                  </td>
                  <td>
                    <Translate contentKey={`senaAttendanceApp.StateAcademic.${apprentice.stateAcademic}`} />
                  </td>
                  <td>
                    {apprentice.student ? (
                      <Link to={`/user-profile/${apprentice.student.id}`}>
                        {apprentice.student.firstName} {apprentice.student.firstLastName} ({apprentice.student.documentNumber})
                      </Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>{apprentice.grade ? <Link to={`/grade/${apprentice.grade.id}`}>{apprentice.grade.code}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button as={Link as any} to={`/apprentice/${apprentice.id}`} variant="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      {isAdmin && (
                        <Button
                          as={Link as any}
                          to={`/apprentice/${apprentice.id}/unlink`}
                          variant="danger"
                          size="sm"
                          data-cy="entityUnlinkButton"
                        >
                          <FontAwesomeIcon icon="right-from-bracket" />{' '}
                          <span className="d-none d-md-inline">
                            <Translate contentKey="senaAttendanceApp.apprentice.unlink.confirm">Desvincular</Translate>
                          </span>
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-success">
              <Translate contentKey="senaAttendanceApp.apprentice.home.notFound">No Apprentices found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={apprenticeList && apprenticeList.length > 0 ? '' : 'd-none'}>
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

export default Apprentice;

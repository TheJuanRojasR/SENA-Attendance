import React, { useEffect, useState } from 'react';
import { Badge, Button, Col, Row, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getMine } from 'app/entities/class-section/class-section.reducer';
import { StateAttendance } from 'app/shared/model/enumerations/state-attendance.model';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities } from './attendance.reducer';

// A1: historial de asistencia. El backend ya acota por rol (instructor -> sus materias,
// aprendiz -> las suyas), así que este mismo listado sirve para ambos.
export const Attendance = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'date'), pageLocation.search),
  );
  const [classSectionId, setClassSectionId] = useState('');
  const [date, setDate] = useState('');
  const [stateAttendance, setStateAttendance] = useState('');

  const attendanceList = useAppSelector(state => state.attendance.entities);
  const myClassSections = useAppSelector(state => state.classSection.mine);
  const loading = useAppSelector(state => state.attendance.loading);
  const totalItems = useAppSelector(state => state.attendance.totalItems);
  const isInstructor = myClassSections.length > 0;

  useEffect(() => {
    dispatch(getMine());
  }, []);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
        classSectionId: classSectionId || undefined,
        date: date || undefined,
        stateAttendance: stateAttendance || undefined,
      }),
    );
  };

  useEffect(() => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  }, [paginationState.activePage, paginationState.order, paginationState.sort, classSectionId, date, stateAttendance]);

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

  const handlePagination = currentPage => setPaginationState({ ...paginationState, activePage: currentPage });

  const resetToFirstPage = () => setPaginationState({ ...paginationState, activePage: 1 });

  return (
    <div>
      <h2 id="attendance-heading" data-cy="AttendanceHeading" className="d-flex justify-content-between align-items-center">
        Historial de asistencia
        {isInstructor && (
          <Button as={Link as any} to="/attendance/session" variant="primary" size="sm">
            <FontAwesomeIcon icon="clipboard-list" /> Tomar asistencia
          </Button>
        )}
      </h2>
      <Row className="mb-3">
        {isInstructor && (
          <Col md="4">
            <select
              className="form-select"
              value={classSectionId}
              onChange={e => {
                setClassSectionId(e.target.value);
                resetToFirstPage();
              }}
            >
              <option value="">Todas mis materias</option>
              {myClassSections.map(cs => (
                <option value={cs.id} key={cs.id}>
                  {cs.subjectName} — Ficha {cs.grade?.code}
                </option>
              ))}
            </select>
          </Col>
        )}
        <Col md="3">
          <input
            type="date"
            className="form-control"
            value={date}
            onChange={e => {
              setDate(e.target.value);
              resetToFirstPage();
            }}
          />
        </Col>
        <Col md="3">
          <select
            className="form-select"
            value={stateAttendance}
            onChange={e => {
              setStateAttendance(e.target.value);
              resetToFirstPage();
            }}
          >
            <option value="">Todos los estados</option>
            {Object.keys(StateAttendance).map(value => (
              <option value={value} key={value}>
                {value}
              </option>
            ))}
          </select>
        </Col>
      </Row>
      <div className="table-responsive">
        {attendanceList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Materia</th>
                <th>Aprendiz</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {attendanceList.map(attendance => (
                <tr key={`entity-${attendance.id}`} data-cy="entityTable">
                  <td>{attendance.date ? attendance.date.toString() : ''}</td>
                  <td>{attendance.classSection?.subjectName}</td>
                  <td>
                    {attendance.student?.firstName} {attendance.student?.firstLastName}
                  </td>
                  <td>
                    <Badge
                      bg={
                        attendance.stateAttendance === 'FALLA'
                          ? 'danger'
                          : attendance.stateAttendance === 'JUSTIFICADA'
                            ? 'info'
                            : 'success'
                      }
                    >
                      {attendance.stateAttendance}
                    </Badge>
                  </td>
                  <td className="text-end">
                    {attendance.stateAttendance !== 'JUSTIFICADA' && (
                      <Button as={Link as any} to={`/attendance/${attendance.id}/edit`} variant="primary" size="sm">
                        <FontAwesomeIcon icon="pencil-alt" />
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-success">No se encontraron registros de asistencia.</div>
        )}
      </div>
      {totalItems ? (
        <div className={attendanceList?.length > 0 ? '' : 'd-none'}>
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

export default Attendance;

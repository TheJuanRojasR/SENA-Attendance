import React, { useEffect, useMemo, useState } from 'react';
import { Badge, Card, Col, Row, Table } from 'react-bootstrap';
import { TextFormat, Translate } from 'react-jhipster';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch } from '@fortawesome/free-solid-svg-icons';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { IClassSection } from 'app/shared/model/class-section.model';
import { IGrade } from 'app/shared/model/grade.model';

import { getMine } from './class-section.reducer';

const stateVariant: Record<string, string> = {
  PENDIENTE: 'info',
  ACTIVA: 'success',
  FINALIZADA: 'secondary',
  APLAZADA: 'warning',
  CANCELADA: 'danger',
};

interface IGradeGroup {
  grade: IGrade;
  classSections: IClassSection[];
}

// UC017, flujo básico: el instructor no se "une" a una ficha, su vinculación nace de sus
// materias asignadas (GET /api/class-sections/mine). Las fichas se derivan agrupando esa lista
// plana por ficha; es de solo lectura, no hay acciones que modifiquen nada aquí.
export const ClassSectionMine = () => {
  const dispatch = useAppDispatch();

  const [search, setSearch] = useState('');

  const mine = useAppSelector(state => state.classSection.mine);
  const loading = useAppSelector(state => state.classSection.loading);

  useEffect(() => {
    dispatch(getMine(search || undefined));
  }, [search]);

  const gradeGroups = useMemo<IGradeGroup[]>(() => {
    const groups = new Map<string, IGradeGroup>();
    mine.forEach(classSection => {
      const grade = classSection.grade;
      if (!grade?.id) {
        return;
      }
      if (!groups.has(grade.id)) {
        groups.set(grade.id, { grade, classSections: [] });
      }
      groups.get(grade.id)!.classSections.push(classSection);
    });
    return [...groups.values()].sort((a, b) => (a.grade.code ?? '').localeCompare(b.grade.code ?? ''));
  }, [mine]);

  return (
    <div>
      <h2>Mis fichas</h2>
      <p>Fichas y materias en las que estás asignado como instructor.</p>
      <Row className="mb-3">
        <Col md="4">
          <div className="d-flex align-items-center">
            <FontAwesomeIcon icon={faSearch} className="me-2" />
            <input
              type="text"
              className="form-control"
              placeholder="Buscar por número de ficha..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
        </Col>
      </Row>

      {!loading && gradeGroups.length === 0 && search && <div className="alert alert-info">No se encontraron fichas con ese número.</div>}
      {!loading && gradeGroups.length === 0 && !search && (
        <div className="alert alert-info">Aún no tienes materias asignadas. Contacta al Administrador.</div>
      )}

      {gradeGroups.map(({ grade, classSections }) => {
        const startDate = grade.startDate;
        const endDate = grade.endDate;
        return (
          <Card className="mb-3" key={grade.id}>
            <Card.Body>
              <div className="d-flex justify-content-between align-items-start flex-wrap">
                <div>
                  <Card.Title>
                    Ficha {grade.code}
                    <Badge bg={stateVariant[grade.state ?? ''] ?? 'secondary'} className="ms-2">
                      <Translate contentKey={`senaAttendanceApp.StateGrade.${grade.state}`}>{grade.state}</Translate>
                    </Badge>
                  </Card.Title>
                  <Card.Subtitle className="text-muted mb-2">{grade.program?.name}</Card.Subtitle>
                  <p className="text-muted mb-2">
                    {startDate ? <TextFormat value={startDate as unknown as Date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
                    {' – '}
                    {endDate ? <TextFormat value={endDate as unknown as Date} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
                  </p>
                </div>
              </div>
              <Table responsive size="sm" className="mb-0">
                <thead>
                  <tr>
                    <th>Materia</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {classSections.map(classSection => (
                    <tr key={classSection.id}>
                      <td>{classSection.subjectName}</td>
                      <td>
                        <Badge bg={classSection.isActive ? 'success' : 'secondary'}>{classSection.isActive ? 'Activa' : 'Inactiva'}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        );
      })}
    </div>
  );
};

export default ClassSectionMine;

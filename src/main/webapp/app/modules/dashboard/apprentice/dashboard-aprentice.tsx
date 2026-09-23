import React, { useEffect } from 'react';
import { Alert, Badge, Card, Table } from 'react-bootstrap';
import { Link } from 'react-router';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getDashboard } from 'app/modules/dashboard/dashboard.reducer';

// UC023, panel de Aprendiz: attendance/failuresByGrade/upcomingClasses dependen del trimestre
// activo y viajan vacíos con trimesterMessage cuando no hay uno (E2); justifications, grades y
// activeAlerts se calculan siempre.
export const AprenticeDashboard = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getDashboard());
  }, []);

  const dashboard = useAppSelector(state => state.dashboard.dashboard);
  const failuresByGrade = dashboard.failuresByGrade ?? [];
  const upcomingClasses = dashboard.upcomingClasses ?? [];
  const grades = dashboard.grades ?? [];
  const justifications = dashboard.justifications;
  const withinCorrectionWindow = justifications?.withinCorrectionWindow ?? [];

  return (
    <div>
      <div className="card-container d-flex justify-content-around mb-4 flex-wrap">
        <Card className="statistics-card">
          <p>Asistencia del trimestre</p>
          <p className="card-data">{dashboard.attendance ? `${dashboard.attendance.percentage}%` : '—'}</p>
          {dashboard.attendance && (
            <p className="text-muted mb-0">
              A: {dashboard.attendance.present} · F: {dashboard.attendance.failure} · J: {dashboard.attendance.justified}
            </p>
          )}
        </Card>
        <Card className="statistics-card">
          <p>Justificaciones pendientes</p>
          <p className="card-data">{justifications?.pending ?? 0}</p>
          <Link to="/justification">Ver mis justificaciones</Link>
        </Card>
        <Card className="statistics-card">
          <p>Alertas activas</p>
          <p className="card-data">{dashboard.activeAlerts ?? 0}</p>
        </Card>
      </div>

      {dashboard.trimesterMessage && <Alert variant="info">{dashboard.trimesterMessage}</Alert>}

      {failuresByGrade.length > 0 && (
        <>
          <h3>Fallas no justificadas por ficha</h3>
          <Table responsive size="sm">
            <thead>
              <tr>
                <th>Ficha</th>
                <th>Fallas</th>
                <th>Umbral</th>
                <th>Te faltan</th>
              </tr>
            </thead>
            <tbody>
              {failuresByGrade.map(item => (
                <tr key={item.gradeId}>
                  <td>{item.gradeCode}</td>
                  <td>{item.unexcusedFailures}</td>
                  <td>{item.threshold}</td>
                  <td>{item.missingToThreshold}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </>
      )}

      {withinCorrectionWindow.length > 0 && (
        <>
          <h3>Justificaciones por subsanar</h3>
          <p className="text-muted">Estas partes fueron rechazadas y todavía puedes corregirlas desde tus justificaciones.</p>
          <Table responsive size="sm">
            <thead>
              <tr>
                <th>Materia</th>
                <th>Plazo</th>
                <th>Días hábiles restantes</th>
              </tr>
            </thead>
            <tbody>
              {withinCorrectionWindow.map((item, index) => (
                <tr key={`${item.id}-${index}`}>
                  <td>{item.subjectName}</td>
                  <td>{item.deadline}</td>
                  <td>
                    <Badge bg={item.remainingBusinessDays === 0 ? 'danger' : 'warning'}>{item.remainingBusinessDays}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          <Link to="/justification">Ir a mis justificaciones</Link>
        </>
      )}

      <h3>Mis fichas y materias</h3>
      {grades.length === 0 ? (
        <p className="text-muted">No estás matriculado en ninguna ficha.</p>
      ) : (
        grades.map(grade => (
          <Card className="mb-3" key={grade.gradeId}>
            <Card.Body>
              <Card.Title>Ficha {grade.gradeCode}</Card.Title>
              <Card.Subtitle className="text-muted mb-2">{grade.programName}</Card.Subtitle>
              <p className="mb-0">{(grade.subjects ?? []).map(subject => subject.subjectName).join(', ')}</p>
            </Card.Body>
          </Card>
        ))
      )}

      {!dashboard.trimesterMessage && (
        <>
          <h3>Próximas clases</h3>
          {upcomingClasses.length === 0 ? (
            <p className="text-muted">No tienes próximas clases programadas.</p>
          ) : (
            <Table responsive size="sm">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Materia</th>
                  <th>Ficha</th>
                  <th>Horario</th>
                </tr>
              </thead>
              <tbody>
                {upcomingClasses.map((session, index) => (
                  <tr key={`${session.classSectionId}-${index}`}>
                    <td>{session.date}</td>
                    <td>{session.subjectName}</td>
                    <td>{session.gradeCode}</td>
                    <td>
                      {session.startTime} - {session.endTime}
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </>
      )}
    </div>
  );
};

export default AprenticeDashboard;

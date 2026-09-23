import React, { useEffect } from 'react';
import { Alert, Card, Table } from 'react-bootstrap';
import { Link } from 'react-router';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getDashboard } from 'app/modules/dashboard/dashboard.reducer';

// UC023, panel de Instructor: pendingJustifications, activeAlerts, assignedSubjects/Grades/
// Apprentices se calculan siempre (independientes de trimestre); todayClasses/upcomingClasses
// dependen del trimestre activo y viajan vacíos con trimesterMessage cuando no hay uno (E2).
export const InstructorDashboard = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getDashboard());
  }, []);

  const dashboard = useAppSelector(state => state.dashboard.dashboard);
  const todayClasses = dashboard.todayClasses ?? [];
  const upcomingClasses = dashboard.upcomingClasses ?? [];

  return (
    <div>
      <div className="card-container d-flex justify-content-around mb-4 flex-wrap">
        <Card className="statistics-card">
          <p>Justificaciones pendientes</p>
          <p className="card-data">{dashboard.pendingJustifications ?? 0}</p>
          <Link to="/justification-details">Ir a la bandeja</Link>
        </Card>
        <Card className="statistics-card">
          <p>Alertas activas</p>
          <p className="card-data">{dashboard.activeAlerts ?? 0}</p>
          <Link to="/alert">Ver alertas</Link>
        </Card>
        <Card className="statistics-card">
          <p>Materias asignadas</p>
          <p className="card-data">{dashboard.assignedSubjects ?? 0}</p>
        </Card>
        <Card className="statistics-card">
          <p>Fichas a mi cargo</p>
          <p className="card-data">{dashboard.assignedGrades ?? 0}</p>
          <Link to="/class-section/mine">Ver mis fichas</Link>
        </Card>
        <Card className="statistics-card">
          <p>Aprendices a mi cargo</p>
          <p className="card-data">{dashboard.assignedApprentices ?? 0}</p>
        </Card>
      </div>

      {dashboard.trimesterMessage ? (
        <Alert variant="info">{dashboard.trimesterMessage}</Alert>
      ) : (
        <>
          <h3>Clases de hoy</h3>
          {todayClasses.length === 0 ? (
            <p className="text-muted">No tienes clases programadas hoy.</p>
          ) : (
            <Table responsive size="sm">
              <thead>
                <tr>
                  <th>Materia</th>
                  <th>Ficha</th>
                  <th>Horario</th>
                </tr>
              </thead>
              <tbody>
                {todayClasses.map((session, index) => (
                  <tr key={`${session.classSectionId}-${index}`}>
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

export default InstructorDashboard;

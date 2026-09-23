import React, { useEffect, useMemo, useState } from 'react';
import { Alert, Badge, Button, Card, Col, Row, Table } from 'react-bootstrap';
import { Link } from 'react-router';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getMine } from 'app/entities/class-section/class-section.reducer';
import { getEntities as getApprentices } from 'app/entities/apprentice/apprentice.reducer';

import { clearSession, getEntities as getAttendances, saveSession } from './attendance.reducer';

const todayIso = () => new Date().toISOString().slice(0, 10);

// UC009: registrar asistencia es por materia + fecha de sesión. El instructor solo marca
// PRESENTE/FALLA (JUSTIFICADA llega por UC010, así que esos registros quedan de solo lectura).
export const AttendanceSession = () => {
  const dispatch = useAppDispatch();

  const [classSectionId, setClassSectionId] = useState('');
  const [date, setDate] = useState(todayIso());
  const [marks, setMarks] = useState<Record<string, 'PRESENTE' | 'FALLA'>>({});

  const myClassSections = useAppSelector(state => state.classSection.mine);
  const apprentices = useAppSelector(state => state.apprentice.entities);
  const existingRecords = useAppSelector(state => state.attendance.entities);
  const session = useAppSelector(state => state.attendance.session);
  const updating = useAppSelector(state => state.attendance.updating);

  const selectedClassSection = myClassSections.find(cs => cs.id === classSectionId);

  useEffect(() => {
    dispatch(getMine());
    return () => {
      dispatch(clearSession());
    };
  }, []);

  useEffect(() => {
    if (classSectionId && selectedClassSection?.grade?.id) {
      dispatch(getApprentices({ gradeId: selectedClassSection.grade.id, stateAcademic: 'MATRICULADO', size: 200 }));
    }
  }, [classSectionId, selectedClassSection?.grade?.id]);

  useEffect(() => {
    if (classSectionId && date) {
      dispatch(clearSession());
      dispatch(getAttendances({ classSectionId, date, size: 200 }));
    }
  }, [classSectionId, date]);

  // El roster nace de los matriculados de la ficha; se cruza con lo ya guardado para esa
  // materia+fecha (si lo hay) para precargar marcas y bloquear lo que ya llegó por UC010 (J).
  const roster = useMemo(
    () =>
      apprentices
        .filter(apprentice => apprentice.student?.id)
        .map(apprentice => {
          const existing = existingRecords.find(record => record.student?.id === apprentice.student!.id);
          return { student: apprentice.student!, existing };
        }),
    [apprentices, existingRecords],
  );

  useEffect(() => {
    const nextMarks: Record<string, 'PRESENTE' | 'FALLA'> = {};
    roster.forEach(({ student, existing }) => {
      if (existing?.stateAttendance === 'FALLA') {
        nextMarks[student.id!] = 'FALLA';
      } else if (existing?.stateAttendance === 'PRESENTE' || !existing) {
        nextMarks[student.id!] = 'PRESENTE';
      }
    });
    setMarks(nextMarks);
  }, [roster]);

  const toggleMark = (studentId: string) => {
    setMarks(prev => ({ ...prev, [studentId]: prev[studentId] === 'FALLA' ? 'PRESENTE' : 'FALLA' }));
  };

  const handleSave = async () => {
    const attendances = roster
      .filter(({ existing }) => existing?.stateAttendance !== 'JUSTIFICADA')
      .map(({ student }) => ({ studentId: student.id!, stateAttendance: marks[student.id!] ?? 'PRESENTE' }));

    const resultAction = await dispatch(
      saveSession({
        classSection: { id: classSectionId },
        date,
        attendances,
      }),
    );
    if (saveSession.fulfilled.match(resultAction)) {
      toast.success('Asistencia guardada');
      dispatch(getAttendances({ classSectionId, date, size: 200 }));
    }
  };

  return (
    <div>
      <h2>Tomar asistencia</h2>
      <p>Selecciona una de tus materias y la fecha de la sesión para registrar quién asistió.</p>
      <Row className="mb-3">
        <Col md="6">
          <label htmlFor="attendance-class-section">Materia</label>
          <select
            id="attendance-class-section"
            className="form-select"
            value={classSectionId}
            onChange={e => setClassSectionId(e.target.value)}
          >
            <option value="">Selecciona una materia</option>
            {myClassSections.map(cs => (
              <option value={cs.id} key={cs.id}>
                {cs.subjectName} — Ficha {cs.grade?.code}
              </option>
            ))}
          </select>
        </Col>
        <Col md="4">
          <label htmlFor="attendance-date">Fecha</label>
          <input
            id="attendance-date"
            type="date"
            className="form-control"
            value={date}
            max={todayIso()}
            onChange={e => setDate(e.target.value)}
          />
        </Col>
        <Col md="2" className="d-flex align-items-end">
          {classSectionId && (
            <Button as={Link as any} to={`/class-exception/new?classSectionId=${classSectionId}`} variant="outline-secondary" size="sm">
              <FontAwesomeIcon icon="calendar-times" /> Fecha no lectiva
            </Button>
          )}
        </Col>
      </Row>

      {classSectionId && date && (
        <Card>
          <Card.Body>
            {session && (
              <Alert variant={session.complete ? 'success' : 'warning'}>
                {session.complete
                  ? 'Sesión completa: todos los aprendices quedaron con un estado explícito.'
                  : `Sesión incompleta: se guardaron ${session.recordedCount} de ${session.enrolledCount} aprendices.`}
              </Alert>
            )}
            {roster.length === 0 ? (
              <p className="text-muted">No hay aprendices matriculados en esta ficha.</p>
            ) : (
              <Table responsive>
                <thead>
                  <tr>
                    <th>Aprendiz</th>
                    <th>Documento</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {roster.map(({ student, existing }) => {
                    const isJustified = existing?.stateAttendance === 'JUSTIFICADA';
                    const mark = marks[student.id!] ?? 'PRESENTE';
                    return (
                      <tr key={student.id}>
                        <td>
                          {student.firstName} {student.firstLastName}
                        </td>
                        <td>{student.documentNumber}</td>
                        <td>
                          {isJustified ? (
                            <Badge bg="info">Justificada</Badge>
                          ) : (
                            <Button size="sm" variant={mark === 'FALLA' ? 'danger' : 'success'} onClick={() => toggleMark(student.id!)}>
                              {mark === 'FALLA' ? 'Falla' : 'Asistió'}
                            </Button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            )}
            <Button variant="primary" onClick={handleSave} disabled={updating || roster.length === 0}>
              <FontAwesomeIcon icon="save" /> Guardar asistencia
            </Button>
          </Card.Body>
        </Card>
      )}
    </div>
  );
};

export default AttendanceSession;

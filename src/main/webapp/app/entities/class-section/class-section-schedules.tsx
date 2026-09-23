import React, { useEffect, useState } from 'react';
import { Badge, Button, Table } from 'react-bootstrap';
import { translate } from 'react-jhipster';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getTrimesters } from 'app/entities/trimester/trimester.reducer';
import { DayOfWeek } from 'app/shared/model/enumerations/day-of-week.model';
import { IClassSchedule } from 'app/shared/model/class-schedule.model';
import { ITimeSlot } from 'app/shared/model/time-slot.model';

import { createEntity, deleteEntity, getEntities, updateEntity } from '../class-schedule/class-schedule.reducer';

const dayOfWeekValues = Object.keys(DayOfWeek);

interface IScheduleFormValues {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  trimester: string;
}

const emptyForm: IScheduleFormValues = { dayOfWeek: '', startTime: '', endTime: '', trimester: '' };

interface ClassSectionSchedulesProps {
  classSectionId: string;
  timeSlot?: ITimeSlot;
}

// UC015, flujo básico paso 2 y A2: los horarios de la materia se gestionan desde esta misma
// vista (edición de la competencia), por trimestre, en vez de una pantalla de admin genérica y
// desconectada. El backend valida jornada/solape/trimestre cerrado; aquí solo se arma el payload.
export const ClassSectionSchedules = ({ classSectionId, timeSlot }: ClassSectionSchedulesProps) => {
  const dispatch = useAppDispatch();

  const [form, setForm] = useState<IScheduleFormValues>(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);

  const allSchedules = useAppSelector(state => state.classSchedule.entities);
  const trimesters = useAppSelector(state => state.trimester.entities);
  const updating = useAppSelector(state => state.classSchedule.updating);

  useEffect(() => {
    dispatch(getEntities({ page: 0, size: 1000, sort: 'dayOfWeek,asc' }));
    dispatch(getTrimesters({ page: 0, size: 200, sort: 'startDate,desc' }));
  }, []);

  const schedules = allSchedules.filter(schedule => schedule.classSection?.id === classSectionId);
  // No se ofrecen trimestres CERRADOS para crear/editar horarios (E6); un horario ya guardado en
  // uno cerrado se sigue listando, solo no se puede tocar.
  const openTrimesters = trimesters.filter(trimester => trimester.status !== 'CERRADO');

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const startEdit = (schedule: IClassSchedule) => {
    setEditingId(schedule.id!);
    setForm({
      dayOfWeek: schedule.dayOfWeek ?? '',
      startTime: schedule.startTime ?? '',
      endTime: schedule.endTime ?? '',
      trimester: schedule.trimester?.id ?? '',
    });
  };

  const handleDelete = (schedule: IClassSchedule) => {
    if (globalThis.confirm('¿Eliminar este horario?')) {
      dispatch(deleteEntity(schedule.id!));
    }
  };

  const handleSubmit = () => {
    if (!form.dayOfWeek || !form.startTime || !form.endTime || !form.trimester) {
      return;
    }
    const trimester = trimesters.find(it => it.id === form.trimester);
    const entity = {
      id: editingId ?? undefined,
      dayOfWeek: form.dayOfWeek as keyof typeof DayOfWeek,
      startTime: form.startTime,
      endTime: form.endTime,
      trimester,
      classSection: { id: classSectionId },
    };
    const resultAction = editingId ? dispatch(updateEntity(entity)) : dispatch(createEntity(entity));
    resultAction.then(action => {
      if (createEntity.fulfilled.match(action) || updateEntity.fulfilled.match(action)) {
        resetForm();
      }
    });
  };

  return (
    <div className="mt-4">
      <h4>Horarios</h4>
      {timeSlot && (
        <p className="text-muted">
          Jornada de la ficha ({timeSlot.name}): {timeSlot.startTime} – {timeSlot.endTime}. Cada horario debe caer dentro de este rango.
        </p>
      )}
      {schedules.length > 0 && (
        <Table responsive size="sm">
          <thead>
            <tr>
              <th>Día</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Trimestre</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {schedules.map(schedule => (
              <tr key={schedule.id}>
                <td>{translate(`senaAttendanceApp.DayOfWeek.${schedule.dayOfWeek}`)}</td>
                <td>{schedule.startTime}</td>
                <td>{schedule.endTime}</td>
                <td>
                  {schedule.trimester?.name}
                  {schedule.trimester?.status === 'CERRADO' && (
                    <Badge bg="secondary" className="ms-1">
                      Cerrado
                    </Badge>
                  )}
                </td>
                <td className="text-end">
                  <Button
                    variant="primary"
                    size="sm"
                    className="me-1"
                    onClick={() => startEdit(schedule)}
                    disabled={schedule.trimester?.status === 'CERRADO'}
                  >
                    <FontAwesomeIcon icon="pencil-alt" />
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => handleDelete(schedule)}
                    disabled={schedule.trimester?.status === 'CERRADO'}
                  >
                    <FontAwesomeIcon icon="trash" />
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
      {schedules.length === 0 && <p className="text-muted">Esta competencia aún no tiene horarios registrados.</p>}

      <h5>{editingId ? 'Editar horario' : 'Agregar horario'}</h5>
      <div className="d-flex gap-2 align-items-end flex-wrap mb-2">
        <div>
          <label htmlFor="schedule-day">Día</label>
          <select
            id="schedule-day"
            className="form-select"
            value={form.dayOfWeek}
            onChange={e => setForm({ ...form, dayOfWeek: e.target.value })}
          >
            <option value="">Selecciona un día</option>
            {dayOfWeekValues.map(day => (
              <option value={day} key={day}>
                {translate(`senaAttendanceApp.DayOfWeek.${day}`)}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="schedule-start">Hora inicio</label>
          <input
            id="schedule-start"
            type="time"
            className="form-control"
            value={form.startTime}
            onChange={e => setForm({ ...form, startTime: e.target.value })}
          />
        </div>
        <div>
          <label htmlFor="schedule-end">Hora fin</label>
          <input
            id="schedule-end"
            type="time"
            className="form-control"
            value={form.endTime}
            onChange={e => setForm({ ...form, endTime: e.target.value })}
          />
        </div>
        <div>
          <label htmlFor="schedule-trimester">Trimestre</label>
          <select
            id="schedule-trimester"
            className="form-select"
            value={form.trimester}
            onChange={e => setForm({ ...form, trimester: e.target.value })}
          >
            <option value="">Selecciona un trimestre</option>
            {openTrimesters.map(trimester => (
              <option value={trimester.id} key={trimester.id}>
                {trimester.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <Button variant="success" onClick={handleSubmit} disabled={updating}>
            <FontAwesomeIcon icon="save" /> {editingId ? 'Guardar' : 'Agregar'}
          </Button>
          {editingId && (
            <Button variant="secondary" className="ms-2" onClick={resetForm}>
              Cancelar
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ClassSectionSchedules;

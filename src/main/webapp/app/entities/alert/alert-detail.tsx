import React, { useEffect, useState } from 'react';
import { Badge, Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { attend, getEntity, markAsRead } from './alert.reducer';

const stateVariant: Record<string, string> = {
  NO_LEIDA: 'warning',
  LEIDA: 'info',
  ATENDIDA: 'success',
  RESUELTA_AUTOMATICAMENTE: 'secondary',
};

const stateLabel: Record<string, string> = {
  NO_LEIDA: 'No leída',
  LEIDA: 'Leída',
  ATENDIDA: 'Atendida',
  RESUELTA_AUTOMATICAMENTE: 'Resuelta automáticamente',
};

// UC013, A2/A3: abrir el detalle marca la alerta como leída (idempotente); atenderla exige la
// observación de seguimiento y no aplica sobre una alerta resuelta automáticamente.
export const AlertDetail = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [observation, setObservation] = useState('');

  useEffect(() => {
    dispatch(getEntity(id!));
  }, [id]);

  const alerta = useAppSelector(state => state.alert.entity);
  const updating = useAppSelector(state => state.alert.updating);

  useEffect(() => {
    if (alerta.id === id && alerta.state === 'NO_LEIDA') {
      dispatch(markAsRead(id!));
    }
  }, [alerta.id, alerta.state]);

  const canAttend = alerta.state === 'LEIDA' || alerta.state === 'NO_LEIDA';

  const handleAttend = async () => {
    if (!observation.trim()) {
      toast.error('Registra la observación del seguimiento');
      return;
    }
    const resultAction = await dispatch(attend({ id: id!, observation: observation.trim() }));
    if (attend.fulfilled.match(resultAction)) {
      toast.success('Alerta atendida');
    }
  };

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="alertDetailsHeading">Alerta de inasistencia</h2>
        <dl className="jh-entity-details">
          <dt>Estado</dt>
          <dd>
            <Badge bg={stateVariant[alerta.state ?? ''] ?? 'secondary'}>{stateLabel[alerta.state ?? ''] ?? alerta.state}</Badge>
          </dd>
          <dt>Tipo</dt>
          <dd>{alerta.type === 'CONSECUTIVAS' ? 'Fallas consecutivas' : 'Fallas acumuladas'}</dd>
          <dt>Aprendiz</dt>
          <dd>
            {alerta.student?.firstName} {alerta.student?.firstLastName} ({alerta.student?.documentNumber})
          </dd>
          {alerta.type === 'CONSECUTIVAS' ? (
            <>
              <dt>Materia</dt>
              <dd>{alerta.classSection?.subjectName}</dd>
            </>
          ) : null}
          <dt>Ficha</dt>
          <dd>{alerta.grade?.code}</dd>
          <dt>Trimestre</dt>
          <dd>{alerta.trimester?.name}</dd>
          <dt>Conteo / Umbral</dt>
          <dd>
            {alerta.absenceCount} / {alerta.threshold}
          </dd>
          <dt>Generada</dt>
          <dd>{alerta.generatedAt ? <TextFormat value={alerta.generatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          {alerta.resolvedAt ? (
            <>
              <dt>Resuelta</dt>
              <dd>
                <TextFormat value={alerta.resolvedAt} type="date" format={APP_DATE_FORMAT} />
              </dd>
            </>
          ) : null}
          {alerta.observation ? (
            <>
              <dt>Observación de seguimiento</dt>
              <dd>{alerta.observation}</dd>
            </>
          ) : null}
        </dl>

        {alerta.state === 'RESUELTA_AUTOMATICAMENTE' && (
          <p className="text-muted">
            Esta alerta se resolvió automáticamente: una justificación aprobada bajó el conteo por debajo del umbral.
          </p>
        )}

        {canAttend && (
          <>
            <h3>Atender</h3>
            <Row className="mb-3">
              <Col md="8">
                <label htmlFor="observation">Observación del seguimiento</label>
                <textarea
                  id="observation"
                  className="form-control"
                  maxLength={300}
                  value={observation}
                  onChange={e => setObservation(e.target.value)}
                />
              </Col>
            </Row>
            <Button variant="success" onClick={handleAttend} disabled={updating}>
              <FontAwesomeIcon icon="check" /> Marcar como atendida
            </Button>
            &nbsp;
          </>
        )}

        <Button variant="info" onClick={() => navigate('/alert')} data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        {alerta.student?.id && (
          <>
            &nbsp;
            <Button as={Link as any} to={`/alert/students/${alerta.student.id}`} variant="outline-secondary">
              Ver historial de este aprendiz
            </Button>
          </>
        )}
      </Col>
    </Row>
  );
};

export default AlertDetail;

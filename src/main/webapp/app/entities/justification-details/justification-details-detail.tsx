import React, { useEffect, useState } from 'react';
import { Alert, Badge, Button, Col, Row } from 'react-bootstrap';
import { TextFormat, byteSize, openFile } from 'react-jhipster';
import { useNavigate, useParams } from 'react-router';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { decideEntity, getEntity } from './justification-details.reducer';

const stateVariant: Record<string, string> = {
  PENDIENTE: 'warning',
  ACEPTADA: 'success',
  RECHAZADA: 'danger',
  CANCELADA: 'secondary',
};

// UC010, flujo básico paso 5: el instructor aprueba o rechaza su materia. Un rechazo exige
// motivo (E1); aprobar una justificación fuera de tiempo exige el motivo adicional (A2).
export const JustificationDetailsDetail = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [rejectionReason, setRejectionReason] = useState('');
  const [outOfTimeReason, setOutOfTimeReason] = useState('');

  useEffect(() => {
    dispatch(getEntity(id!));
  }, [id]);

  const detail = useAppSelector(state => state.justificationDetails.entity);
  const updating = useAppSelector(state => state.justificationDetails.updating);
  const justification = detail.justification;
  const isPending = detail.stateJustification === 'PENDIENTE';
  const requiresOutOfTimeReason = justification?.onTime === false;

  const handleApprove = async () => {
    if (requiresOutOfTimeReason && !outOfTimeReason.trim()) {
      toast.error('Esta justificación quedó fuera de tiempo: registra el motivo adicional para aprobarla');
      return;
    }
    const resultAction = await dispatch(
      decideEntity({
        id: id!,
        stateJustification: 'ACEPTADA',
        outOfTimeReason: requiresOutOfTimeReason ? outOfTimeReason.trim() : undefined,
      }),
    );
    if (decideEntity.fulfilled.match(resultAction)) {
      toast.success('Justificación aprobada');
    }
  };

  const handleReject = async () => {
    if (!rejectionReason.trim()) {
      toast.error('Registra el motivo del rechazo');
      return;
    }
    const resultAction = await dispatch(
      decideEntity({ id: id!, stateJustification: 'RECHAZADA', rejectionReason: rejectionReason.trim() }),
    );
    if (decideEntity.fulfilled.match(resultAction)) {
      toast.success('Justificación rechazada');
    }
  };

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="justificationDetailsDetailsHeading">Parte de justificación</h2>
        <dl className="jh-entity-details">
          <dt>Estado</dt>
          <dd>
            <Badge bg={stateVariant[detail.stateJustification ?? ''] ?? 'secondary'}>{detail.stateJustification}</Badge>
            {detail.lateDecision && (
              <Badge bg="dark" className="ms-1">
                Decisión demorada
              </Badge>
            )}
          </dd>
          <dt>Aprendiz</dt>
          <dd>{justification?.student?.documentNumber}</dd>
          <dt>Materia</dt>
          <dd>{detail.classSection?.subjectName}</dd>
          <dt>Período</dt>
          <dd>
            {justification?.startDate ? <TextFormat value={justification.startDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
            {' – '}
            {justification?.endDate ? <TextFormat value={justification.endDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>Marca de plazo</dt>
          <dd>
            {justification?.onTime === undefined ? null : (
              <Badge bg={justification.onTime ? 'success' : 'danger'}>{justification.onTime ? 'En tiempo' : 'Fuera de tiempo'}</Badge>
            )}
          </dd>
          <dt>Fecha de solicitud</dt>
          <dd>{detail.requestDate ? <TextFormat value={detail.requestDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>Tipo de justificación</dt>
          <dd>{justification?.justificationType?.name}</dd>
          <dt>Descripción</dt>
          <dd>{justification?.description}</dd>
          <dt>Soporte</dt>
          <dd>
            {justification?.evidence ? (
              <div>
                <a onClick={openFile(justification.evidenceContentType!, justification.evidence)}>Ver soporte</a> (
                {justification.evidenceContentType}, {byteSize(justification.evidence)})
              </div>
            ) : (
              <span className="text-muted">El archivo adjunto no está disponible</span>
            )}
          </dd>
        </dl>

        {isPending ? (
          <>
            <h3>Decisión</h3>
            {requiresOutOfTimeReason && (
              <Alert variant="warning">
                Esta justificación se envió fuera de tiempo. Para aprobarla debes registrar el motivo adicional de la excepción.
              </Alert>
            )}
            <Row className="mb-3">
              <Col md="8">
                <label htmlFor="out-of-time-reason">Motivo adicional (aprobación fuera de tiempo)</label>
                <textarea
                  id="out-of-time-reason"
                  className="form-control"
                  maxLength={300}
                  value={outOfTimeReason}
                  onChange={e => setOutOfTimeReason(e.target.value)}
                  disabled={!requiresOutOfTimeReason}
                />
              </Col>
            </Row>
            <Row className="mb-3">
              <Col md="8">
                <label htmlFor="rejection-reason">Motivo de rechazo</label>
                <textarea
                  id="rejection-reason"
                  className="form-control"
                  maxLength={300}
                  value={rejectionReason}
                  onChange={e => setRejectionReason(e.target.value)}
                />
              </Col>
            </Row>
            <Button variant="success" onClick={handleApprove} disabled={updating}>
              <FontAwesomeIcon icon="check" /> Aprobar
            </Button>
            &nbsp;
            <Button variant="danger" onClick={handleReject} disabled={updating}>
              <FontAwesomeIcon icon="ban" /> Rechazar
            </Button>
          </>
        ) : (
          <>
            <h3>Decisión</h3>
            <dl className="jh-entity-details">
              <dt>Fecha de respuesta</dt>
              <dd>{detail.responseDate ? <TextFormat value={detail.responseDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
              {detail.stateJustification === 'RECHAZADA' && (
                <>
                  <dt>Motivo de rechazo</dt>
                  <dd>{detail.rejectionReason}</dd>
                </>
              )}
              {detail.outOfTimeReason && (
                <>
                  <dt>Motivo adicional (fuera de tiempo)</dt>
                  <dd>{detail.outOfTimeReason}</dd>
                </>
              )}
            </dl>
          </>
        )}

        <Button variant="info" onClick={() => navigate('/justification-details')} data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
      </Col>
    </Row>
  );
};

export default JustificationDetailsDetail;

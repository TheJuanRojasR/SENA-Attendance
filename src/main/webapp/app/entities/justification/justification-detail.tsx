import React, { useEffect, useState } from 'react';
import { Alert, Badge, Button, Col, Modal, ModalBody, ModalFooter, ModalHeader, Row, Table } from 'react-bootstrap';
import { TextFormat, byteSize, openFile } from 'react-jhipster';
import { Link, useParams } from 'react-router';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { partialUpdateEntity as correctDetails } from 'app/entities/justification-details/justification-details.reducer';

import { cancelJustification, getEntity } from './justification.reducer';

const stateVariant: Record<string, string> = {
  PENDIENTE: 'warning',
  ACEPTADA: 'success',
  RECHAZADA: 'danger',
  CANCELADA: 'secondary',
};

const fileToBase64 = (file: File): Promise<{ data: string; contentType: string }> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      resolve({ data: result.substring(result.indexOf(',') + 1), contentType: file.type });
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });

// Réplica en el cliente de BusinessDays.plus del backend: días hábiles lunes a viernes, sin
// calendario de festivos. Solo se usa para mostrar el plazo de subsanación (A5), nunca para
// decidir si el servidor la acepta.
const addBusinessDays = (from: Date, days: number): Date => {
  const result = new Date(from);
  let remaining = days;
  while (remaining > 0) {
    result.setDate(result.getDate() + 1);
    const day = result.getDay();
    if (day !== 0 && day !== 6) {
      remaining -= 1;
    }
  }
  return result;
};

const CorrectionForm = ({ detailId, onDone }: { detailId: string; onDone: () => void }) => {
  const dispatch = useAppDispatch();
  const updating = useAppSelector(state => state.justificationDetails.updating);
  const [correctionText, setCorrectionText] = useState('');
  const [correctionFile, setCorrectionFile] = useState<{ data: string; contentType: string } | undefined>(undefined);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('El archivo de corrección no puede superar 5 MB');
      e.target.value = '';
      return;
    }
    setCorrectionFile(await fileToBase64(file));
  };

  const handleSubmit = async () => {
    if (!correctionText.trim() && !correctionFile) {
      toast.error('Agrega un texto o un archivo de corrección');
      return;
    }
    const resultAction = await dispatch(
      correctDetails({
        id: detailId,
        correctionText: correctionText.trim(),
        ...(correctionFile ? { correctionFileUrl: correctionFile.data, correctionFileUrlContentType: correctionFile.contentType } : {}),
      }),
    );
    if (correctDetails.fulfilled.match(resultAction)) {
      toast.success('Corrección enviada, la parte vuelve a quedar pendiente');
      onDone();
    }
  };

  return (
    <div className="border rounded p-2 mt-2">
      <div className="mb-2">
        <label htmlFor={`correction-text-${detailId}`}>Corrección</label>
        <textarea
          id={`correction-text-${detailId}`}
          className="form-control"
          maxLength={300}
          value={correctionText}
          onChange={e => setCorrectionText(e.target.value)}
        />
      </div>
      <div className="mb-2">
        <label htmlFor={`correction-file-${detailId}`}>Archivo de corrección (opcional)</label>
        <input id={`correction-file-${detailId}`} type="file" className="form-control" accept=".pdf,image/*" onChange={handleFileChange} />
      </div>
      <Button size="sm" variant="primary" onClick={handleSubmit} disabled={updating}>
        <FontAwesomeIcon icon="save" /> Enviar corrección
      </Button>
    </div>
  );
};

export const JustificationDetail = () => {
  const dispatch = useAppDispatch();
  const { id } = useParams<'id'>();

  const [showCancel, setShowCancel] = useState(false);
  const [correctingId, setCorrectingId] = useState<string | null>(null);

  useEffect(() => {
    dispatch(getEntity(id!));
  }, [id]);

  const justificationEntity = useAppSelector(state => state.justification.entity);
  const updating = useAppSelector(state => state.justification.updating);

  const detailses = justificationEntity.detailses ?? [];
  const allPending = detailses.length > 0 && detailses.every(d => d.stateJustification === 'PENDIENTE');

  const confirmCancel = async () => {
    const resultAction = await dispatch(cancelJustification(justificationEntity.id!));
    if (cancelJustification.fulfilled.match(resultAction)) {
      toast.success('Justificación cancelada');
      setShowCancel(false);
    }
  };

  return (
    <Row>
      <Col md="9">
        <h2 data-cy="justificationDetailsHeading">Justificación</h2>
        <dl className="jh-entity-details">
          <dt>Período</dt>
          <dd>
            {justificationEntity.startDate ? (
              <TextFormat value={justificationEntity.startDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
            {' – '}
            {justificationEntity.endDate ? (
              <TextFormat value={justificationEntity.endDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Marca de plazo</dt>
          <dd>
            {justificationEntity.onTime === undefined ? null : (
              <Badge bg={justificationEntity.onTime ? 'success' : 'danger'}>
                {justificationEntity.onTime ? 'En tiempo' : 'Fuera de tiempo'}
              </Badge>
            )}
          </dd>
          <dt>Tipo de justificación</dt>
          <dd>{justificationEntity.justificationType?.name}</dd>
          <dt>Descripción</dt>
          <dd>{justificationEntity.description}</dd>
          <dt>Soporte</dt>
          <dd>
            {justificationEntity.evidence ? (
              <div>
                <a onClick={openFile(justificationEntity.evidenceContentType!, justificationEntity.evidence)}>Ver soporte</a> (
                {justificationEntity.evidenceContentType}, {byteSize(justificationEntity.evidence)})
              </div>
            ) : null}
          </dd>
        </dl>

        <h3>Partes por materia</h3>
        <Table responsive size="sm">
          <thead>
            <tr>
              <th>Materia</th>
              <th>Estado</th>
              <th>Fecha de respuesta</th>
              <th>Motivo de rechazo</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {detailses.map(detail => {
              const deadline = detail.responseDate ? addBusinessDays(new Date(`${detail.responseDate}T00:00:00`), 2) : null;
              const withinWindow = detail.stateJustification === 'RECHAZADA' && deadline !== null && new Date() <= deadline;
              return (
                <React.Fragment key={detail.id}>
                  <tr>
                    <td>{detail.classSection?.subjectName}</td>
                    <td>
                      <Badge bg={stateVariant[detail.stateJustification ?? ''] ?? 'secondary'}>{detail.stateJustification}</Badge>
                    </td>
                    <td>
                      {detail.responseDate ? <TextFormat value={detail.responseDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </td>
                    <td>{detail.rejectionReason}</td>
                    <td>
                      {withinWindow && (
                        <Button
                          size="sm"
                          variant="outline-primary"
                          onClick={() => setCorrectingId(detail.id === correctingId ? null : detail.id!)}
                        >
                          Subsanar
                        </Button>
                      )}
                      {detail.stateJustification === 'RECHAZADA' && !withinWindow && (
                        <span className="text-muted">Plazo de subsanación vencido</span>
                      )}
                    </td>
                  </tr>
                  {correctingId === detail.id && (
                    <tr>
                      <td colSpan={5}>
                        <Alert variant="info" className="mb-2">
                          Tienes hasta el{' '}
                          <TextFormat value={deadline!.toISOString().slice(0, 10)} type="date" format={APP_LOCAL_DATE_FORMAT} /> para
                          corregir esta parte.
                        </Alert>
                        <CorrectionForm
                          detailId={detail.id!}
                          onDone={() => {
                            setCorrectingId(null);
                            dispatch(getEntity(id!));
                          }}
                        />
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </Table>

        <Button as={Link as any} to="/justification" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        {allPending && (
          <>
            &nbsp;
            <Button as={Link as any} to={`/justification/${justificationEntity.id}/edit`} variant="primary">
              <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
            </Button>
            &nbsp;
            <Button variant="danger" onClick={() => setShowCancel(true)}>
              <FontAwesomeIcon icon="ban" /> <span className="d-none d-md-inline">Cancelar</span>
            </Button>
          </>
        )}

        <Modal show={showCancel} onHide={() => setShowCancel(false)}>
          <ModalHeader closeButton>Cancelar justificación</ModalHeader>
          <ModalBody>¿Seguro que quieres cancelar esta justificación? El cupo reservado se liberará.</ModalBody>
          <ModalFooter>
            <Button variant="secondary" onClick={() => setShowCancel(false)}>
              Volver
            </Button>
            <Button variant="danger" onClick={confirmCancel} disabled={updating}>
              <FontAwesomeIcon icon="ban" /> Confirmar cancelación
            </Button>
          </ModalFooter>
        </Modal>
      </Col>
    </Row>
  );
};

export default JustificationDetail;

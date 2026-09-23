import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Button, Col, Row, Table } from 'react-bootstrap';
import { TextFormat, byteSize, openFile } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getAttendances } from 'app/entities/attendance/attendance.reducer';
import { getActiveEntities as getActiveJustificationTypes } from 'app/entities/justification-type/justification-type.reducer';

import { createEntity, getEntity, reset, updateEntity } from './justification.reducer';

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

// UC011, flujo básico: el aprendiz selecciona las fallas (F) que quiere justificar; el servidor
// crea la cabecera y una parte PENDIENTE por cada materia afectada. En edición (A3) solo se
// permite mientras todas las partes sigan PENDIENTE.
export const JustificationUpdate = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;
  const prefilledRef = useRef(false);

  const [selectedFallaIds, setSelectedFallaIds] = useState<Set<string>>(new Set());
  const [description, setDescription] = useState('');
  const [justificationTypeId, setJustificationTypeId] = useState('');
  const [evidence, setEvidence] = useState<{ data: string; contentType: string } | undefined>(undefined);

  const fallas = useAppSelector(state => state.attendance.entities);
  const justificationTypes = useAppSelector(state => state.justificationType.entities);
  const justificationEntity = useAppSelector(state => state.justification.entity);
  const loading = useAppSelector(state => state.justification.loading);
  const updating = useAppSelector(state => state.justification.updating);
  const updateSuccess = useAppSelector(state => state.justification.updateSuccess);

  const handleClose = () => navigate(isNew ? '/justification' : `/justification/${id}`);

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
    dispatch(getAttendances({ stateAttendance: 'FALLA', size: 200, sort: 'date,desc' }));
    dispatch(getActiveJustificationTypes());
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      toast.success(isNew ? 'Justificación enviada' : 'Justificación actualizada');
      handleClose();
    }
  }, [updateSuccess]);

  // Precarga el formulario de edición: descripción, tipo, soporte y las fallas ya cubiertas por
  // el rango y las materias de la justificación existente.
  useEffect(() => {
    if (isNew || prefilledRef.current || !justificationEntity.id || justificationEntity.id !== id) {
      return;
    }
    setDescription(justificationEntity.description ?? '');
    setJustificationTypeId(justificationEntity.justificationType?.id ?? '');
    if (justificationEntity.evidence && justificationEntity.evidenceContentType) {
      setEvidence({ data: justificationEntity.evidence, contentType: justificationEntity.evidenceContentType });
    }
    const classSectionIds = new Set((justificationEntity.detailses ?? []).map(d => d.classSection?.id));
    const covered = fallas
      .filter(
        falla =>
          classSectionIds.has(falla.classSection?.id) &&
          falla.date! >= justificationEntity.startDate! &&
          falla.date! <= justificationEntity.endDate!,
      )
      .map(falla => falla.id!);
    if (covered.length > 0) {
      setSelectedFallaIds(new Set(covered));
      prefilledRef.current = true;
    }
  }, [justificationEntity, fallas]);

  const selectedFallas = useMemo(() => fallas.filter(falla => selectedFallaIds.has(falla.id!)), [fallas, selectedFallaIds]);

  const toggleFalla = (fallaId: string) => {
    setSelectedFallaIds(prev => {
      const next = new Set(prev);
      if (next.has(fallaId)) {
        next.delete(fallaId);
      } else {
        next.add(fallaId);
      }
      return next;
    });
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('El soporte no puede superar 5 MB');
      e.target.value = '';
      return;
    }
    setEvidence(await fileToBase64(file));
  };

  const handleSubmit = () => {
    if (selectedFallas.length === 0) {
      toast.error('Selecciona al menos una falla para justificar');
      return;
    }
    if (!justificationTypeId) {
      toast.error('Selecciona un tipo de justificación');
      return;
    }
    if (!description.trim()) {
      toast.error('La descripción es obligatoria');
      return;
    }
    if (!evidence) {
      toast.error('Adjunta un soporte (PDF o imagen, máximo 5 MB)');
      return;
    }

    const dates = selectedFallas.map(falla => falla.date!);
    const classSectionIds: string[] = Array.from(new Set(selectedFallas.map(falla => falla.classSection!.id!)));
    const studentId = selectedFallas[0].student!.id!;

    const entity = {
      id,
      description: description.trim(),
      startDate: dates.reduce((min, date) => (date < min ? date : min)),
      endDate: dates.reduce((max, date) => (date > max ? date : max)),
      evidence: evidence.data,
      evidenceContentType: evidence.contentType,
      justificationType: { id: justificationTypeId },
      student: { id: studentId },
      detailses: classSectionIds.map(csId => ({ classSection: { id: csId } })),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="10">
          <h2>{isNew ? 'Justificar fallas' : 'Editar justificación'}</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="10">
          {loading ? (
            <p>Cargando...</p>
          ) : (
            <>
              <p>
                Selecciona las fallas que quieres justificar. Por cada materia afectada se creará una parte independiente que decide su
                instructor.
              </p>
              {fallas.length === 0 ? (
                <Alert variant="info">No tienes fallas registradas para justificar.</Alert>
              ) : (
                <Table responsive size="sm">
                  <thead>
                    <tr>
                      <th />
                      <th>Fecha</th>
                      <th>Materia</th>
                    </tr>
                  </thead>
                  <tbody>
                    {fallas.map(falla => (
                      <tr key={falla.id}>
                        <td>
                          <input
                            type="checkbox"
                            checked={selectedFallaIds.has(falla.id!)}
                            onChange={() => toggleFalla(falla.id!)}
                            aria-label={`Seleccionar falla del ${falla.date}`}
                          />
                        </td>
                        <td>
                          <TextFormat type="date" value={falla.date} format={APP_LOCAL_DATE_FORMAT} />
                        </td>
                        <td>{falla.classSection?.subjectName}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
              <Row className="mb-3">
                <Col md="6">
                  <label htmlFor="justification-type">Tipo de justificación</label>
                  <select
                    id="justification-type"
                    className="form-select"
                    value={justificationTypeId}
                    onChange={e => setJustificationTypeId(e.target.value)}
                  >
                    <option value="">Selecciona un tipo</option>
                    {justificationTypes.map(type => (
                      <option value={type.id} key={type.id}>
                        {type.name}
                      </option>
                    ))}
                  </select>
                </Col>
              </Row>
              <Row className="mb-3">
                <Col md="10">
                  <label htmlFor="justification-description">Descripción</label>
                  <textarea
                    id="justification-description"
                    className="form-control"
                    maxLength={300}
                    value={description}
                    onChange={e => setDescription(e.target.value)}
                  />
                </Col>
              </Row>
              <Row className="mb-3">
                <Col md="6">
                  <label htmlFor="justification-evidence">Soporte (PDF o imagen, máximo 5 MB)</label>
                  <input
                    id="justification-evidence"
                    type="file"
                    className="form-control"
                    accept=".pdf,image/*"
                    onChange={handleFileChange}
                  />
                  {evidence && (
                    <div className="mt-1">
                      <a onClick={openFile(evidence.contentType, evidence.data)}>Ver soporte actual</a> ({evidence.contentType},{' '}
                      {byteSize(evidence.data)})
                    </div>
                  )}
                </Col>
              </Row>
              <Button as={Link as any} id="cancel-save" to={isNew ? '/justification' : `/justification/${id}`} replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Volver</span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" onClick={handleSubmit} disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;Enviar
              </Button>
            </>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default JustificationUpdate;

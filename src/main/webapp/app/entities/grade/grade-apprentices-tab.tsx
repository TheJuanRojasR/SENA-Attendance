import React, { useEffect, useState } from 'react';
import { Badge, Button, Table } from 'react-bootstrap';
import { translate } from 'react-jhipster';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { IApprentice } from 'app/shared/model/apprentice.model';

import { enrollApprentice, getEntities, unlinkApprentice } from '../apprentice/apprentice.reducer';

interface GradeApprenticesTabProps {
  gradeId: string;
  canManage: boolean;
}

const UNLINK_REASONS = ['RETIRO_VOLUNTARIO', 'APLAZADO', 'CANCELADO'] as const;

export const GradeApprenticesTab = ({ gradeId, canManage }: GradeApprenticesTabProps) => {
  const dispatch = useAppDispatch();

  const [documentNumber, setDocumentNumber] = useState('');
  const [unlinkTarget, setUnlinkTarget] = useState<IApprentice | null>(null);
  const [unlinkReason, setUnlinkReason] = useState<(typeof UNLINK_REASONS)[number]>('RETIRO_VOLUNTARIO');

  const apprentices = useAppSelector(state => state.apprentice.entities);
  const loadingList = useAppSelector(state => state.apprentice.loading);
  const updating = useAppSelector(state => state.apprentice.updating);

  const loadApprentices = () => dispatch(getEntities({ page: 0, size: 1000, sort: 'id,asc', gradeId }));

  useEffect(() => {
    loadApprentices();
  }, [gradeId]);

  const gradeApprentices = (apprentices ?? []).filter(apprentice => apprentice.grade?.id === gradeId);

  const handleEnroll = async event => {
    event.preventDefault();
    const resultAction = await dispatch(enrollApprentice({ documentNumber, gradeId }));
    if (enrollApprentice.fulfilled.match(resultAction)) {
      toast.success('Aprendiz vinculado a la ficha.');
      setDocumentNumber('');
      loadApprentices();
    }
  };

  const confirmUnlink = async () => {
    if (!unlinkTarget) {
      return;
    }
    const resultAction = await dispatch(unlinkApprentice({ id: unlinkTarget.id!, reason: unlinkReason }));
    if (unlinkApprentice.fulfilled.match(resultAction)) {
      const historyKept = resultAction.payload.status === 200;
      toast.success(
        historyKept
          ? translate('senaAttendanceApp.apprentice.unlink.successKept')
          : translate('senaAttendanceApp.apprentice.unlink.success'),
      );
      setUnlinkTarget(null);
      setUnlinkReason('RETIRO_VOLUNTARIO');
      loadApprentices();
    }
  };

  return (
    <div>
      {canManage && (
        <form className="d-flex justify-content-end align-items-start gap-2 mb-3" onSubmit={handleEnroll}>
          <input
            type="text"
            className="form-control"
            style={{ maxWidth: '260px' }}
            placeholder="Número de documento del aprendiz"
            value={documentNumber}
            onChange={e => setDocumentNumber(e.target.value)}
            required
          />
          <Button variant="success" type="submit" disabled={updating || !documentNumber}>
            <FontAwesomeIcon icon="plus" />
            &nbsp; Vincular Aprendiz
          </Button>
        </form>
      )}

      {!canManage && (
        <div className="alert alert-warning">
          Esta ficha no admite vincular ni desvincular aprendices en su estado actual; solo puede consultar los ya matriculados.
        </div>
      )}

      <div className="table-responsive">
        {gradeApprentices.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>Aprendiz</th>
                <th>Documento</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {gradeApprentices.map(apprentice => (
                <tr key={`apprentice-${apprentice.id}`} data-cy="entityTable">
                  <td>{apprentice.student ? `${apprentice.student.firstName} ${apprentice.student.firstLastName}` : 'Sin datos'}</td>
                  <td>{apprentice.student?.documentNumber}</td>
                  <td>
                    <Badge bg={apprentice.stateAcademic === 'MATRICULADO' ? 'success' : 'secondary'}>
                      {translate(`senaAttendanceApp.StateAcademic.${apprentice.stateAcademic}`)}
                    </Badge>
                  </td>
                  <td className="text-end">
                    <Button variant="danger" size="sm" disabled={!canManage} onClick={() => setUnlinkTarget(apprentice)}>
                      <FontAwesomeIcon icon="right-from-bracket" />
                      &nbsp; Desvincular
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loadingList && <div className="alert alert-success">Esta ficha aún no tiene aprendices vinculados.</div>
        )}
      </div>

      {unlinkTarget && (
        <div className="modal d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Desvincular aprendiz</h5>
                <button type="button" className="btn-close" onClick={() => setUnlinkTarget(null)} />
              </div>
              <div className="modal-body">
                <p>
                  ¿Seguro que quieres desvincular a{' '}
                  {unlinkTarget.student ? `${unlinkTarget.student.firstName} ${unlinkTarget.student.firstLastName}` : 'este aprendiz'} de
                  esta ficha?
                </p>
                <label htmlFor="grade-apprentice-unlink-reason">Motivo de desvinculación</label>
                <select
                  id="grade-apprentice-unlink-reason"
                  className="form-select"
                  value={unlinkReason}
                  onChange={e => setUnlinkReason(e.target.value as (typeof UNLINK_REASONS)[number])}
                >
                  {UNLINK_REASONS.map(value => (
                    <option value={value} key={value}>
                      {translate(`senaAttendanceApp.StateAcademic.${value}`)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="modal-footer">
                <Button variant="secondary" onClick={() => setUnlinkTarget(null)}>
                  Cancelar
                </Button>
                <Button variant="danger" onClick={confirmUnlink} disabled={updating}>
                  <FontAwesomeIcon icon="right-from-bracket" />
                  &nbsp; Desvincular
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GradeApprenticesTab;

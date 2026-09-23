import React, { useEffect, useState } from 'react';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'react-bootstrap';
import { Translate, translate } from 'react-jhipster';
import { useNavigate, useParams } from 'react-router';
import { toast } from 'react-toastify';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity, unlinkApprentice } from './apprentice.reducer';

// UC008-A1: desvincular exige un motivo de retiro (no un DELETE genérico); el backend conserva
// el registro con ese motivo cuando el aprendiz ya tiene asistencias en la ficha (200) o lo
// elimina cuando no las tiene (204).
const UNLINK_REASONS = ['RETIRO_VOLUNTARIO', 'APLAZADO', 'CANCELADO'] as const;

export const ApprenticeUnlinkDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();
  const [reason, setReason] = useState<(typeof UNLINK_REASONS)[number]>('RETIRO_VOLUNTARIO');

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const apprenticeEntity = useAppSelector(state => state.apprentice.entity);
  const updating = useAppSelector(state => state.apprentice.updating);

  const handleClose = () => {
    navigate('/apprentice');
  };

  const confirmUnlink = async () => {
    const resultAction = await dispatch(unlinkApprentice({ id: apprenticeEntity.id!, reason }));
    if (unlinkApprentice.fulfilled.match(resultAction)) {
      const historyKept = resultAction.payload.status === 200;
      toast.success(
        historyKept
          ? translate('senaAttendanceApp.apprentice.unlink.successKept')
          : translate('senaAttendanceApp.apprentice.unlink.success'),
      );
      handleClose();
    }
  };

  return (
    <Modal show onHide={handleClose}>
      <ModalHeader data-cy="apprenticeUnlinkDialogHeading" closeButton>
        <Translate contentKey="senaAttendanceApp.apprentice.unlink.title">Desvincular aprendiz</Translate>
      </ModalHeader>
      <ModalBody>
        <p>
          <Translate
            contentKey="senaAttendanceApp.apprentice.unlink.question"
            interpolate={{ documentNumber: apprenticeEntity.student?.documentNumber }}
          >
            ¿Seguro que quieres desvincular a este aprendiz de la ficha?
          </Translate>
        </p>
        <label htmlFor="apprentice-unlink-reason">
          <Translate contentKey="senaAttendanceApp.apprentice.unlink.reason">Motivo de desvinculación</Translate>
        </label>
        <select
          id="apprentice-unlink-reason"
          data-cy="unlinkReason"
          className="form-select"
          value={reason}
          onChange={e => setReason(e.target.value as (typeof UNLINK_REASONS)[number])}
        >
          {UNLINK_REASONS.map(value => (
            <option value={value} key={value}>
              {translate(`senaAttendanceApp.StateAcademic.${value}`)}
            </option>
          ))}
        </select>
      </ModalBody>
      <ModalFooter>
        <Button variant="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp;
          <Translate contentKey="entity.action.cancel">Cancel</Translate>
        </Button>
        <Button data-cy="entityConfirmUnlinkButton" variant="danger" onClick={confirmUnlink} disabled={updating}>
          <FontAwesomeIcon icon="right-from-bracket" />
          &nbsp;
          <Translate contentKey="senaAttendanceApp.apprentice.unlink.confirm">Desvincular</Translate>
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default ApprenticeUnlinkDialog;

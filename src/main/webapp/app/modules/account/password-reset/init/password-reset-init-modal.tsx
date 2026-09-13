import React, { useEffect } from 'react';
import { Alert, Button, Modal, ModalBody, ModalHeader, Row } from 'react-bootstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';

import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { handlePasswordResetInit, reset } from '../password-reset.reducer';
import { getEntities as getDocumentTypes } from 'app/entities/document-type/document-type.reducer';

export interface IPasswordResetInitModalProps {
  showModal?: boolean;
  handleClose?: () => void;
}

export const PasswordResetInitModal = (props: IPasswordResetInitModalProps) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getDocumentTypes({}));
    return () => {
      dispatch(reset());
    };
  }, []);

  const handleValidSubmit = ({ documentNumber, documentTypeId }: Record<string, any>) => {
    dispatch(handlePasswordResetInit({ documentTypeId, documentNumber }));
  };

  const successMessage = useAppSelector(state => state.passwordReset.successMessage);
  const documentTypes = useAppSelector(state => state.documentType.entities);

  useEffect(() => {
    if (successMessage) {
      toast.success(translate(successMessage));
    }
  }, [successMessage]);

  return (
    <Modal show={props.showModal} onHide={props.handleClose} id={'password-reset-init-modal'} autoFocus={false}>
      <Row className="justify-content-center">
        <div className="pad">
          <ModalHeader id="recovery-title" data-cy="recoveryTitle" closeButton>
            <h1>
              <Translate contentKey="reset.request.title">Reset your password</Translate>
            </h1>
          </ModalHeader>
          <ModalBody>
            <Alert variant="warning">
              <p>
                <Translate contentKey="reset.request.messages.info">Enter the document type and number used upon registration</Translate>
              </p>
            </Alert>
            <ValidatedForm onSubmit={handleValidSubmit}>
              <ValidatedField
                id="documentTypeId"
                name="documentTypeId"
                data-cy="documentTypeId"
                label="Tipo de Documento"
                type="select"
                required
                validate={{ required: 'Selecciona un tipo de documento' }}
              >
                <option value="" key="0">
                  Selecciona una opcion
                </option>
                {documentTypes
                  ? documentTypes.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                name="documentNumber"
                label="Numero de Documento"
                placeholder="Numero de Documento"
                type="text"
                required
                validate={{ required: 'Ingrese su numero de documento' }}
                data-cy="documentNumber"
              />
              <Button variant="primary" type="submit" data-cy="submit">
                <Translate contentKey="reset.request.form.button">Reset password</Translate>
              </Button>
            </ValidatedForm>
          </ModalBody>
        </div>
      </Row>
    </Modal>
  );
};

export default PasswordResetInitModal;

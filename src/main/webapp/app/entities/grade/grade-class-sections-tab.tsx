import React, { useEffect } from 'react';
import { Badge, Button, Table } from 'react-bootstrap';
import { Link } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { IClassSection } from 'app/shared/model/class-section.model';

import { deleteEntity, getEntities, partialUpdateEntity } from '../class-section/class-section.reducer';

interface GradeClassSectionsTabProps {
  gradeId: string;
  canManage: boolean;
}

export const GradeClassSectionsTab = ({ gradeId, canManage }: GradeClassSectionsTabProps) => {
  const dispatch = useAppDispatch();

  const classSections = useAppSelector(state => state.classSection.entities);
  const loadingList = useAppSelector(state => state.classSection.loading);

  useEffect(() => {
    dispatch(getEntities({ page: 0, size: 1000, sort: 'subjectName,asc' }));
  }, []);

  const gradeClassSections = (classSections ?? []).filter(classSection => classSection.grade?.id === gradeId);

  const handleDelete = (classSection: IClassSection) => {
    if (globalThis.confirm(`¿Eliminar la competencia "${classSection.subjectName}"?`)) {
      dispatch(deleteEntity(classSection.id!));
    }
  };

  const handleToggleActive = (classSection: IClassSection) => {
    dispatch(partialUpdateEntity({ id: classSection.id, isActive: !classSection.isActive }));
  };

  return (
    <div>
      <div className="d-flex justify-content-end mb-3">
        <Button as={Link as any} to={`/class-section/new?gradeId=${gradeId}`} variant="success" disabled={!canManage}>
          <FontAwesomeIcon icon="plus" />
          &nbsp; Añadir Competencia
        </Button>
      </div>

      {!canManage && (
        <div className="alert alert-warning">
          Esta ficha no admite crear, modificar, desactivar ni eliminar competencias en su estado actual; solo puede consultarlas.
        </div>
      )}

      <div className="table-responsive">
        {gradeClassSections.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>Nombre de la competencia</th>
                <th>Instructor asignado</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {gradeClassSections.map(classSection => (
                <tr key={`class-section-${classSection.id}`} data-cy="entityTable">
                  <td>{classSection.subjectName}</td>
                  <td>{classSection.instructor ? classSection.instructor.documentNumber : 'Sin asignar'}</td>
                  <td>
                    <Badge bg={classSection.isActive ? 'success' : 'secondary'}>{classSection.isActive ? 'Activa' : 'Inactiva'}</Badge>
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        as={Link as any}
                        to={`/class-section/${classSection.id}/edit?gradeId=${gradeId}`}
                        variant="primary"
                        size="sm"
                        disabled={!canManage}
                      >
                        <FontAwesomeIcon icon="pencil-alt" />
                      </Button>
                      <Button
                        variant={classSection.isActive ? 'secondary' : 'success'}
                        size="sm"
                        disabled={!canManage}
                        onClick={() => handleToggleActive(classSection)}
                      >
                        {classSection.isActive ? 'Desactivar' : 'Reactivar'}
                      </Button>
                      <Button variant="danger" size="sm" disabled={!canManage} onClick={() => handleDelete(classSection)}>
                        <FontAwesomeIcon icon="trash" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loadingList && <div className="alert alert-success">Esta ficha aún no tiene competencias registradas.</div>
        )}
      </div>
    </div>
  );
};

export default GradeClassSectionsTab;

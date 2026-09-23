import React, { useEffect, useState } from 'react';
import { Badge, Button, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { Link, useLocation, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';

import { getStudentHistory } from './alert.reducer';

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

// UC013, A5: historial completo de alertas de un aprendiz (por ficha y trimestre), acotado al
// alcance de lectura de quien consulta.
export const AlertStudentHistory = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const { studentId } = useParams<'studentId'>();

  const [activePage, setActivePage] = useState(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'generatedAt').activePage);

  const history = useAppSelector(state => state.alert.studentHistory);
  const totalItems = useAppSelector(state => state.alert.studentHistoryTotalItems);
  const loading = useAppSelector(state => state.alert.loading);

  useEffect(() => {
    dispatch(getStudentHistory({ studentId: studentId!, page: activePage - 1, size: ITEMS_PER_PAGE, sort: 'generatedAt,desc' }));
  }, [studentId, activePage]);

  return (
    <div>
      <h2>Historial de alertas del aprendiz</h2>
      <div className="table-responsive">
        {history?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>Generada</th>
                <th>Tipo</th>
                <th>Materia / Ficha</th>
                <th>Conteo</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {history.map(alerta => (
                <tr key={`entity-${alerta.id}`}>
                  <td>{alerta.generatedAt ? <TextFormat type="date" value={alerta.generatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{alerta.type === 'CONSECUTIVAS' ? 'Consecutivas' : 'Acumuladas'}</td>
                  <td>{alerta.type === 'CONSECUTIVAS' ? alerta.classSection?.subjectName : `Ficha ${alerta.grade?.code ?? ''}`}</td>
                  <td>
                    {alerta.absenceCount} / {alerta.threshold}
                  </td>
                  <td>
                    <Badge bg={stateVariant[alerta.state ?? ''] ?? 'secondary'}>{stateLabel[alerta.state ?? ''] ?? alerta.state}</Badge>
                  </td>
                  <td className="text-end">
                    <Button as={Link as any} to={`/alert/${alerta.id}`} variant="info" size="sm">
                      <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">Revisar</span>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-success">Este aprendiz no tiene alertas registradas.</div>
        )}
      </div>
      {totalItems ? (
        <div className={history && history.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={activePage} total={totalItems} itemsPerPage={ITEMS_PER_PAGE} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={activePage}
              onSelect={setActivePage}
              maxButtons={5}
              itemsPerPage={ITEMS_PER_PAGE}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
      <Button as={Link as any} to="/alert" replace variant="info">
        <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver a la bandeja</span>
      </Button>
    </div>
  );
};

export default AlertStudentHistory;

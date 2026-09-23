import React, { useEffect, useState } from 'react';
import { Badge, Button, Col, Row, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { useLocation, useNavigate } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getEntities, markAllAsRead, markAsRead } from './notification.reducer';

const typeLabel: Record<string, string> = {
  CREDENTIALS: 'Credenciales',
  JUSTIFICACION: 'Justificación',
  ALERTA: 'Alerta de inasistencia',
};

// Resuelve a dónde navegar al abrir una notificación (paso 3 de UC018): solo si la referencia
// es de un tipo conocido y el rol actual puede realmente leer ese objeto. La justificación de
// UC011 solo la lee su dueño (Aprendiz) o el Admin; la bandeja de decisión (UC010) del
// instructor no tiene una ruta de detalle por id de justificación, así que no hay a dónde
// llevarlo. Una alerta (UC013) solo la lee Admin/Instructor; el aprendiz solo recibe el aviso.
const resolveReferenceLink = (referenceType: string | null | undefined, referenceId: string | null | undefined, authorities: string[]) => {
  if (!referenceType || !referenceId) {
    return undefined;
  }
  const isAdmin = hasAnyAuthority(authorities, [Authority.ADMIN]);
  const isApprentice = hasAnyAuthority(authorities, [Authority.APPRENTICE]);
  const isInstructor = hasAnyAuthority(authorities, [Authority.INSTRUCTOR]);
  if (referenceType === 'JUSTIFICATION' && (isAdmin || isApprentice)) {
    return `/justification/${referenceId}`;
  }
  if (referenceType === 'ALERT' && (isAdmin || isInstructor)) {
    return `/alert/${referenceId}`;
  }
  return undefined;
};

// UC018, flujo básico: bandeja in-app del usuario autenticado, con estado de entrega y de
// lectura independientes. Abrir una notificación la marca como leída y, si hay referencia
// accesible, navega al detalle del objeto de origen.
export const Notification = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'createdDate'), pageLocation.search),
  );
  const [readFilter, setReadFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  const authorities = useAppSelector(state => state.authentication.account.authorities);
  const notificationList = useAppSelector(state => state.notification.entities);
  const loading = useAppSelector(state => state.notification.loading);
  const totalItems = useAppSelector(state => state.notification.totalItems);
  const unreadCount = useAppSelector(state => state.notification.unreadCount);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
        read: readFilter === '' ? undefined : readFilter === 'true',
        type: typeFilter || undefined,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, readFilter, typeFilter]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleMarkAllAsRead = async () => {
    const resultAction = await dispatch(markAllAsRead());
    if (markAllAsRead.fulfilled.match(resultAction)) {
      getAllEntities();
    }
  };

  const handleOpen = (notification: (typeof notificationList)[number]) => {
    if (!notification.read) {
      dispatch(markAsRead(notification.id!));
    }
    const link = resolveReferenceLink(notification.referenceType, notification.referenceId, authorities);
    if (link) {
      navigate(link);
    }
  };

  return (
    <div>
      <h2 id="notification-heading" data-cy="NotificationHeading">
        Notificaciones {unreadCount > 0 && <Badge bg="danger">{unreadCount} sin leer</Badge>}
        <div className="d-flex justify-content-end">
          <Button variant="outline-secondary" onClick={handleMarkAllAsRead} disabled={unreadCount === 0}>
            <FontAwesomeIcon icon="check" /> Marcar todas como leídas
          </Button>
        </div>
      </h2>
      <Row className="mb-3">
        <Col md="3">
          <label htmlFor="read-filter">Estado</label>
          <select id="read-filter" className="form-select" value={readFilter} onChange={e => setReadFilter(e.target.value)}>
            <option value="">Todas</option>
            <option value="false">No leídas</option>
            <option value="true">Leídas</option>
          </select>
        </Col>
        <Col md="3">
          <label htmlFor="type-filter">Tipo</label>
          <select id="type-filter" className="form-select" value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
            <option value="">Todos</option>
            <option value="CREDENTIALS">Credenciales</option>
            <option value="JUSTIFICACION">Justificación</option>
            <option value="ALERTA">Alerta de inasistencia</option>
          </select>
        </Col>
      </Row>
      <div className="table-responsive">
        {notificationList?.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>Tipo</th>
                <th>Mensaje</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {notificationList.map(notification => (
                <tr key={`entity-${notification.id}`} className={notification.read ? '' : 'fw-bold'}>
                  <td>{typeLabel[notification.tipo ?? ''] ?? notification.tipo}</td>
                  <td>{notification.mensaje}</td>
                  <td>
                    {notification.createdDate ? <TextFormat type="date" value={notification.createdDate} format={APP_DATE_FORMAT} /> : null}
                  </td>
                  <td>
                    <Badge bg={notification.read ? 'secondary' : 'warning'}>{notification.read ? 'Leída' : 'No leída'}</Badge>
                  </td>
                  <td className="text-end">
                    <Button variant="info" size="sm" onClick={() => handleOpen(notification)}>
                      <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">Abrir</span>
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-success">No tienes notificaciones.</div>
        )}
      </div>
      {totalItems ? (
        <div className={notificationList && notificationList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default Notification;

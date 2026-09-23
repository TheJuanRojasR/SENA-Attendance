import React from 'react';
import { Col, Row } from 'react-bootstrap';
import { Link } from 'react-router';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faUserPlus } from '@fortawesome/free-solid-svg-icons';
import { useAppSelector } from 'app/config/store';

import './dashboard.scss';

import { AprenticeDashboard } from './apprentice/dashboard-aprentice';
import { CoordinatorDashboard } from './coordinator/dashboard-coordinator';
import { InstructorDashboard } from './instructor/dashboard-instructor';
import { AdminDashboard } from './admin/dashboard-admin';

export interface IDashboardProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isCoordinator: boolean;
  isInstructor: boolean;
  isAprentice: boolean;
}

const Dashboard = (props: IDashboardProps) => {
  const account = useAppSelector(state => state.authentication.account);
  // Construir nombre dinámico
  const fullName = account?.firstName ? `${account.firstName} ${account.lastName || ''}`.trim() : account?.login;

  return (
    <div className="dashboard-wrapper">
      {props.isAuthenticated && (
        <Row className="dashboard-header d-flex justify-content-between align-items-center mb-4">
          <Col md="8" className="header-text">
            <h2 className="fw-bold" style={{ color: '#112236' }}>
              Bienvenido de nuevo, {fullName || 'Usuario'}
            </h2>

            {props.isAdmin && (
              <p className="text-muted mb-0" style={{ maxWidth: '850px' }}>
                Panel de control general del sistema de asistencia SENA. Visualice el estado global de las fichas, supervise el
                funcionamiento de la plataforma y gestione las alertas administrativas.
              </p>
            )}
            {props.isInstructor && (
              <p className="text-muted mb-0" style={{ maxWidth: '850px' }}>
                Panel de control del instructor. Consulte el estado general de sus formaciones asignadas, gestione el registro de asistencia
                y revise las solicitudes de justificación pendientes.
              </p>
            )}
            {props.isAprentice && (
              <p className="text-muted mb-0" style={{ maxWidth: '850px' }}>
                Panel de control del aprendiz. Consulte la información general de su programa de formación, el estado de sus asistencias
                registradas y el historial de sus justificaciones.
              </p>
            )}
          </Col>

          {/* Botones de acción del Admin movidos al encabezado global */}
          {props.isAdmin && (
            <Col md="4" className="header-actions d-flex gap-3 justify-content-end mt-3 mt-md-0">
              <Link
                to="/grade/new"
                className="btn btn-outline-success d-flex align-items-center gap-2"
                style={{ padding: '0.5rem 1.2rem', borderRadius: '8px', fontWeight: '600', color: '#16c829', borderColor: '#16c829' }}
              >
                <FontAwesomeIcon icon={faPlus} />
                Crear Ficha
              </Link>
              <Link
                to="/admin/users/new"
                className="btn btn-success d-flex align-items-center gap-2 text-white"
                style={{
                  padding: '0.5rem 1.2rem',
                  borderRadius: '8px',
                  fontWeight: '600',
                  backgroundColor: '#1a6b0c',
                  borderColor: '#1a6b0c',
                }}
              >
                <FontAwesomeIcon icon={faUserPlus} />
                Crear Nuevo Usuario
              </Link>
            </Col>
          )}
        </Row>
      )}

      {/* Renderizado dinámico según el rol */}
      <Row>
        <Col md="12">
          {props.isAuthenticated && props.isAdmin && <AdminDashboard />}
          {props.isAuthenticated && props.isCoordinator && <CoordinatorDashboard />}
          {props.isAuthenticated && props.isInstructor && <InstructorDashboard />}
          {props.isAuthenticated && props.isAprentice && <AprenticeDashboard />}
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;

import React from 'react';

import './dashboard.scss';
import { Col, Row } from 'react-bootstrap';

import { AprenticeDashboard } from './apprentice/dashboard-aprentice';
import { CoordinatorDashboard } from './coordinator/dashboard-coordinator';
import { DashboardDescription, WelcomeBanner } from './dashboard-components';
import { InstructorDashboard } from './instructor/dashboard-instructor';
import { AdminDashboard } from './admin/dashboard-admin';
import LinkButton from 'app/shared/components/link-button';

export interface IDashboardProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isCoordinator: boolean;
  isInstructor: boolean;
  isAprentice: boolean;
}

const Dashboard = (props: IDashboardProps) => {
  return (
    <Row className="d-flex justify-content-between">
      <Col md="8" className="welcome-text">
        {props.isAuthenticated && <WelcomeBanner />}
        {props.isAuthenticated && props.isAdmin && (
          <DashboardDescription>
            Panel de control general del sistema de asistencia SENA. Visualice el estado global de las fichas, supervise el funcionamiento
            de la plataforma y gestione las alertas administrativas.
          </DashboardDescription>
        )}
        {props.isAuthenticated && props.isInstructor && (
          <DashboardDescription>
            Panel de control del instructor. Consulte el estado general de sus formaciones asignadas, gestione el registro de asistencia y
            revise las solicitudes de justificación pendientes.
          </DashboardDescription>
        )}
        {props.isAuthenticated && props.isAprentice && (
          <DashboardDescription>
            Panel de control del aprendiz. Consulte la información general de su programa de formación, el estado de sus asistencias
            registradas y el historial de sus justificaciones.
          </DashboardDescription>
        )}
      </Col>
      {props.isAdmin && (
        <Col md="4" className="admin-actions">
          <LinkButton to="/grade/new" translationKey="senaAttendanceApp.grade.home.createLabel">
            Create Grade
          </LinkButton>
          <LinkButton to="/admin/users/new" translationKey="userManagement.home.createLabel">
            Create User
          </LinkButton>
        </Col>
      )}
      <Col md="12">
        {props.isAuthenticated && props.isAdmin && <AdminDashboard />}
        {props.isAuthenticated && props.isCoordinator && <CoordinatorDashboard />}
        {props.isAuthenticated && props.isInstructor && <InstructorDashboard />}
        {props.isAuthenticated && props.isAprentice && <AprenticeDashboard />}
      </Col>
    </Row>
  );
};

export default Dashboard;

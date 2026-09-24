import React, { useEffect } from 'react';
import { Link } from 'react-router';
import { Card, Table } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGraduationCap, faShapes, faUserGroup, faUsers, faPencilAlt } from '@fortawesome/free-solid-svg-icons';

import { getDashboard as getAdminKpis } from 'app/modules/dashboard/dashboard.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import './dashboard-admin.scss';

export const AdminDashboard = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getAdminKpis());
  }, []);

  const adminKpis = useAppSelector(state => state.dashboard.dashboard.kpis);
  const recentGrades = useAppSelector(state => state.dashboard.dashboard.recentGrades) ?? [];

  return (
    <div className="admin-dashboard-container pt-0">
      {/* 1. SECCIÓN DE TARJETAS KPI (Limpias, sin textos estáticos inventados) */}
      <div className="kpi-cards-container d-flex flex-column flex-lg-row justify-content-between mb-4 gap-3">
        <Card className="kpi-card flex-fill">
          <div className="d-flex justify-content-between align-items-start mb-3">
            <div className="icon-wrapper green-bg">
              <FontAwesomeIcon icon={faUserGroup} className="kpi-icon" />
            </div>
          </div>
          <p className="kpi-title text-muted text-uppercase mb-1">Total Usuarios</p>
          <h3 className="kpi-value fw-bold text-success mb-0">{adminKpis?.totalUsers || '0'}</h3>
        </Card>

        <Card className="kpi-card flex-fill">
          <div className="d-flex justify-content-between align-items-start mb-3">
            <div className="icon-wrapper green-bg">
              <FontAwesomeIcon icon={faUsers} className="kpi-icon" />
            </div>
          </div>
          <p className="kpi-title text-muted text-uppercase mb-1">Fichas Activas</p>
          <h3 className="kpi-value fw-bold text-success mb-0">{adminKpis?.activeGrades || '0'}</h3>
        </Card>

        <Card className="kpi-card flex-fill">
          <div className="d-flex justify-content-between align-items-start mb-3">
            <div className="icon-wrapper green-bg">
              <FontAwesomeIcon icon={faGraduationCap} className="kpi-icon" />
            </div>
          </div>
          <p className="kpi-title text-muted text-uppercase mb-1">Programas de Formación</p>
          <h3 className="kpi-value fw-bold text-success mb-0">{adminKpis?.totalPrograms || '0'}</h3>
        </Card>

        <Card className="kpi-card flex-fill">
          <div className="d-flex justify-content-between align-items-start mb-3">
            <div className="icon-wrapper green-bg">
              <FontAwesomeIcon icon={faShapes} className="kpi-icon" />
            </div>
          </div>
          <p className="kpi-title text-muted text-uppercase mb-1">Modalidades de Estudio</p>
          <h3 className="kpi-value fw-bold text-success mb-0">{adminKpis?.totalModalities || '0'}</h3>
        </Card>
      </div>

      {/* 2. SECCIÓN DE TABLA (Fichas Recientes) */}
      <Card className="recent-grades-card">
        <div className="d-flex justify-content-between align-items-center p-4 border-bottom bg-light rounded-top">
          <h5 className="mb-0 fw-bold text-dark">Fichas Creadas Recientemente</h5>
          <Link to="/grade" className="text-success fw-bold text-decoration-none">
            Ver Todo
          </Link>
        </div>
        <div className="table-responsive p-0 m-0">
          {recentGrades.length > 0 ? (
            <Table className="custom-dashboard-table mb-0" hover>
              <thead className="bg-light text-muted text-uppercase">
                <tr>
                  <th className="ps-4">ID Ficha</th>
                  <th>Nombre del Programa</th>
                  <th>Instructor</th>
                  <th>Estado</th>
                  <th className="text-center pe-4">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {recentGrades.map((grade, i) => (
                  <tr key={`grade-${grade.id || i}`}>
                    <td className="ps-4 fw-bold text-success">{grade.code}</td>
                    <td className="text-dark fw-bold">{grade.programName}</td>
                    <td className="text-dark">{grade.instructorName || 'Sin asignar'}</td>
                    <td>
                      <span className={`status-badge ${grade.state?.toLowerCase() === 'pendiente' ? 'status-pending' : 'status-active'}`}>
                        {grade.state || 'ACTIVO'}
                      </span>
                    </td>
                    <td className="text-center pe-4">
                      <Link to={`/grade/${grade.id}/edit`} className="action-icon" title="Editar Ficha">
                        <FontAwesomeIcon icon={faPencilAlt} />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : (
            <div className="p-4 text-center text-muted">No hay fichas creadas recientemente.</div>
          )}
        </div>
      </Card>
    </div>
  );
};

export default AdminDashboard;

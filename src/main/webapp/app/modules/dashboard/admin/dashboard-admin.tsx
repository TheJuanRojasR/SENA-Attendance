import React, { useEffect } from 'react';

import './dashboard-admin.scss';
import { Button, Card, Table } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGraduationCap, faShapes, faUserGroup, faUsers } from '@fortawesome/free-solid-svg-icons';

import { getAdminDashboard as getAdminKpis } from 'app/modules/dashboard/dashboard.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const AdminDashboard = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getAdminKpis());
  }, []);

  const adminKpis = useAppSelector(state => state.dashboard.dashboard.kpis);
  const recentGrades = useAppSelector(state => state.dashboard.dashboard.recentGrades) ?? [];

  return (
    <div>
      <div className="card-container d-flex justify-content-around mb-5">
        <Card className="statistics-card">
          <FontAwesomeIcon icon={faUserGroup} className="card-icon" />
          <p>Total de Usuarios</p>
          <p className="card-data">{adminKpis?.totalUsers}</p>
        </Card>
        <Card className="statistics-card">
          <FontAwesomeIcon icon={faUsers} className="card-icon" />
          <p>Fichas Activas</p>
          <p className="card-data">{adminKpis?.activeGrades}</p>
        </Card>
        <Card className="statistics-card">
          <FontAwesomeIcon icon={faGraduationCap} className="card-icon" />
          <p>Programas de Formación</p>
          <p className="card-data">{adminKpis?.totalPrograms}</p>
        </Card>
        <Card className="statistics-card">
          <FontAwesomeIcon icon={faShapes} className="card-icon" />
          <p>Modalidades de Estudio</p>
          <p className="card-data">{adminKpis?.totalModalities}</p>
        </Card>
      </div>
      <div className="table-responsive">
        {recentGrades.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID Ficha</th>
                <th>Nombre del Programa</th>
                <th>Instructor</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {recentGrades.map(grade => (
                <tr>
                  <td>{grade.code}</td>
                  <td>{grade.programName}</td>
                  <td>{grade.instructorName}</td>
                  <td>{grade.state}</td>
                  <td>
                    <Button>Ver Detalle</Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          <p>No hay fichas</p>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;

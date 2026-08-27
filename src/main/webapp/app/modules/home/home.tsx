import './home.scss';

import React from 'react';
import { Button, Card, Col, Row } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { Link } from 'react-router';

import { faListCheck } from '@fortawesome/free-solid-svg-icons';
import { faFileAlt } from '@fortawesome/free-solid-svg-icons';
import { faChartLine } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const Home = () => {
  return (
    <Row>
      <Col md="7" className="d-flex flex-column justify-content-evenly">
        <h1 className="display-4">
          <Translate contentKey="home.title">Optimization of Academic Management and Attendance</Translate>
        </h1>
        <p className="lead">
          <Translate contentKey="home.subtitle">
            An institutional platform designed to facilitate monitoring and tracking. Precise tools for instructors, trainees, and
            administrative staff that ensure transparency and efficiency in SENA's processes.
          </Translate>
        </p>
        <Button as={Link as any} to="/login" size="lg" color="primary" className="button-generic">
          <Translate contentKey="home.button">Access the platform</Translate>
        </Button>
      </Col>
      <Col md="5" className="pad">
        <span className="hipster rounded" />
      </Col>
      <Col md="12" className="pad home-content">
        <h5>Capacidades Centrales</h5>
        <p>Herramientas robustas diseñadas para optimizar la gestión académica y el control de asistencia en SENA.</p>
      </Col>
      <Col md="12" className="pad home-cards">
        <Card className="home-card">
          <FontAwesomeIcon className="home-icon" icon={faListCheck} size="2x" />
          <h6>Seguimiento de Asistencia</h6>
          <p>Registro digital y centralizado para control diario por ficha y competencia, asegurando trazabilidad completa.</p>
        </Card>
        <Card className="home-card">
          <FontAwesomeIcon className="home-icon" icon={faFileAlt} size="2x" />
          <h6> Gestion de Justificaciones </h6>
          <p>Flujo automatizado para la radicación y aprobación de excusas médicas e inasistencias con soporte documental.</p>
        </Card>
        <Card className="home-card">
          <FontAwesomeIcon className="home-icon" icon={faChartLine} size="2x" />
          <h6> Reportes en Tiempo Real </h6>
          <p>Generación de indicadores de deserción y ausentismo para toma de decisiones oportunas en coordinación.</p>
        </Card>
      </Col>
    </Row>
  );
};

export default Home;

import './footer.scss';

import React from 'react';
import { Col, Row } from 'react-bootstrap';

export const BrandIcon = props => (
  <div {...props} className="brand-icon">
    <img src="content/images/logo.png" alt="Logo" />
  </div>
);

const Footer = () => (
  <div className="footer page-content">
    <Row className="footer-branding">
      <Col md="12">
        <BrandIcon />
        <span className="footer-brand-title">
          <span> SENA </span>
          <span> Attendance </span>
        </span>
      </Col>
      <Col md="12">
        <span className="footer-text">
          <span>© 2024 SENA Attendance.</span>
          <span>Todos los derechos reservados.</span>
        </span>
      </Col>
      <Col md="12">
        <span className="footer-version">Version 1.0.0</span>
      </Col>
    </Row>
    <Row className="footer-branding">
      <Col>
        <span className="footer-links">
          <span>Politica de Privacidad</span>
        </span>
      </Col>
      <Col>
        <span className="footer-links">
          <span>Términos y Condiciones</span>
        </span>
      </Col>
      <Col>
        <span className="footer-links">
          <span>Contacto</span>
        </span>
      </Col>
    </Row>
  </div>
);

export default Footer;

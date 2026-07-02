import React from 'react';
import { NavItem, NavLink, NavbarBrand } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { NavLink as Link } from 'react-router';

import { faHome } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

interface IHomeProps {
  isAuthenticated: boolean;
}

export const BrandIcon = props => (
  <div {...props} className="brand-icon">
    <img src="content/images/logo.png" alt="Logo" />
  </div>
);

export const Brand = (props: IHomeProps) => (
  <NavbarBrand as={Link as any} to={props.isAuthenticated ? '/dashboard' : '/'} className="brand-logo">
    <BrandIcon />
    <span className="brand-title">
      <span> SENA </span>
      <span style={{ color: '#16C829' }}>Attendance</span>
    </span>
    {/* Número de versión eliminado */}
  </NavbarBrand>
);

export const Home = (props: IHomeProps) => (
  <NavItem>
    <NavLink as={Link as any} to={props.isAuthenticated ? '/dashboard' : '/'} className="d-flex align-items-center">
      <FontAwesomeIcon icon={faHome} />
      <span>
        <Translate contentKey="global.menu.home">Home</Translate>
      </span>
    </NavLink>
  </NavItem>
);

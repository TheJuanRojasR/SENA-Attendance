import React from 'react';
import { NavItem, NavLink, NavbarBrand } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { NavLink as Link } from 'react-router';

import { faHome } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

interface IHomeProps {
  isAuthenticated: boolean;
  roleName?: string;
}

export const BrandIcon = props => (
  <div {...props} className="brand-icon">
    <img src="content/images/logo.png" alt="Logo" />
  </div>
);

export const Brand = (props: IHomeProps) => (
  <NavbarBrand as={Link as any} to={props.isAuthenticated ? '/dashboard' : '/'} className="brand-logo d-flex align-items-center">
    <BrandIcon />
    <div className="d-flex flex-column ms-2">
      <span className="brand-title mb-0" style={{ lineHeight: '1.2' }}>
        <span className="logo-sena">SENA </span>
        <span className="logo-attendance">Attendance</span>
      </span>
      {props.roleName && (
        <span className="text-muted" style={{ fontSize: '0.80rem' }}>
          {props.roleName}
        </span>
      )}
    </div>
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

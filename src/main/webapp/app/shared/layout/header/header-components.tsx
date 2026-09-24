import React from 'react';
import { NavItem, NavLink, NavbarBrand } from 'react-bootstrap';
import { Translate } from 'react-jhipster';
import { NavLink as Link } from 'react-router';

import { faHome } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

interface IHomeProps {
  isAuthenticated: boolean;
  roleName?: string;
  icon: any;
  placeholder: string;
}

interface IBrandProps {
  isAuthenticated: boolean;
  roleName?: string;
}

interface INotificationsProps {
  isAuthenticated: boolean;
  icon: any;
}

interface IUserInfoProps {
  fullName: string;
  role: string;
}

export const BrandIcon = props => (
  <div {...props} className="brand-icon">
    <img src="content/images/logo.png" alt="Logo" />
  </div>
);

export const Brand = (props: IBrandProps) => (
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

export const Notifications = (props: INotificationsProps) => (
  <NavItem className="notifications">
    <FontAwesomeIcon icon={props.icon} />
  </NavItem>
);

export const UserInfo = (props: IUserInfoProps) => (
  <NavItem className="user-info">
    <div className="d-flex align-items-center">
      <div className="user-info-text">
        <p className="user-info-name">{props.fullName}</p>
        <p className="user-info-role">{props.role}</p>
      </div>
      <div className="user-info-avatar">{props.fullName?.trim().charAt(0).toUpperCase() || '?'}</div>
    </div>
  </NavItem>
);

import React from 'react';
import { NavItem, NavbarBrand } from 'react-bootstrap';
import { NavLink as Link } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

interface IHeaderProps {
  isAuthenticated: boolean;
}

interface ISearchBarProps {
  isAuthenticated: boolean;
  icon: any;
  placeholder: string;
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

export const Brand = (props: IHeaderProps) => (
  <NavbarBrand as={Link as any} to={props.isAuthenticated ? '/dashboard' : '/'} className="brand-logo">
    <BrandIcon />
    <span className="brand-title">
      <span> SENA </span>
      <span style={{ color: '#16C829' }}>Attendance</span>
    </span>
    {/* Número de versión eliminado */}
  </NavbarBrand>
);

export const SearchBar = (props: ISearchBarProps) => (
  <NavItem className="search-bar d-flex align-items-center">
    <FontAwesomeIcon icon={props.icon} />
    <input type="search" placeholder={props.placeholder} />
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

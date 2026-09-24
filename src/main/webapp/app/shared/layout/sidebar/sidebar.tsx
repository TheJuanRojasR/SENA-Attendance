import './sidebar.scss';

import React from 'react';
import { Navbar } from 'react-bootstrap';

import { AccountMenuItemsAuthenticated, EntitiesMenu } from '../menus';
import MenuItem from '../menus/menu-item';
import { Brand } from '../header/header-components';
import { useAppSelector } from 'app/config/store';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isOpenAPIEnabled: boolean;
}

// Función para mapear las authorities de JHipster a texto legible
const getRoleName = (authorities: string[]) => {
  if (authorities.includes('ROLE_ADMIN')) return 'Super Admin';
  if (authorities.includes('ROLE_COORDINATOR')) return 'Coordinador';
  if (authorities.includes('ROLE_INSTRUCTOR')) return 'Instructor';
  if (authorities.includes('ROLE_APPRENTICE')) return 'Aprendiz';
  return 'Usuario';
};

const Sidebar = (props: IHeaderProps) => {
  const account = useAppSelector(state => state.authentication.account);
  const roleName = account?.authorities ? getRoleName(account.authorities) : '';

  if (!props.isAuthenticated) {
    return null;
  } else {
    return (
      <Navbar data-cy="sidebar" data-bs-theme="light" expand="sm" className="sidebar d-flex flex-column align-items-start">
        <div className="sidebar-brand-wrapper w-100">
          <Brand isAuthenticated={props.isAuthenticated} roleName={roleName} />
        </div>

        <div className="sidebar-menu-items flex-grow-1 w-100 mt-4">
          {/* Dashboard Manual */}
          <MenuItem icon="tachometer-alt" to="/dashboard">
            Dashboard
          </MenuItem>
          {/* Menús de entidades mapeados sueltos */}
          <EntitiesMenu />
        </div>

        <div className="sidebar-footer w-100 mt-auto pt-3">{props.isAuthenticated && AccountMenuItemsAuthenticated()}</div>
      </Navbar>
    );
  }
};

export default Sidebar;

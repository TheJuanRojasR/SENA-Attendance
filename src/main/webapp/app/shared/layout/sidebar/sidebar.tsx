import './sidebar.scss';

import React from 'react';
import { Navbar } from 'react-bootstrap';

import { AdminMenu, EntitiesMenu } from '../menus';
import { Brand } from '../header/header-components';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isOpenAPIEnabled: boolean;
}

const Sidebar = (props: IHeaderProps) => {
  if (!props.isAuthenticated) {
    return null;
  } else {
    return (
      <Navbar data-cy="sidebar" data-bs-theme="light" expand="sm" className="sidebar">
        <Brand isAuthenticated={props.isAuthenticated} />
        <div>
          {props.isAuthenticated && props.isAdmin && <AdminMenu showOpenAPI={props.isOpenAPIEnabled} variant="accordion" />}
          <EntitiesMenu variant="accordion" />
        </div>
      </Navbar>
    );
  }
};

export default Sidebar;

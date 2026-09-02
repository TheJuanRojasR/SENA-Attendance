import React, { useState } from 'react';
import { Collapse, Dropdown, DropdownMenu, DropdownToggle, Nav } from 'react-bootstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const NavDropdown = props => (
  <Dropdown as={Nav.Item} id={props.id} data-cy={props['data-cy']}>
    <DropdownToggle as={Nav.Link} className="d-flex align-items-center">
      <FontAwesomeIcon icon={props.icon} />
      <span>{props.name}</span>
    </DropdownToggle>
    <DropdownMenu renderOnMount align="end" style={props.style}>
      {props.children}
    </DropdownMenu>
  </Dropdown>
);

/**
 * Same idea as NavDropdown but for the sidebar: expanding it pushes the items
 * below it down (normal document flow via Collapse) instead of floating over them,
 * and the children render inside a bordered box instead of a dropdown menu.
 */
export const AccordionNavItem = props => {
  const [open, setOpen] = useState(false);

  return (
    <div className="accordion-nav-item" id={props.id} data-cy={props['data-cy']}>
      <Nav.Link
        as="button"
        type="button"
        className="accordion-nav-toggle d-flex align-items-center"
        onClick={() => setOpen(!open)}
        aria-expanded={open}
      >
        <FontAwesomeIcon icon={props.icon} />
        <span>{props.name}</span>
        <FontAwesomeIcon icon="chevron-down" className={`accordion-nav-caret ms-auto${open ? ' accordion-nav-caret-open' : ''}`} />
      </Nav.Link>
      <Collapse in={open}>
        <div>
          <div className="accordion-nav-body" style={props.style}>
            {props.children}
          </div>
        </div>
      </Collapse>
    </div>
  );
};

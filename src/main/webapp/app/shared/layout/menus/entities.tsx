import React from 'react';
import { translate } from 'react-jhipster';

import EntitiesMenuItems from 'app/entities/menu';

import { AccordionNavItem, NavDropdown } from './menu-components';

export const EntitiesMenu = ({ variant = 'dropdown' }: { variant?: 'dropdown' | 'accordion' } = {}) => {
  const Container = variant === 'accordion' ? AccordionNavItem : NavDropdown;
  return (
    <Container
      icon="bars-staggered"
      name={translate('global.menu.entities.main')}
      id="entity-menu"
      data-cy="entity"
      style={variant === 'dropdown' ? { maxHeight: '80vh', overflow: 'auto' } : { maxHeight: '50vh', overflow: 'auto' }}
    >
      <EntitiesMenuItems />
    </Container>
  );
};

import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="cogs" to="/global-configuration">
        <Translate contentKey="global.menu.entities.globalConfiguration" />
      </MenuItem>
      <MenuItem icon="cloud-sun" to="/time-slot">
        <Translate contentKey="global.menu.entities.timeSlot" />
      </MenuItem>
      <MenuItem icon="layer-group" to="/modality">
        <Translate contentKey="global.menu.entities.modality" />
      </MenuItem>
      <MenuItem icon="graduation-cap" to="/program">
        <Translate contentKey="global.menu.entities.program" />
      </MenuItem>
      <MenuItem icon="th-list" to="/grade">
        <Translate contentKey="global.menu.entities.grade" />
      </MenuItem>
      <MenuItem icon="id-card" to="/document-type">
        <Translate contentKey="global.menu.entities.documentType" />
      </MenuItem>
      <MenuItem icon="user" to="/user-profile">
        <Translate contentKey="global.menu.entities.userProfile" />
      </MenuItem>
      <MenuItem icon="user-graduate" to="/apprentice">
        <Translate contentKey="global.menu.entities.apprentice" />
      </MenuItem>
      <MenuItem icon="calendar-alt" to="/trimester">
        <Translate contentKey="global.menu.entities.trimester" />
      </MenuItem>
      <MenuItem icon="book-open" to="/class-section">
        <Translate contentKey="global.menu.entities.classSection" />
      </MenuItem>
      <MenuItem icon="clock" to="/class-schedule">
        <Translate contentKey="global.menu.entities.classSchedule" />
      </MenuItem>
      <MenuItem icon="ban" to="/class-exception">
        <Translate contentKey="global.menu.entities.classException" />
      </MenuItem>
      <MenuItem icon="tag" to="/justification-type">
        <Translate contentKey="global.menu.entities.justificationType" />
      </MenuItem>
      <MenuItem icon="tasks" to="/attendance">
        <Translate contentKey="global.menu.entities.attendance" />
      </MenuItem>
      <MenuItem icon="eye" to="/audit-log">
        <Translate contentKey="global.menu.entities.auditLog" />
      </MenuItem>
      <MenuItem icon="file" to="/justification">
        <Translate contentKey="global.menu.entities.justification" />
      </MenuItem>
      <MenuItem icon="clipboard-check" to="/justification-details">
        <Translate contentKey="global.menu.entities.justificationDetails" />
      </MenuItem>
      <MenuItem icon="bell" to="/desertion-counter">
        <Translate contentKey="global.menu.entities.desertionCounter" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;

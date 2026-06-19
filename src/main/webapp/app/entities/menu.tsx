import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/global-configuration">
        <Translate contentKey="global.menu.entities.globalConfiguration" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/time-slot">
        <Translate contentKey="global.menu.entities.timeSlot" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/modality">
        <Translate contentKey="global.menu.entities.modality" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/program">
        <Translate contentKey="global.menu.entities.program" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/grade">
        <Translate contentKey="global.menu.entities.grade" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/document-type">
        <Translate contentKey="global.menu.entities.documentType" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/user-profile">
        <Translate contentKey="global.menu.entities.userProfile" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/apprentice">
        <Translate contentKey="global.menu.entities.apprentice" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/trimester">
        <Translate contentKey="global.menu.entities.trimester" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/class-section">
        <Translate contentKey="global.menu.entities.classSection" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/class-schedule">
        <Translate contentKey="global.menu.entities.classSchedule" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/class-exception">
        <Translate contentKey="global.menu.entities.classException" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/justification-type">
        <Translate contentKey="global.menu.entities.justificationType" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/attendance">
        <Translate contentKey="global.menu.entities.attendance" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/audit-log">
        <Translate contentKey="global.menu.entities.auditLog" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/justification">
        <Translate contentKey="global.menu.entities.justification" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/justification-details">
        <Translate contentKey="global.menu.entities.justificationDetails" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/desertion-counter">
        <Translate contentKey="global.menu.entities.desertionCounter" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;

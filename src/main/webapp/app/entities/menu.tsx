import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

import { useAppSelector } from 'app/config/store';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import { Authority } from 'app/shared/jhipster/constants';

const EntitiesMenu = () => {
  const authorities = useAppSelector(state => state.authentication.account.authorities);

  const isAdmin = hasAnyAuthority(authorities, [Authority.ADMIN]);
  const isCoordinator = hasAnyAuthority(authorities, ['ROLE_COORDINATOR']);
  const isInstructor = hasAnyAuthority(authorities, ['ROLE_INSTRUCTOR']);
  const isApprentice = hasAnyAuthority(authorities, ['ROLE_APPRENTICE']);

  const adminOrCoordinator = isAdmin || isCoordinator;
  const adminOrInstructor = isAdmin || isInstructor;
  const adminOrApprentice = isAdmin || isApprentice;
  const adminOrCoordinatorOrInstructor = isAdmin || isCoordinator || isInstructor;
  return (
    <>
      {/* prettier-ignore */}
      {/* ══════════════════════════════════════════
          SOLO ADMIN — Configuración del sistema
      ══════════════════════════════════════════ */}
      {isAdmin && (
        <MenuItem icon="eye" to="/audit-log">
          <Translate contentKey="global.menu.entities.auditLog" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="id-card" to="/document-type">
          <Translate contentKey="global.menu.entities.documentType" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="user" to="/user-profile">
          <Translate contentKey="global.menu.entities.userProfile" />
        </MenuItem>
      )}
      {/* ══════════════════════════════════════════
          ADMIN + COORDINADOR — Estructura académica
      ══════════════════════════════════════════ */}
      {adminOrCoordinator && (
        <MenuItem icon="cogs" to="/global-configuration">
          <Translate contentKey="global.menu.entities.globalConfiguration" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="cloud-sun" to="/time-slot">
          <Translate contentKey="global.menu.entities.timeSlot" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="layer-group" to="/modality">
          <Translate contentKey="global.menu.entities.modality" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="graduation-cap" to="/program">
          <Translate contentKey="global.menu.entities.program" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="calendar-alt" to="/trimester">
          <Translate contentKey="global.menu.entities.trimester" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="tag" to="/justification-type">
          <Translate contentKey="global.menu.entities.justificationType" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="user-graduate" to="/apprentice">
          <Translate contentKey="global.menu.entities.apprentice" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="bell" to="/desertion-counter">
          <Translate contentKey="global.menu.entities.desertionCounter" />
        </MenuItem>
      )}
      {adminOrCoordinator && (
        <MenuItem icon="clock" to="/class-schedule">
          <Translate contentKey="global.menu.entities.classSchedule" />
        </MenuItem>
      )}
      {/* ══════════════════════════════════════════
          ADMIN + COORDINADOR + INSTRUCTOR — Gestión de clases
      ══════════════════════════════════════════ */}
      {adminOrCoordinatorOrInstructor && (
        <MenuItem icon="th-list" to="/grade">
          <Translate contentKey="global.menu.entities.grade" />
        </MenuItem>
      )}
      {adminOrCoordinatorOrInstructor && (
        <MenuItem icon="book-open" to="/class-section">
          <Translate contentKey="global.menu.entities.classSection" />
        </MenuItem>
      )}
      {adminOrCoordinatorOrInstructor && (
        <MenuItem icon="ban" to="/class-exception">
          <Translate contentKey="global.menu.entities.classException" />
        </MenuItem>
      )}
      {/* ══════════════════════════════════════════
          ADMIN + INSTRUCTOR — Toma de asistencia
      ══════════════════════════════════════════ */}
      {adminOrInstructor && (
        <MenuItem icon="tasks" to="/attendance">
          <Translate contentKey="global.menu.entities.attendance" />
        </MenuItem>
      )}
      {adminOrInstructor && (
        <MenuItem icon="clipboard-check" to="/justification-details">
          <Translate contentKey="global.menu.entities.justificationDetails" />
        </MenuItem>
      )}
      {/* ══════════════════════════════════════════
          ADMIN + APRENDIZ — Justificaciones
      ══════════════════════════════════════════ */}
      {adminOrApprentice && (
        <MenuItem icon="file" to="/justification">
          <Translate contentKey="global.menu.entities.justification" />
        </MenuItem>
      )}
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;

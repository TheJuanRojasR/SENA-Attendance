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
  return (
    <>
      {/* prettier-ignore */}
      {/* ══════════════════════════════════════════
          TODOS — Notificaciones
      ══════════════════════════════════════════ */}
      <MenuItem icon="bell" to="/notification">
        <Translate contentKey="global.menu.entities.notification" />
      </MenuItem>
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
      {/* ══════════════════════════════════════════
          ADMIN + COORDINADOR — Estructura académica
      ══════════════════════════════════════════ */}
      {adminOrCoordinator && (
        <MenuItem icon="cogs" to="/global-configuration">
          <Translate contentKey="global.menu.entities.globalConfiguration" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="cloud-sun" to="/time-slot">
          <Translate contentKey="global.menu.entities.timeSlot" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="layer-group" to="/modality">
          <Translate contentKey="global.menu.entities.modality" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="graduation-cap" to="/program">
          <Translate contentKey="global.menu.entities.program" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="calendar-alt" to="/trimester">
          <Translate contentKey="global.menu.entities.trimester" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="tag" to="/justification-type">
          <Translate contentKey="global.menu.entities.justificationType" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="th-list" to="/grade">
          <Translate contentKey="global.menu.entities.grade" />
        </MenuItem>
      )}
      {isAdmin && (
        <MenuItem icon="book-open" to="/class-section">
          <Translate contentKey="global.menu.entities.classSection" />
        </MenuItem>
      )}
      {adminOrInstructor && (
        <MenuItem icon="user-graduate" to="/apprentice">
          <Translate contentKey="global.menu.entities.apprentice" />
        </MenuItem>
      )}
      {adminOrInstructor && (
        <MenuItem icon="clock" to="/class-schedule">
          <Translate contentKey="global.menu.entities.classSchedule" />
        </MenuItem>
      )}
      {adminOrInstructor && (
        <MenuItem icon="ban" to="/class-exception">
          <Translate contentKey="global.menu.entities.classException" />
        </MenuItem>
      )}
      {/* ══════════════════════════════════════════
          ADMIN + INSTRUCTOR — Toma de asistencia
      ══════════════════════════════════════════ */}
      {isInstructor && (
        <MenuItem icon="th-large" to="/class-section/mine">
          Mis fichas
        </MenuItem>
      )}
      {isInstructor && (
        <MenuItem icon="clipboard-list" to="/attendance/session">
          Tomar asistencia
        </MenuItem>
      )}
      {(adminOrInstructor || isApprentice) && (
        <MenuItem icon="tasks" to="/attendance">
          <Translate contentKey="global.menu.entities.attendance" />
        </MenuItem>
      )}
      {adminOrInstructor && (
        <MenuItem icon="clipboard-check" to="/justification-details">
          <Translate contentKey="global.menu.entities.justificationDetails" />
        </MenuItem>
      )}
      {adminOrInstructor && (
        <MenuItem icon="triangle-exclamation" to="/alert">
          <Translate contentKey="global.menu.entities.alert" />
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

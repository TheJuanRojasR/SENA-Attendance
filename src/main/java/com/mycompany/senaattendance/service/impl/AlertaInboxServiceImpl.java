package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.repository.AlertaReadScope;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.AlertaSearchCriteria;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.AlertaInboxService;
import com.mycompany.senaattendance.service.dto.AlertaDTO;
import com.mycompany.senaattendance.service.mapper.AlertaMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for the alert inbox (UC013, A1/A2/A3/A5).
 *
 * <p>The readable scope is resolved from the security context: an administrator reads every alert,
 * an instructor reads the consecutive alerts of their materias and the accumulated alerts of their
 * fichas, and a user without assigned materias reads nothing. Reading and attending one alert
 * filter by that scope, so a foreign alert resolves as absent.
 */
@Service
public class AlertaInboxServiceImpl implements AlertaInboxService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaInboxServiceImpl.class);

    private static final String ENTITY_NAME = "alerta";

    private final AlertaRepository alertaRepository;

    private final ClassSectionRepository classSectionRepository;

    private final CurrentUserContext currentUserContext;

    private final AlertaMapper alertaMapper;

    public AlertaInboxServiceImpl(
        AlertaRepository alertaRepository,
        ClassSectionRepository classSectionRepository,
        CurrentUserContext currentUserContext,
        AlertaMapper alertaMapper
    ) {
        this.alertaRepository = alertaRepository;
        this.classSectionRepository = classSectionRepository;
        this.currentUserContext = currentUserContext;
        this.alertaMapper = alertaMapper;
    }

    @Override
    public Page<AlertaDTO> findAllForCurrentUser(
        AlertaType type,
        AlertaState state,
        String gradeId,
        String studentId,
        Instant from,
        Instant to,
        Pageable pageable
    ) {
        LOG.debug("Request to get the page of Alerts the current user can read");
        AlertaSearchCriteria criteria = new AlertaSearchCriteria(type, state, gradeId, studentId, from, to);
        return alertaRepository.searchAlerts(criteria, readScopeOfCurrentUser(), pageable).map(alertaMapper::toDto);
    }

    @Override
    public Page<AlertaDTO> findByStudentForCurrentUser(String studentId, Pageable pageable) {
        LOG.debug("Request to get the page of Alerts of apprentice {} the current user can read", studentId);
        AlertaSearchCriteria criteria = new AlertaSearchCriteria(null, null, null, studentId, null, null);
        return alertaRepository.searchAlerts(criteria, readScopeOfCurrentUser(), pageable).map(alertaMapper::toDto);
    }

    @Override
    public Optional<AlertaDTO> findOneForCurrentUser(String id) {
        LOG.debug("Request to get Alert : {}", id);
        return alertaRepository.findById(id).filter(this::isReadableByCurrentUser).map(alertaMapper::toDto);
    }

    @Override
    public Optional<AlertaDTO> markAsRead(String id) {
        LOG.debug("Request to mark Alert {} as read", id);
        return alertaRepository
            .findById(id)
            .filter(this::isReadableByCurrentUser)
            .map(alerta -> {
                if (alerta.getState() == AlertaState.NO_LEIDA) {
                    alerta.setState(AlertaState.LEIDA);
                    alertaRepository.save(alerta);
                }
                return alertaMapper.toDto(alerta);
            });
    }

    @Override
    public Optional<AlertaDTO> attend(String id, String observation) {
        LOG.debug("Request to mark Alert {} as attended", id);
        return alertaRepository
            .findById(id)
            .filter(this::isReadableByCurrentUser)
            .map(alerta -> {
                if (alerta.getState() == AlertaState.RESUELTA_AUTOMATICAMENTE) {
                    throw new BadRequestAlertException(
                        "Una alerta resuelta automáticamente no se puede atender",
                        ENTITY_NAME,
                        "alertAlreadyResolved"
                    );
                }
                alerta.setState(AlertaState.ATENDIDA);
                alerta.setObservation(observation);
                alertaRepository.save(alerta);
                return alertaMapper.toDto(alerta);
            });
    }

    /**
     * @return the scope of the current user: unrestricted for an administrator, and the materias
     *         and fichas of the instructor for everyone else. A user without a resolvable profile
     *         or without assigned materias reads nothing.
     */
    private AlertaReadScope readScopeOfCurrentUser() {
        if (isCurrentUserAdmin()) {
            return AlertaReadScope.unrestricted();
        }
        String currentProfileId = currentUserProfileId();
        if (currentProfileId == null) {
            return new AlertaReadScope(Set.of(), Set.of());
        }
        List<ClassSection> classSections = classSectionRepository.findByInstructorId(currentProfileId);
        Set<String> classSectionIds = classSections.stream().map(ClassSection::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> gradeIds = classSections
            .stream()
            .map(ClassSection::getGrade)
            .filter(Objects::nonNull)
            .map(Grade::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return new AlertaReadScope(classSectionIds, gradeIds);
    }

    /**
     * Resolves whether the current user can read the alert with the same scope of the inbox
     * query: an administrator reads everything, and an instructor reads the consecutive alerts of
     * their materias and the accumulated alerts of their fichas.
     *
     * @param alerta the alert to read.
     * @return whether the alert is inside the readable scope of the current user.
     */
    private boolean isReadableByCurrentUser(Alerta alerta) {
        return readScopeOfCurrentUser().contains(
            alerta.getType(),
            alerta.getClassSection() == null ? null : alerta.getClassSection().getId(),
            alerta.getGrade() == null ? null : alerta.getGrade().getId()
        );
    }

    /**
     * @return whether the current user is an administrator, who reads every alert.
     */
    private static boolean isCurrentUserAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    /**
     * @return the profile id of the authenticated user, or {@code null} when the session has no
     *         login or the account has no profile.
     */
    private String currentUserProfileId() {
        return currentUserContext.profileId();
    }
}

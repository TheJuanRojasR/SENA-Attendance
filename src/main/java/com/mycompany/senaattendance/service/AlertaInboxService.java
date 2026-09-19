package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.service.dto.AlertaDTO;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for the alert inbox of the authenticated user (UC013, A1/A2/A3/A5). Every
 * read is scoped: an administrator reaches every alert, while an instructor only reaches the
 * consecutive alerts of their materias and the accumulated alerts of their fichas. A foreign
 * alert resolves as absent, so the caller answers {@code 404} without leaking its existence.
 */
public interface AlertaInboxService {
    /**
     * Reads the alerts the current user can see, newest first by default, with the optional
     * filters combined into one query (A1).
     *
     * @param type the alert type to filter by (optional).
     * @param state the alert state to filter by (optional).
     * @param gradeId the ficha to filter by (optional).
     * @param studentId the apprentice profile to filter by (optional).
     * @param from the first generation instant to include (optional).
     * @param to the last generation instant to include (optional).
     * @param pageable the pagination information.
     * @return the page of readable alerts matching the filters.
     */
    Page<AlertaDTO> findAllForCurrentUser(
        AlertaType type,
        AlertaState state,
        String gradeId,
        String studentId,
        Instant from,
        Instant to,
        Pageable pageable
    );

    /**
     * Reads the alert history of one apprentice (A5), restricted to the readable scope of the
     * current user.
     *
     * @param studentId the apprentice profile id.
     * @param pageable the pagination information.
     * @return the page of readable alerts of that apprentice.
     */
    Page<AlertaDTO> findByStudentForCurrentUser(String studentId, Pageable pageable);

    /**
     * Reads one alert the current user can see.
     *
     * @param id the alert id.
     * @return the readable alert, or empty when it does not exist or is out of scope.
     */
    Optional<AlertaDTO> findOneForCurrentUser(String id);

    /**
     * Marks one readable alert as read (A2). The transition only applies from unread, so calling
     * it again keeps the alert in the state it already reached.
     *
     * @param id the alert id.
     * @return the updated alert, or empty when it does not exist or is out of scope.
     */
    Optional<AlertaDTO> markAsRead(String id);

    /**
     * Marks one readable alert as attended with the follow-up observation (A3). A resolved alert
     * is history and cannot be attended.
     *
     * @param id the alert id.
     * @param observation the follow-up note of the instructor.
     * @return the updated alert, or empty when it does not exist or is out of scope.
     */
    Optional<AlertaDTO> attend(String id, String observation);
}

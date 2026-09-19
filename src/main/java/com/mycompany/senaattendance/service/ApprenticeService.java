package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.web.rest.vm.EnrollApprenticeVM;
import com.mycompany.senaattendance.web.rest.vm.UnlinkApprenticeVM;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Apprentice}.
 */
public interface ApprenticeService {
    /**
     * Enrolls the apprentice identified by the document number in the requested ficha, with the
     * academic state fixed by the server as Matriculado (UC008).
     *
     * @param enrollApprenticeVM the document number of the apprentice and the target ficha.
     * @return the persisted enrollment.
     */
    ApprenticeDTO enroll(EnrollApprenticeVM enrollApprenticeVM);

    /**
     * Unlinks the enrollment from its ficha with the chosen reason (UC008, A1). When the
     * apprentice has no attendance in the ficha the record is deleted; when it has attendance the
     * record is kept and its academic state becomes the reason.
     *
     * @param unlinkApprenticeVM the enrollment id and the withdrawal reason.
     * @return the updated enrollment when the record is kept, or empty when it was deleted.
     */
    Optional<ApprenticeDTO> unlink(UnlinkApprenticeVM unlinkApprenticeVM);

    /**
     * Get the apprentices filtered by any combination of ficha, document number, name and
     * academic state (UC008, A2). Every filter is optional: a filter that is not given does not
     * restrict the result, and a text filter that matches no profile returns an empty page.
     *
     * @param gradeId the ficha id to filter by (optional).
     * @param documentNumber the document number fragment to filter by (optional).
     * @param name the first name or first last name fragment to filter by (optional).
     * @param stateAcademic the academic state to filter by (optional).
     * @param pageable the pagination information.
     * @return the page of matching apprentices.
     */
    Page<ApprenticeDTO> findAll(String gradeId, String documentNumber, String name, StateAcademic stateAcademic, Pageable pageable);

    /**
     * Get the apprentices filtered by ficha, document number, name and academic state, with
     * eager load of many-to-many relationships.
     *
     * @param gradeId the ficha id to filter by (optional).
     * @param documentNumber the document number fragment to filter by (optional).
     * @param name the first name or first last name fragment to filter by (optional).
     * @param stateAcademic the academic state to filter by (optional).
     * @param pageable the pagination information.
     * @return the page of matching apprentices.
     */
    Page<ApprenticeDTO> findAllWithEagerRelationships(
        String gradeId,
        String documentNumber,
        String name,
        StateAcademic stateAcademic,
        Pageable pageable
    );

    /**
     * Get the "id" apprentice.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ApprenticeDTO> findOne(String id);
}

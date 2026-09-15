package com.mycompany.senaattendance.service;

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
     * Get all the apprentices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ApprenticeDTO> findAll(Pageable pageable);

    /**
     * Get all the apprentices with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ApprenticeDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" apprentice.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ApprenticeDTO> findOne(String id);
}

package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.web.rest.vm.EnrollApprenticeVM;
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

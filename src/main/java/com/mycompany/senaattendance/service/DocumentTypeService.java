package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.DocumentTypeDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.DocumentType}.
 */
public interface DocumentTypeService {
    /**
     * Save a documentType.
     *
     * @param documentTypeDTO the entity to save.
     * @return the persisted entity.
     */
    DocumentTypeDTO save(DocumentTypeDTO documentTypeDTO);

    /**
     * Updates a documentType.
     *
     * @param documentTypeDTO the entity to update.
     * @return the persisted entity.
     */
    DocumentTypeDTO update(DocumentTypeDTO documentTypeDTO);

    /**
     * Partially updates a documentType.
     *
     * @param documentTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DocumentTypeDTO> partialUpdate(DocumentTypeDTO documentTypeDTO);

    /**
     * Get all the documentTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DocumentTypeDTO> findAll(Pageable pageable);

    /**
     * Get all the active documentTypes.
     *
     * @return the list of active entities.
     */
    List<DocumentTypeDTO> findActiveDocumentTypes();

    /**
     * Get the "id" documentType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DocumentTypeDTO> findOne(String id);

    /**
     * Delete the "id" documentType.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}

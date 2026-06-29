package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.DocumentTypeService;
import com.mycompany.senaattendance.service.dto.DocumentTypeDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.DocumentType}.
 */
@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeResource {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentTypeResource.class);

    private static final String ENTITY_NAME = "documentType";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final DocumentTypeService documentTypeService;

    private final DocumentTypeRepository documentTypeRepository;

    public DocumentTypeResource(DocumentTypeService documentTypeService, DocumentTypeRepository documentTypeRepository) {
        this.documentTypeService = documentTypeService;
        this.documentTypeRepository = documentTypeRepository;
    }

    /**
     * {@code POST  /document-types} : Create a new documentType.
     *
     * @param documentTypeDTO the documentTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new documentTypeDTO, or with status {@code 400 (Bad Request)} if the documentType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<DocumentTypeDTO> createDocumentType(@Valid @RequestBody DocumentTypeDTO documentTypeDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DocumentType : {}", documentTypeDTO);
        if (documentTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new documentType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        documentTypeDTO = documentTypeService.save(documentTypeDTO);
        return ResponseEntity.created(new URI("/api/document-types/" + documentTypeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, documentTypeDTO.getId()))
            .body(documentTypeDTO);
    }

    /**
     * {@code PUT  /document-types/:id} : Updates an existing documentType.
     *
     * @param id the id of the documentTypeDTO to save.
     * @param documentTypeDTO the documentTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentTypeDTO,
     * or with status {@code 400 (Bad Request)} if the documentTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the documentTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<DocumentTypeDTO> updateDocumentType(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody DocumentTypeDTO documentTypeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DocumentType : {}, {}", id, documentTypeDTO);
        if (documentTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        documentTypeDTO = documentTypeService.update(documentTypeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, documentTypeDTO.getId()))
            .body(documentTypeDTO);
    }

    /**
     * {@code PATCH  /document-types/:id} : Partial updates given fields of an existing documentType, field will ignore if it is null
     *
     * @param id the id of the documentTypeDTO to save.
     * @param documentTypeDTO the documentTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentTypeDTO,
     * or with status {@code 400 (Bad Request)} if the documentTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the documentTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the documentTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<DocumentTypeDTO> partialUpdateDocumentType(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody DocumentTypeDTO documentTypeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DocumentType partially : {}, {}", id, documentTypeDTO);
        if (documentTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DocumentTypeDTO> result = documentTypeService.partialUpdate(documentTypeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, documentTypeDTO.getId())
        );
    }

    /**
     * {@code GET  /document-types} : get all the Document Types.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Document Types in body.
     */
    @GetMapping("")
    public List<DocumentTypeDTO> getAllDocumentTypes() {
        LOG.debug("REST request to get all DocumentTypes");
        return documentTypeService.findAll();
    }

    /**
     * {@code GET  /document-types/:id} : get the "id" documentType.
     *
     * @param id the id of the documentTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the documentTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentTypeDTO> getDocumentType(@PathVariable("id") String id) {
        LOG.debug("REST request to get DocumentType : {}", id);
        Optional<DocumentTypeDTO> documentTypeDTO = documentTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(documentTypeDTO);
    }

    /**
     * {@code DELETE  /document-types/:id} : delete the "id" documentType.
     *
     * @param id the id of the documentTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<Void> deleteDocumentType(@PathVariable("id") String id) {
        LOG.debug("REST request to delete DocumentType : {}", id);
        documentTypeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}

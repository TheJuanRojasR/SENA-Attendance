package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ApprenticeService;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.web.rest.vm.EnrollApprenticeVM;
import com.mycompany.senaattendance.web.rest.vm.UnlinkApprenticeVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Apprentice}.
 */
@RestController
@RequestMapping("/api/apprentices")
public class ApprenticeResource {

    private static final Logger LOG = LoggerFactory.getLogger(ApprenticeResource.class);

    private static final String ENTITY_NAME = "apprentice";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final ApprenticeService apprenticeService;

    public ApprenticeResource(ApprenticeService apprenticeService) {
        this.apprenticeService = apprenticeService;
    }

    /**
     * {@code POST  /apprentices} : enrols the apprentice identified by its document number in the
     * requested ficha (UC008).
     *
     * @param enrollApprenticeVM the document number of the apprentice and the target ficha.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and the created
     *         enrollment, or {@code 400 (Bad Request)} when the apprentice does not exist or is
     *         inactive, already has a record in the ficha, or the ficha is not operable.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ApprenticeDTO> enrollApprentice(@Valid @RequestBody EnrollApprenticeVM enrollApprenticeVM)
        throws URISyntaxException {
        LOG.debug("REST request to enroll Apprentice by document number : {}", enrollApprenticeVM.getDocumentNumber());
        ApprenticeDTO apprenticeDTO = apprenticeService.enroll(enrollApprenticeVM);
        return ResponseEntity.created(new URI("/api/apprentices/" + apprenticeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, apprenticeDTO.getId()))
            .body(apprenticeDTO);
    }

    /**
     * {@code PATCH  /apprentices/unlinked} : unlinks the enrollment from its ficha with the chosen
     * reason (UC008, A1). Only the administrator can unlink. The id is taken from the request body.
     *
     * @param unlinkApprenticeVM the enrollment id and the withdrawal reason.
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)} when the record was
     *         deleted, or {@code 200 (OK)} with the updated enrollment when its attendance history
     *         forced the record to be kept.
     */
    @PatchMapping("/unlinked")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ApprenticeDTO> unlinkApprentice(@Valid @RequestBody UnlinkApprenticeVM unlinkApprenticeVM) {
        LOG.debug("REST request to unlink Apprentice : {} with reason {}", unlinkApprenticeVM.getId(), unlinkApprenticeVM.getReason());
        return apprenticeService
            .unlink(unlinkApprenticeVM)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * {@code GET  /apprentices} : get all the Apprentices, optionally filtered by ficha,
     * document number, name and academic state (UC008, A2). Every filter is optional and the
     * result is paginated.
     *
     * @param gradeId the ficha id to filter by (optional).
     * @param documentNumber the document number fragment to filter by (optional).
     * @param name the first name or first last name fragment to filter by (optional).
     * @param stateAcademic the academic state to filter by (optional).
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Apprentices in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<ApprenticeDTO>> getAllApprentices(
        @RequestParam(name = "gradeId", required = false) String gradeId,
        @RequestParam(name = "documentNumber", required = false) String documentNumber,
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "stateAcademic", required = false) StateAcademic stateAcademic,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Apprentices");
        Page<ApprenticeDTO> page;
        if (eagerload) {
            page = apprenticeService.findAllWithEagerRelationships(gradeId, documentNumber, name, stateAcademic, pageable);
        } else {
            page = apprenticeService.findAll(gradeId, documentNumber, name, stateAcademic, pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /apprentices/:id} : get the "id" apprentice.
     *
     * @param id the id of the apprenticeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the apprenticeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<ApprenticeDTO> getApprentice(@PathVariable("id") String id) {
        LOG.debug("REST request to get Apprentice : {}", id);
        Optional<ApprenticeDTO> apprenticeDTO = apprenticeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(apprenticeDTO);
    }
}

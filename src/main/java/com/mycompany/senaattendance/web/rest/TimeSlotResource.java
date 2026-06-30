package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.TimeSlotService;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.TimeSlot}.
 */
@RestController
@RequestMapping("/api/time-slots")
public class TimeSlotResource {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSlotResource.class);

    private static final String ENTITY_NAME = "timeSlot";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final TimeSlotService timeSlotService;

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotResource(TimeSlotService timeSlotService, TimeSlotRepository timeSlotRepository) {
        this.timeSlotService = timeSlotService;
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * {@code POST  /time-slots} : Create a new timeSlot.
     *
     * @param timeSlotDTO the timeSlotDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new timeSlotDTO, or with status {@code 400 (Bad Request)} if the timeSlot has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<TimeSlotDTO> createTimeSlot(@Valid @RequestBody TimeSlotDTO timeSlotDTO) throws URISyntaxException {
        LOG.debug("REST request to save TimeSlot : {}", timeSlotDTO);
        if (timeSlotDTO.getId() != null) {
            throw new BadRequestAlertException("A new timeSlot cannot already have an ID", ENTITY_NAME, "idexists");
        }
        timeSlotDTO = timeSlotService.save(timeSlotDTO);
        return ResponseEntity.created(new URI("/api/time-slots/" + timeSlotDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, timeSlotDTO.getId()))
            .body(timeSlotDTO);
    }

    /**
     * {@code PUT  /time-slots/:id} : Updates an existing timeSlot.
     *
     * @param id the id of the timeSlotDTO to save.
     * @param timeSlotDTO the timeSlotDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated timeSlotDTO,
     * or with status {@code 400 (Bad Request)} if the timeSlotDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the timeSlotDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<TimeSlotDTO> updateTimeSlot(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody TimeSlotDTO timeSlotDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TimeSlot : {}, {}", id, timeSlotDTO);
        if (timeSlotDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, timeSlotDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!timeSlotRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        timeSlotDTO = timeSlotService.update(timeSlotDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, timeSlotDTO.getId()))
            .body(timeSlotDTO);
    }

    /**
     * {@code PATCH  /time-slots/:id} : Partial updates given fields of an existing timeSlot, field will ignore if it is null
     *
     * @param id the id of the timeSlotDTO to save.
     * @param timeSlotDTO the timeSlotDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated timeSlotDTO,
     * or with status {@code 400 (Bad Request)} if the timeSlotDTO is not valid,
     * or with status {@code 404 (Not Found)} if the timeSlotDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the timeSlotDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<TimeSlotDTO> partialUpdateTimeSlot(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody TimeSlotDTO timeSlotDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TimeSlot partially : {}, {}", id, timeSlotDTO);
        if (timeSlotDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, timeSlotDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!timeSlotRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TimeSlotDTO> result = timeSlotService.partialUpdate(timeSlotDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, timeSlotDTO.getId())
        );
    }

    /**
     * {@code GET  /time-slots} : get all the Time Slots.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Time Slots in body.
     */
    @GetMapping("")
    public List<TimeSlotDTO> getAllTimeSlots() {
        LOG.debug("REST request to get all TimeSlots");
        return timeSlotService.findAll();
    }

    /**
     * {@code GET  /time-slots/:id} : get the "id" timeSlot.
     *
     * @param id the id of the timeSlotDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the timeSlotDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotDTO> getTimeSlot(@PathVariable("id") String id) {
        LOG.debug("REST request to get TimeSlot : {}", id);
        Optional<TimeSlotDTO> timeSlotDTO = timeSlotService.findOne(id);
        return ResponseUtil.wrapOrNotFound(timeSlotDTO);
    }

    /**
     * {@code DELETE  /time-slots/:id} : delete the "id" timeSlot.
     *
     * @param id the id of the timeSlotDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable("id") String id) {
        LOG.debug("REST request to delete TimeSlot : {}", id);
        timeSlotService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }

    @GetMapping("/active")
    public List<TimeSlotDTO> getActiveTimeSlots() {
        LOG.debug("REST request to get all active TimeSlots");
        return timeSlotService.findByIsActiveTrue();
    }
}

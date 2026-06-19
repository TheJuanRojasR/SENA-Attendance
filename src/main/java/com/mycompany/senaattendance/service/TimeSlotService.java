package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.TimeSlot}.
 */
public interface TimeSlotService {
    /**
     * Save a timeSlot.
     *
     * @param timeSlotDTO the entity to save.
     * @return the persisted entity.
     */
    TimeSlotDTO save(TimeSlotDTO timeSlotDTO);

    /**
     * Updates a timeSlot.
     *
     * @param timeSlotDTO the entity to update.
     * @return the persisted entity.
     */
    TimeSlotDTO update(TimeSlotDTO timeSlotDTO);

    /**
     * Partially updates a timeSlot.
     *
     * @param timeSlotDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TimeSlotDTO> partialUpdate(TimeSlotDTO timeSlotDTO);

    /**
     * Get all the timeSlots.
     *
     * @return the list of entities.
     */
    List<TimeSlotDTO> findAll();

    /**
     * Get the "id" timeSlot.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TimeSlotDTO> findOne(String id);

    /**
     * Delete the "id" timeSlot.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}

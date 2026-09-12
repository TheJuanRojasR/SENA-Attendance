package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.TimeSlot;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the TimeSlot entity.
 */
@Repository
public interface TimeSlotRepository extends MongoRepository<TimeSlot, String> {
    List<TimeSlot> findTimeSlotByIsActive(Boolean isActive);

    /**
     * Returns whether a time slot with the given name already exists (case-insensitive).
     *
     * @param name the name to check.
     * @return {@code true} if a time slot with this name exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Returns whether a time slot with the given name exists, excluding a specific id.
     * Used so an update that keeps the same name does not collide with itself.
     *
     * @param name the name to check.
     * @param id the id to exclude.
     * @return {@code true} if another time slot with this name exists.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
}

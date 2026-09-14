package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.enumeration.Status;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the JustificationType entity.
 */
@Repository
public interface JustificationTypeRepository extends MongoRepository<JustificationType, String> {
    /**
     * Returns the justification types with the given status.
     *
     * @param status the status to filter by.
     * @return the matching justification types.
     */
    List<JustificationType> findJustificationTypeByStatus(Status status);

    /**
     * Returns whether a justification type with the given name already exists (case-insensitive).
     *
     * @param name the name to check.
     * @return {@code true} if a justification type with this name exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Returns whether a justification type with the given name exists, excluding a specific id.
     * Used so an update that keeps the same name does not collide with itself.
     *
     * @param name the name to check.
     * @param id the id to exclude.
     * @return {@code true} if another justification type with this name exists.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
}

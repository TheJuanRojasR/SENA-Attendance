package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Modality;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Modality entity.
 */
@Repository
public interface ModalityRepository extends MongoRepository<Modality, String> {
    List<Modality> findModalityByIsActive(Boolean isActive);

    /**
     * Returns whether a modality with the given name already exists (case-insensitive).
     *
     * @param name the name to check.
     * @return {@code true} if a modality with this name exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Returns whether a modality with the given name exists, excluding a specific id.
     * Used so an update that keeps the same name does not collide with itself.
     *
     * @param name the name to check.
     * @param id the id to exclude.
     * @return {@code true} if another modality with this name exists.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
}

package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.DocumentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the DocumentType entity.
 */
@Repository
public interface DocumentTypeRepository extends MongoRepository<DocumentType, String> {
    /**
     * Returns whether a document type with the given name already exists (case-insensitive).
     *
     * @param name the name to check.
     * @return {@code true} if a document type with this name exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Returns whether a document type with the given name exists, excluding a specific id.
     * Used so an update that keeps the same name does not collide with itself.
     *
     * @param name the name to check.
     * @param id the id to exclude.
     * @return {@code true} if another document type with this name exists.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    /**
     * Returns whether a document type with the given initials already exists (case-insensitive).
     *
     * @param initials the initials to check.
     * @return {@code true} if a document type with these initials exists.
     */
    boolean existsByInitialsIgnoreCase(String initials);

    /**
     * Returns whether a document type with the given initials exists, excluding a specific id.
     * Used so an update that keeps the same initials does not collide with itself.
     *
     * @param initials the initials to check.
     * @param id the id to exclude.
     * @return {@code true} if another document type with these initials exists.
     */
    boolean existsByInitialsIgnoreCaseAndIdNot(String initials, String id);
}

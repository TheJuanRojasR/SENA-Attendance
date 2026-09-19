package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.JustificationDetails;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the JustificationDetails entity.
 */
@Repository
public interface JustificationDetailsRepository
    extends MongoRepository<JustificationDetails, String>, JustificationDetailsRepositoryCustom
{
    @Query("{}")
    Page<JustificationDetails> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<JustificationDetails> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<JustificationDetails> findOneWithEagerRelationships(String id);

    /**
     * Finds the parts that belong to the given justifications (UC011), used to scope the list to
     * the current apprentice. The justification is a DBRef, and the {@code $in} operator compares
     * DBRef ids directly, so the values must be explicit {@link ObjectId} instances.
     *
     * @param justificationIds the ObjectId values of the justifications to include.
     * @param pageable the pagination information.
     * @return the page of parts of those justifications.
     */
    @Query("{ 'justification.$id': { $in: ?0 } }")
    Page<JustificationDetails> findByJustificationIdIn(List<ObjectId> justificationIds, Pageable pageable);

    /**
     * Finds every part of the given justifications (UC011), used to derive the failure dates a
     * justification covers. The justification is a DBRef, and the {@code $in} operator compares
     * DBRef ids directly, so the values must be explicit {@link ObjectId} instances.
     *
     * @param justificationIds the ObjectId values of the justifications to include.
     * @return the parts of those justifications, possibly empty.
     */
    @Query("{ 'justification.$id': { $in: ?0 } }")
    List<JustificationDetails> findAllByJustificationIdIn(List<ObjectId> justificationIds);

    /**
     * Returns whether any justification part references the given class section. The class
     * section is a DBRef, so it is matched through the scalar {@code classSection._id}, where a
     * String resolves to the referenced id. Used as a defense-in-depth guard before deleting a
     * materia: a part would otherwise stay orphaned.
     *
     * @param classSectionId the class section id to check.
     * @return {@code true} when at least one part references the class section.
     */
    @Query(value = "{ 'classSection._id': ?0 }", exists = true)
    boolean existsByClassSectionId(String classSectionId);
}

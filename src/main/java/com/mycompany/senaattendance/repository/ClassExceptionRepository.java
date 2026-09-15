package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.ClassException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the ClassException entity.
 */
@Repository
public interface ClassExceptionRepository extends MongoRepository<ClassException, String> {
    @Query("{}")
    Page<ClassException> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<ClassException> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<ClassException> findOneWithEagerRelationships(String id);

    /**
     * Finds the exceptions that reference the given class section. Used to cascade the
     * deletion of a ficha onto the exceptions of its class sections.
     *
     * @param classSectionId the class section id to match against the {@code classSection} DBRef.
     * @return the exceptions whose {@code classSection} reference matches {@code classSectionId}.
     */
    // ------- SEARCH CLASS EXCEPTIONS BY CLASS SECTION ID -------
    @Query("{ 'classSection._id': ?0 }")
    List<ClassException> findByClassSectionId(String classSectionId);

    /**
     * Returns whether the class section has a non-teaching exception on the given date. The class
     * section is matched through the scalar {@code classSection._id}, where a String resolves to
     * the referenced id.
     *
     * @param classSectionId the class section id.
     * @param date the date to check.
     * @return {@code true} when that date is a non-teaching exception of the class section.
     */
    @Query(value = "{ 'classSection._id': ?0, 'date': ?1 }", exists = true)
    boolean existsByClassSectionIdAndDate(String classSectionId, LocalDate date);

    /**
     * Finds the exceptions of the given class sections. Used to read only the exceptions of the
     * materias assigned to the current instructor. The class sections are matched through the
     * DBRef id ({@code $id}), which needs explicit ObjectIds, unlike the scalar {@code _id}
     * lookups where a String resolves to the referenced id.
     *
     * @param classSectionIds the ObjectId values of the class sections.
     * @param pageable the pagination information.
     * @return the page of exceptions of those class sections.
     */
    @Query("{ 'classSection.$id': { $in: ?0 } }")
    Page<ClassException> findByClassSectionIdIn(List<ObjectId> classSectionIds, Pageable pageable);
}

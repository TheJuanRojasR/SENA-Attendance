package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.ClassException;
import java.util.List;
import java.util.Optional;
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
}

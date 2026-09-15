package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Justification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Justification entity.
 */
@Repository
public interface JustificationRepository extends MongoRepository<Justification, String> {
    @Query("{}")
    Page<Justification> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Justification> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Justification> findOneWithEagerRelationships(String id);

    /**
     * Returns whether any justification references the given justification type.
     * Used to block deleting a justification type that is still in use.
     *
     * @param justificationTypeId the justification type id to check.
     * @return {@code true} if at least one justification references this type.
     */
    boolean existsByJustificationTypeId(String justificationTypeId);

    /**
     * Finds the justifications of one apprentice (UC011), used to scope the list to the current
     * user. The student is a DBRef, so it is matched through its referenced id, where a String
     * resolves to the profile id.
     *
     * @param studentId the apprentice profile id.
     * @param pageable the pagination information.
     * @return the page of justifications of that apprentice.
     */
    @Query("{ 'student._id': ?0 }")
    Page<Justification> findByStudentId(String studentId, Pageable pageable);

    /**
     * Finds every justification of one apprentice (UC011), used to scope the parts to the
     * justifications of the current user.
     *
     * @param studentId the apprentice profile id.
     * @return the justifications of that apprentice, possibly empty.
     */
    @Query("{ 'student._id': ?0 }")
    List<Justification> findAllByStudentId(String studentId);
}

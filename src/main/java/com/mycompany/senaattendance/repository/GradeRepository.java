package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Grade entity.
 */
@Repository
public interface GradeRepository extends MongoRepository<Grade, String> {
    @Query("{}")
    Page<Grade> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Grade> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Grade> findOneWithEagerRelationships(String id);

    // ------- SEARCH GRADES BY STATE -------
    long countByState(StateGrade state);

    // ------- SEARCH LATEST 5 GRADES -------
    List<Grade> findTop5ByOrderByCreatedDateDesc();

    // ------- COUNT ACTIVE GRADES BY PROGRAM -------
    long countByProgram_IdAndState(String programId, StateGrade state);

    /**
     * Returns whether any ficha (grade) references the given time slot.
     * Used to block deleting a time slot that is still in use.
     *
     * @param timeSlotId the time slot id to check.
     * @return {@code true} if at least one grade references this time slot.
     */
    boolean existsByTimeSlotId(String timeSlotId);

    /**
     * Returns whether any ficha (grade) references the given modality.
     * Used to block deleting a modality that is still in use.
     *
     * @param modalityId the modality id to check.
     * @return {@code true} if at least one grade references this modality.
     */
    boolean existsByModalityId(String modalityId);

    /**
     * Returns whether any ficha (grade) references the given program.
     * Used to block deleting a program that still has fichas in any state.
     *
     * @param programId the program id to check.
     * @return {@code true} if at least one grade references this program.
     */
    boolean existsByProgramId(String programId);

    /**
     * Returns whether a ficha with the given code already exists. Used to keep
     * the ficha code unique.
     *
     * @param code the code to check.
     * @return {@code true} if a ficha with this code exists.
     */
    boolean existsByCode(String code);

    /**
     * Returns whether a ficha with the given code exists, excluding a specific id.
     * Used so an update that keeps the same code does not collide with itself.
     *
     * @param code the code to check.
     * @param id the id to exclude.
     * @return {@code true} if another ficha with this code exists.
     */
    boolean existsByCodeAndIdNot(String code, String id);
}

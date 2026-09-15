package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Justification;
import java.time.Instant;
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

    /**
     * Finds the justifications of one apprentice for one type (UC011), used to count the days
     * already covered by the type quota. Both references are DBRefs matched by their referenced
     * id, where a String resolves to the profile/type id.
     *
     * @param justificationTypeId the justification type id.
     * @param studentId the apprentice profile id.
     * @return the justifications of that apprentice with that type, possibly empty.
     */
    @Query("{ 'justificationType._id': ?0, 'student._id': ?1 }")
    List<Justification> findByJustificationTypeIdAndStudentId(String justificationTypeId, String studentId);

    /**
     * Finds the justifications requested from an instant on (UC010, A1), used to keep only the
     * parts whose request date falls inside the requested range.
     *
     * @param from the inclusive lower bound of the request date.
     * @return the justifications requested from that instant on, possibly empty.
     */
    List<Justification> findByCreatedDateGreaterThanEqual(Instant from);

    /**
     * Finds the justifications requested before an instant (UC010, A1), used to keep only the
     * parts whose request date falls inside the requested range.
     *
     * @param to the exclusive upper bound of the request date.
     * @return the justifications requested before that instant, possibly empty.
     */
    List<Justification> findByCreatedDateBefore(Instant to);

    /**
     * Finds the justifications requested inside a half-open range (UC010, A1), used to keep only
     * the parts whose request date falls inside the requested range.
     *
     * @param from the inclusive lower bound of the request date.
     * @param to the exclusive upper bound of the request date.
     * @return the justifications requested inside the range, possibly empty.
     */
    List<Justification> findByCreatedDateGreaterThanEqualAndCreatedDateBefore(Instant from, Instant to);
}

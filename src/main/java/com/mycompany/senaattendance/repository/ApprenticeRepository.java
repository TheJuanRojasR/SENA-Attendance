package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Apprentice entity.
 */
@Repository
public interface ApprenticeRepository extends MongoRepository<Apprentice, String> {
    @Query("{}")
    Page<Apprentice> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Apprentice> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Apprentice> findOneWithEagerRelationships(String id);

    // ------- SEARCH BY GRADE ID -------
    @Query("{'grade._id': ?0}")
    List<Apprentice> findByGradeId(String gradeId);

    /**
     * Finds every enrollment of one apprentice. Used by the apprentice dashboard to resolve the
     * fichas and materias of the current user (UC023). The student is a DBRef, so it is matched
     * through its referenced id, where a String resolves to the profile id.
     *
     * @param studentId the apprentice profile id.
     * @return the enrollments of that apprentice, possibly empty.
     */
    @Query("{'student._id': ?0}")
    List<Apprentice> findByStudentId(String studentId);

    /**
     * Returns whether the apprentice already has a record in the ficha, in any academic state.
     * A previously unenrolled apprentice cannot rejoin the same ficha, so the check ignores
     * the state. The references are matched through {@code _id} because both are {@code @DBRef}
     * fields of entities with String ids.
     *
     * @param studentId the apprentice profile id.
     * @param gradeId the ficha id.
     * @return {@code true} when a record already links that apprentice to that ficha.
     */
    @Query(value = "{'student._id': ?0, 'grade._id': ?1}", exists = true)
    boolean existsByStudentIdAndGradeId(String studentId, String gradeId);

    /**
     * Returns whether the apprentice is matriculado in the ficha (UC011, E8). The references are
     * matched through {@code _id} because both are {@code @DBRef} fields of entities with String
     * ids.
     *
     * @param studentId the apprentice profile id.
     * @param gradeId the ficha id.
     * @param stateAcademic the academic state the enrollment must be in.
     * @return {@code true} when a record links that apprentice to that ficha in that state.
     */
    @Query(value = "{'student._id': ?0, 'grade._id': ?1, 'state_academic': ?2}", exists = true)
    boolean existsByStudentIdAndGradeIdAndStateAcademic(String studentId, String gradeId, StateAcademic stateAcademic);

    /**
     * Finds the apprentices that match the given ficha and academic state (UC008, A2). Each
     * filter is ignored when its argument is {@code null}.
     *
     * @param gradeId the ficha id to filter by (optional).
     * @param stateAcademic the academic state to filter by (optional).
     * @param pageable the pagination information.
     * @return the page of matching apprentices.
     */
    @Query(
        "{ $and: [ " +
            "{ $or: [ { $expr: { $eq: [?0, null] } }, { 'grade._id': ?0 } ] }, " +
            "{ $or: [ { $expr: { $eq: [?1, null] } }, { 'state_academic': ?1 } ] } " +
            "] }"
    )
    Page<Apprentice> findByFilters(String gradeId, StateAcademic stateAcademic, Pageable pageable);

    /**
     * Finds the apprentices that match the given ficha, academic state and student list
     * (UC008, A2). Each filter is ignored when its argument is {@code null}, except the
     * student list, which is only used when the text filters resolved matching profiles.
     *
     * <p>The student list matches the referenced profiles through the DBRef id: a scalar
     * {@code _id} lookup resolves a String into the referenced id, but the {@code $in} operator
     * compares DBRef ids directly, so the values must be explicit {@link ObjectId} instances.
     *
     * @param gradeId the ficha id to filter by (optional).
     * @param stateAcademic the academic state to filter by (optional).
     * @param studentIds the ObjectId values of the apprentice profiles to filter by.
     * @param pageable the pagination information.
     * @return the page of matching apprentices.
     */
    @Query(
        "{ $and: [ " +
            "{ $or: [ { $expr: { $eq: [?0, null] } }, { 'grade._id': ?0 } ] }, " +
            "{ $or: [ { $expr: { $eq: [?1, null] } }, { 'state_academic': ?1 } ] }, " +
            "{ 'student.$id': { $in: ?2 } } " +
            "] }"
    )
    Page<Apprentice> findByFiltersAndStudentIds(String gradeId, StateAcademic stateAcademic, List<ObjectId> studentIds, Pageable pageable);
}

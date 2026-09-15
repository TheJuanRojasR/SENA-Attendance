package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Attendance;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Attendance entity.
 */
@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    @Query("{}")
    Page<Attendance> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Attendance> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Attendance> findOneWithEagerRelationships(String id);

    // ------- SEARCH COUNT CLASS SECTION BY ID -------
    @Query(value = "{ 'classSection.$id': { $in: ?0 } }", count = true)
    long countByClassSection_IdIn(List<ObjectId> classSectionIds);

    /**
     * Returns whether the apprentice has any attendance record in the given class sections.
     * Attendance references both the student and the class section through DBRefs: the student
     * id is matched through {@code student._id}, where a String resolves to the referenced id,
     * while the class section list compares DBRef ids ({@code $id}) and therefore needs explicit
     * {@link ObjectId} values, the same way {@link #countByClassSection_IdIn(List)} does.
     *
     * @param studentId the apprentice profile id.
     * @param classSectionIds the ObjectId values of the class sections to check.
     * @return {@code true} when at least one attendance record links that apprentice to one of the class sections.
     */
    @Query(value = "{ 'student._id': ?0, 'classSection.$id': { $in: ?1 } }", exists = true)
    boolean existsByStudentIdAndClassSectionIdIn(String studentId, List<ObjectId> classSectionIds);
}

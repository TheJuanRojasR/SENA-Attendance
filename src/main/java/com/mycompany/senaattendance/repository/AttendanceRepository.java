package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
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
 * Spring Data MongoDB repository for the Attendance entity.
 */
@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String>, AttendanceRepositoryCustom {
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

    /**
     * Finds the attendance records of a class section for one session date. The class section is
     * matched through the scalar {@code classSection._id}, where a String resolves to the
     * referenced id (unlike the {@code $id}/{$in} lookups, which need explicit ObjectIds).
     *
     * @param classSectionId the class section id.
     * @param date the session date.
     * @return the records of that session, possibly empty.
     */
    // ------- SEARCH ATTENDANCE BY CLASS SECTION AND DATE -------
    @Query("{ 'classSection._id': ?0, 'date': ?1 }")
    List<Attendance> findByClassSectionIdAndDate(String classSectionId, LocalDate date);

    /**
     * Finds the attendance record of one apprentice in a class section for a session date. This
     * is the upsert key of the session: materia, aprendiz and fecha.
     *
     * @param classSectionId the class section id.
     * @param studentId the apprentice profile id.
     * @param date the session date.
     * @return the matching record, or empty when the apprentice has none for that session.
     */
    @Query("{ 'classSection._id': ?0, 'student._id': ?1, 'date': ?2 }")
    Optional<Attendance> findByClassSectionIdAndStudentIdAndDate(String classSectionId, String studentId, LocalDate date);

    /**
     * Finds the attendance records of one apprentice in the given class sections, for a state and
     * a date range (UC011). The apprentice is matched through the scalar {@code student._id},
     * where a String resolves to the referenced id, while the class section list compares DBRef
     * ids ({@code $id}) and therefore needs explicit {@link ObjectId} values.
     *
     * @param studentId the apprentice profile id.
     * @param classSectionIds the ObjectId values of the class sections to look into.
     * @param startDate the first day of the range, inclusive.
     * @param endDate the last day of the range, inclusive.
     * @param stateAttendance the state to match.
     * @return the matching records, possibly empty.
     */
    @Query("{ 'student._id': ?0, 'classSection.$id': { $in: ?1 }, 'date': { $gte: ?2, $lte: ?3 }, 'state_attendance': ?4 }")
    List<Attendance> findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
        String studentId,
        List<ObjectId> classSectionIds,
        LocalDate startDate,
        LocalDate endDate,
        StateAttendance stateAttendance
    );

    /**
     * Finds the attendance records of one apprentice in one class section for a date range, in
     * any state. Used to rebuild the state of every programmed session when measuring the
     * consecutive failures of a materia (UC013). The apprentice is matched through the scalar
     * {@code student._id} and the class section through the scalar {@code classSection._id},
     * where a String resolves to the referenced id.
     *
     * @param studentId the apprentice profile id.
     * @param classSectionId the class section id.
     * @param startDate the first day of the range, inclusive.
     * @param endDate the last day of the range, inclusive.
     * @return the matching records, possibly empty.
     */
    @Query("{ 'student._id': ?0, 'classSection._id': ?1, 'date': { $gte: ?2, $lte: ?3 } }")
    List<Attendance> findByStudentIdAndClassSectionIdAndDateBetween(
        String studentId,
        String classSectionId,
        LocalDate startDate,
        LocalDate endDate
    );
}

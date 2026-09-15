package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the ClassSchedule entity.
 */
@Repository
public interface ClassScheduleRepository extends MongoRepository<ClassSchedule, String> {
    @Query("{}")
    Page<ClassSchedule> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<ClassSchedule> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<ClassSchedule> findOneWithEagerRelationships(String id);

    /**
     * Finds the class schedules that reference the given trimester. Used to resolve the
     * multi-hop path from a trimester to its attendance records (E3 guard).
     *
     * @param trimesterId the trimester id to match against the {@code trimester} DBRef.
     * @return the schedules whose {@code trimester} reference matches {@code trimesterId}.
     */

    // ------- SEARCH CLASS SECTION BY TRIMESTER ID -------
    @Query("{ 'trimester._id': ?0 }")
    List<ClassSchedule> findByTrimesterId(String trimesterId);

    /**
     * Finds the schedules that reference the given class section. Used to cascade the
     * deletion of a ficha onto the schedules of its class sections.
     *
     * @param classSectionId the class section id to match against the {@code classSection} DBRef.
     * @return the schedules whose {@code classSection} reference matches {@code classSectionId}.
     */
    // ------- SEARCH CLASS SCHEDULES BY CLASS SECTION ID -------
    @Query("{ 'classSection._id': ?0 }")
    List<ClassSchedule> findByClassSectionId(String classSectionId);

    /**
     * Finds the schedules of a class section on a given weekday inside a trimester. Used to
     * detect overlapping sessions of the same ficha, because the overlap check must consider
     * both the schedule's own subject and every other subject of the ficha.
     *
     * @param classSectionId the class section id to match against the {@code classSection} DBRef.
     * @param trimesterId the trimester id to match against the {@code trimester} DBRef.
     * @param dayOfWeek the weekday the schedule must match.
     * @return the schedules of that class section, trimester and weekday.
     */
    @Query("{ 'classSection._id': ?0, 'trimester._id': ?1, 'day_of_week': ?2 }")
    List<ClassSchedule> findByClassSectionIdAndTrimesterIdAndDayOfWeek(String classSectionId, String trimesterId, DayOfWeek dayOfWeek);
}

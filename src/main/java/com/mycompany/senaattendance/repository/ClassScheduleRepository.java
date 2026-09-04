package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.ClassSchedule;
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
}

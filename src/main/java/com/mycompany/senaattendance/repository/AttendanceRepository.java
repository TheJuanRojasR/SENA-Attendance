package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Attendance;
import java.util.List;
import java.util.Optional;
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
}

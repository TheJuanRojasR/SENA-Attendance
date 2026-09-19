package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.AuditLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    @Query("{}")
    Page<AuditLog> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<AuditLog> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<AuditLog> findOneWithEagerRelationships(String id);

    /**
     * Finds the audit entries of one attendance record. The record is matched through the scalar
     * {@code attendance._id}, where a String resolves to the referenced id.
     *
     * @param attendanceId the attendance record id.
     * @return the audit entries of that record, possibly empty.
     */
    @Query("{ 'attendance._id': ?0 }")
    List<AuditLog> findByAttendanceId(String attendanceId);
}

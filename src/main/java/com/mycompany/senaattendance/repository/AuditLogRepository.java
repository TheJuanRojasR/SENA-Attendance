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
}

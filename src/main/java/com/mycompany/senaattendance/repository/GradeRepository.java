package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Grade;
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
}

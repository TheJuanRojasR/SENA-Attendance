package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Justification;
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
}

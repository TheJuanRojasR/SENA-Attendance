package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.JustificationDetails;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the JustificationDetails entity.
 */
@Repository
public interface JustificationDetailsRepository extends MongoRepository<JustificationDetails, String> {
    @Query("{}")
    Page<JustificationDetails> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<JustificationDetails> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<JustificationDetails> findOneWithEagerRelationships(String id);
}

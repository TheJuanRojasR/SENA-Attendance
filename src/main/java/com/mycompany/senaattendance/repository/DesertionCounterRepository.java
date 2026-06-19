package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.DesertionCounter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the DesertionCounter entity.
 */
@Repository
public interface DesertionCounterRepository extends MongoRepository<DesertionCounter, String> {
    @Query("{}")
    Page<DesertionCounter> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<DesertionCounter> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<DesertionCounter> findOneWithEagerRelationships(String id);
}

package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Apprentice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Apprentice entity.
 */
@Repository
public interface ApprenticeRepository extends MongoRepository<Apprentice, String> {
    @Query("{}")
    Page<Apprentice> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Apprentice> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Apprentice> findOneWithEagerRelationships(String id);
}

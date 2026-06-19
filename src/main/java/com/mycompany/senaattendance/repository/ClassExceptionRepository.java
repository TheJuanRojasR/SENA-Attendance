package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.ClassException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the ClassException entity.
 */
@Repository
public interface ClassExceptionRepository extends MongoRepository<ClassException, String> {
    @Query("{}")
    Page<ClassException> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<ClassException> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<ClassException> findOneWithEagerRelationships(String id);
}

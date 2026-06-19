package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.ClassSection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the ClassSection entity.
 */
@Repository
public interface ClassSectionRepository extends MongoRepository<ClassSection, String> {
    @Query("{}")
    Page<ClassSection> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<ClassSection> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<ClassSection> findOneWithEagerRelationships(String id);
}

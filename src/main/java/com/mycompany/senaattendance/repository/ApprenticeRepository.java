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

    // ------- SEARCH BY GRADE ID -------
    @Query("{'grade._id': ?0}")
    List<Apprentice> findByGradeId(String gradeId);

    /**
     * Returns whether the apprentice already has a record in the ficha, in any academic state.
     * A previously unenrolled apprentice cannot rejoin the same ficha, so the check ignores
     * the state. The references are matched through {@code _id} because both are {@code @DBRef}
     * fields of entities with String ids.
     *
     * @param studentId the apprentice profile id.
     * @param gradeId the ficha id.
     * @return {@code true} when a record already links that apprentice to that ficha.
     */
    @Query(value = "{'student._id': ?0, 'grade._id': ?1}", exists = true)
    boolean existsByStudentIdAndGradeId(String studentId, String gradeId);
}

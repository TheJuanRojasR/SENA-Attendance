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

    // Busca un ClassSection por el ID del instructor asociado
    @Query("{'instructor._id': ?0}")
    List<ClassSection> findByInstructorId(String instructorId);

    // ------- SEARCH ACTIVE CLASSESCTION BY INSTRUCTOR -------
    @Query("{'instructor._id': ?0, 'is_active': true}")
    List<ClassSection> findByInstructorIdAndIsActiveTrue(String instructorId);

    // ------- SEARCH BY GRADE ID -------
    @Query("{'grade._id' :  ?0}")
    List<ClassSection> findByGradeId(String gradeId);

    /**
     * Returns whether a class section with the given subject name already exists inside a
     * ficha (grade), ignoring case. Used to keep the subject name unique per ficha.
     *
     * @param subjectName the subject name to check.
     * @param gradeId the ficha id the name must be unique within.
     * @return {@code true} if a class section in the same ficha uses this name.
     */
    boolean existsBySubjectNameIgnoreCaseAndGradeId(String subjectName, String gradeId);

    /**
     * Returns whether a class section with the given subject name exists inside a ficha,
     * ignoring case and excluding a specific id. Used so an update that keeps the same name
     * does not collide with itself.
     *
     * @param subjectName the subject name to check.
     * @param gradeId the ficha id the name must be unique within.
     * @param id the id to exclude.
     * @return {@code true} if another class section in the same ficha uses this name.
     */
    boolean existsBySubjectNameIgnoreCaseAndGradeIdAndIdNot(String subjectName, String gradeId, String id);
}

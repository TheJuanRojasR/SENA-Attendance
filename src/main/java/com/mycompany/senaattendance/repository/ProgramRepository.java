package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Program entity.
 */
@Repository
public interface ProgramRepository extends MongoRepository<Program, String> {
    /**
     * Finds programs whose code or name contains the given term (case-insensitive regex).
     * The whole filter runs in MongoDB so the page size and total count are accurate.
     *
     * @param term the code/name fragment to search for.
     * @param pageable the pagination information.
     * @return a page of matching programs.
     */
    @Query("{ $or: [ { 'code': { $regex: ?0, $options: 'i' } }, { 'name': { $regex: ?0, $options: 'i' } } ] }")
    Page<Program> searchByCodeOrName(String term, Pageable pageable);

    /**
     * Finds programs whose code or name contains the given term and whose status
     * matches {@code status}. The whole filter runs in MongoDB so the page size and
     * total count are accurate.
     *
     * @param term the code/name fragment to search for.
     * @param status the status to filter by.
     * @param pageable the pagination information.
     * @return a page of matching programs.
     */
    @Query(
        "{ $and: [ { $or: [ { 'code': { $regex: ?0, $options: 'i' } }, { 'name': { $regex: ?0, $options: 'i' } } ] }, { 'status': ?1 } ] }"
    )
    Page<Program> searchByCodeOrNameAndStatus(String term, Boolean status, Pageable pageable);

    /**
     * Finds programs whose status matches the given value. Runs entirely in MongoDB.
     *
     * @param status the status to filter by.
     * @param pageable the pagination information.
     * @return a page of matching programs.
     */
    Page<Program> findByStatus(Boolean status, Pageable pageable);

    /**
     * Returns whether a program with the given code already exists (case-insensitive).
     *
     * @param code the code to check.
     * @return {@code true} if a program with this code exists.
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Returns whether a program with the given initials already exists (case-insensitive).
     *
     * @param initials the initials to check.
     * @return {@code true} if a program with these initials exists.
     */
    boolean existsByInitialsIgnoreCase(String initials);

    /**
     * Returns whether a program with the given name already exists (case-insensitive).
     *
     * @param name the name to check.
     * @return {@code true} if a program with this name exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Returns whether a program with the given code exists, excluding a specific id.
     * Used so an update that keeps the same code does not collide with itself.
     *
     * @param code the code to check.
     * @param id the id to exclude.
     * @return {@code true} if another program with this code exists.
     */
    boolean existsByCodeIgnoreCaseAndIdNot(String code, String id);

    /**
     * Returns whether a program with the given initials exists, excluding a specific id.
     * Used so an update that keeps the same initials does not collide with itself.
     *
     * @param initials the initials to check.
     * @param id the id to exclude.
     * @return {@code true} if another program with these initials exists.
     */
    boolean existsByInitialsIgnoreCaseAndIdNot(String initials, String id);

    /**
     * Returns whether a program with the given name exists, excluding a specific id.
     * Used so an update that keeps the same name does not collide with itself.
     *
     * @param name the name to check.
     * @param id the id to exclude.
     * @return {@code true} if another program with this name exists.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
}

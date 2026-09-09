package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Trimester;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Trimester entity.
 */
@Repository
public interface TrimesterRepository extends MongoRepository<Trimester, String> {
    // ------- SEARCH TRIMESTER BY NAME -------
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Trimester> searchByName(String name, Pageable pageable);

    // ------- SEARCH TRIMESTER BY NAME AND STATUS -------
    @Query("{ $and: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'status': ?1 } ] }")
    Page<Trimester> searchByNameAndStatus(String name, Boolean status, Pageable pageable);

    // ------- SEARCH TRIMESTER BY START DATE YEAR -------
    @Query("{ $expr: { $eq: [ { $year: '$start_date' }, ?0 ] } }")
    Page<Trimester> searchByStartDateYear(int year, Pageable pageable);

    // ------- SEARCH TRIMESTER BY START DATE YEAR AND STATUS -------
    @Query("{ $and: [ { $expr: { $eq: [ { $year: '$start_date' }, ?0 ] } }, { 'status': ?1 } ] }")
    Page<Trimester> searchByStartDateYearAndStatus(int year, Boolean status, Pageable pageable);

    // ------- SEARCH TRIMESTER BY STATUS -------
    Page<Trimester> findByStatus(Boolean status, Pageable pageable);

    // ------- SEARCH TRIMESTER BY OVERLAPPING DATE RANGE -------
    @Query("{ 'start_date': { $lt: ?1 }, 'end_date': { $gt: ?0 } }")
    List<Trimester> findAllOverlapping(LocalDate start, LocalDate end);

    // ------- SEARCH TRIMESTER BY OVERLAPPING DATE RANGE EXCLUDING SPECIFIC ID -------
    @Query("{ 'start_date': { $lt: ?1 }, 'end_date': { $gt: ?0 }, '_id': { $ne: ?2 } }")
    List<Trimester> findAllOverlappingExcluding(LocalDate start, LocalDate end, String id);
}

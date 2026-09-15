package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
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
    Page<Trimester> searchByNameAndStatus(String name, StateTrimester status, Pageable pageable);

    // ------- SEARCH TRIMESTER BY START DATE YEAR -------
    @Query("{ $expr: { $eq: [ { $year: '$start_date' }, ?0 ] } }")
    Page<Trimester> searchByStartDateYear(int year, Pageable pageable);

    // ------- SEARCH TRIMESTER BY START DATE YEAR AND STATUS -------
    @Query("{ $and: [ { $expr: { $eq: [ { $year: '$start_date' }, ?0 ] } }, { 'status': ?1 } ] }")
    Page<Trimester> searchByStartDateYearAndStatus(int year, StateTrimester status, Pageable pageable);

    // ------- SEARCH TRIMESTER BY STATUS -------
    Page<Trimester> findByStatus(StateTrimester status, Pageable pageable);

    // ------- SEARCH TRIMESTER BY OVERLAPPING DATE RANGE -------
    @Query("{ 'start_date': { $lt: ?1 }, 'end_date': { $gt: ?0 } }")
    List<Trimester> findAllOverlapping(LocalDate start, LocalDate end);

    // ------- SEARCH TRIMESTER BY OVERLAPPING DATE RANGE EXCLUDING SPECIFIC ID -------
    @Query("{ 'start_date': { $lt: ?1 }, 'end_date': { $gt: ?0 }, '_id': { $ne: ?2 } }")
    List<Trimester> findAllOverlappingExcluding(LocalDate start, LocalDate end, String id);

    /**
     * Finds the trimester whose {@code [startDate, endDate]} range contains the given day,
     * boundaries included. Ranges do not overlap by construction, so at most one matches.
     *
     * @param date the day to look for.
     * @return the matching trimester, or an empty list when the day falls outside every range.
     */
    // ------- SEARCH TRIMESTER CONTAINING A DATE -------
    @Query("{ 'start_date': { $lte: ?0 }, 'end_date': { $gte: ?0 } }")
    List<Trimester> findAllContaining(LocalDate date);
}

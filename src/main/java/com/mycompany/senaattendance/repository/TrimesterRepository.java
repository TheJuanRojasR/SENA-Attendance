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
    /**
     * Finds trimesters whose name contains the given term (case-insensitive regex).
     * The whole filter runs in MongoDB so the page size and total count are accurate.
     *
     * @param name the name fragment to search for.
     * @param pageable the pagination information.
     * @return a page of matching trimesters.
     */
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Trimester> searchByName(String name, Pageable pageable);

    /**
     * Finds trimesters whose name contains the given term and whose status matches
     * {@code status}. The whole filter runs in MongoDB so the page size and total
     * count are accurate.
     *
     * @param name the name fragment to search for.
     * @param status the status to filter by.
     * @param pageable the pagination information.
     * @return a page of matching trimesters.
     */
    @Query("{ $and: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'status': ?1 } ] }")
    Page<Trimester> searchByNameAndStatus(String name, Boolean status, Pageable pageable);

    /**
     * Finds trimesters whose start date year equals the given year. The start date is
     * stored as a BSON date, so the year is matched with an aggregation {@code $year}
     * expression. The whole filter runs in MongoDB so the page size and total count are
     * accurate.
     *
     * @param year the year to match against the start date.
     * @param pageable the pagination information.
     * @return a page of matching trimesters.
     */
    @Query("{ $expr: { $eq: [ { $year: '$start_date' }, ?0 ] } }")
    Page<Trimester> searchByStartDateYear(int year, Pageable pageable);

    /**
     * Finds trimesters whose start date year equals the given year and whose status
     * matches {@code status}. The start date is stored as a BSON date, so the year is
     * matched with an aggregation {@code $year} expression. The whole filter runs in
     * MongoDB so the page size and total count are accurate.
     *
     * @param year the year to match against the start date.
     * @param status the status to filter by.
     * @param pageable the pagination information.
     * @return a page of matching trimesters.
     */
    @Query("{ $and: [ { $expr: { $eq: [ { $year: '$start_date' }, ?0 ] } }, { 'status': ?1 } ] }")
    Page<Trimester> searchByStartDateYearAndStatus(int year, Boolean status, Pageable pageable);

    /**
     * Finds trimesters whose status matches the given value. Runs entirely in MongoDB.
     *
     * @param status the status to filter by.
     * @param pageable the pagination information.
     * @return a page of matching trimesters.
     */
    Page<Trimester> findByStatus(Boolean status, Pageable pageable);

    /**
     * Finds trimesters whose {@code [startDate, endDate]} range overlaps the given range
     * using the strict interval condition {@code existingStart < newEnd AND
     * existingEnd > newStart}. A single-boundary touch (adjacent ranges) is not an overlap
     * and is therefore not returned.
     *
     * @param start the new range start date.
     * @param end the new range end date.
     * @return trimesters overlapping the given range.
     */
    @Query("{ 'start_date': { $lt: ?1 }, 'end_date': { $gt: ?0 } }")
    List<Trimester> findAllOverlapping(LocalDate start, LocalDate end);
}

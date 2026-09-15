package com.mycompany.senaattendance.service.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Business day arithmetic (Monday to Friday) used by the justification deadlines (UC011).
 *
 * <p>The project has no holiday calendar yet, so weekends are the only non-business days and
 * the count never depends on the ficha schedule (use-cases.md:999).
 */
public final class BusinessDays {

    private BusinessDays() {}

    /**
     * Adds business days to a date, where the starting date is not counted: adding one day to a
     * Friday yields the following Monday.
     *
     * @param date the starting date.
     * @param days the number of business days to add, zero or more.
     * @return the resulting date.
     * @throws IllegalArgumentException when {@code days} is negative.
     */
    public static LocalDate plus(LocalDate date, int days) {
        Objects.requireNonNull(date, "date");
        if (days < 0) {
            throw new IllegalArgumentException("days must not be negative");
        }
        LocalDate result = date;
        int remaining = days;
        while (remaining > 0) {
            result = result.plusDays(1);
            if (isBusinessDay(result)) {
                remaining--;
            }
        }
        return result;
    }

    /**
     * Returns the first business day strictly after the given date, so a Saturday and a Sunday
     * both resolve to the following Monday.
     *
     * @param date the reference date.
     * @return the next business day.
     */
    public static LocalDate nextBusinessDay(LocalDate date) {
        return plus(date, 1);
    }

    /**
     * Deadline of a justification (UC011): the business day after the last covered failure
     * counts as day one, so the deadline is {@code days - 1} business days after that next
     * business day. With 5 configured days and a Friday failure, the next business day is Monday
     * (day 1) and the deadline is the following Friday (day 5).
     *
     * @param lastFailure the last failure date covered by the justification.
     * @param days the configured {@code studentJustificationDays}, one or more.
     * @return the deadline date, inclusive.
     * @throws IllegalArgumentException when {@code days} is lower than one.
     */
    public static LocalDate justificationDeadline(LocalDate lastFailure, int days) {
        Objects.requireNonNull(lastFailure, "lastFailure");
        if (days < 1) {
            throw new IllegalArgumentException("days must be at least 1");
        }
        return plus(nextBusinessDay(lastFailure), days - 1);
    }

    /**
     * @param date the date to check.
     * @return whether the date is a Monday to Friday day.
     */
    public static boolean isBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date");
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}

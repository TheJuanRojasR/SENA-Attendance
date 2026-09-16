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

    /**
     * Business days an apprentice has to correct a rejected justification part (UC011, A5/E5).
     */
    public static final int CORRECTION_BUSINESS_DAYS = 2;

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

    /**
     * Counts the business days of the window {@code (from, to]}: the days after {@code from} up to
     * and including {@code to}. Used to report how many business days are left before a deadline,
     * where zero means the deadline is today or already passed.
     *
     * @param from the day before the window, not counted.
     * @param to the last day of the window, counted when it is a business day.
     * @return the number of business days in the window, zero when {@code to} is not after
     *         {@code from}.
     */
    public static long businessDaysAfter(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        long businessDays = 0;
        for (LocalDate date = from.plusDays(1); !date.isAfter(to); date = date.plusDays(1)) {
            if (isBusinessDay(date)) {
                businessDays++;
            }
        }
        return businessDays;
    }
}

package com.mycompany.senaattendance.service.util;

import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Expands the weekly class schedules of a materia into the concrete session dates of a range
 * (UC023). The dates marked as a non-teaching exception are removed by the caller, which owns the
 * exception lookup.
 */
public final class ClassSessions {

    private ClassSessions() {}

    /**
     * Expands the weekday schedules into the concrete dates of {@code [from, to]}. A date is
     * included once even when several schedules of the same list match its weekday.
     *
     * @param schedules the weekly schedules to expand.
     * @param from the first day of the range, inclusive.
     * @param to the last day of the range, inclusive.
     * @return the matching dates in ascending order, possibly empty.
     */
    public static List<LocalDate> datesInRange(List<ClassSchedule> schedules, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return List.of();
        }
        Set<LocalDate> dates = new HashSet<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            for (ClassSchedule schedule : schedules) {
                if (matches(date, schedule.getDayOfWeek())) {
                    dates.add(date);
                    break;
                }
            }
        }
        List<LocalDate> ordered = new ArrayList<>(dates);
        ordered.sort(Comparator.naturalOrder());
        return ordered;
    }

    /**
     * Expands the weekday schedules into the programmed session dates of {@code [start, end]},
     * most recent first. Used to measure the consecutive failures of a materia (UC013).
     *
     * @param schedules the schedules of the materia in the trimester.
     * @param start the first day of the window.
     * @param end the last day of the window.
     * @return the programmed session dates in descending order, possibly empty.
     */
    public static List<LocalDate> programmedSessions(List<ClassSchedule> schedules, LocalDate start, LocalDate end) {
        return datesInRange(schedules, start, end).stream().sorted(Comparator.reverseOrder()).toList();
    }

    /**
     * @param date the candidate session date.
     * @param scheduleDay the weekday of the schedule.
     * @return whether the date falls on that weekday.
     */
    public static boolean matches(LocalDate date, DayOfWeek scheduleDay) {
        if (scheduleDay == null) {
            return false;
        }
        return switch (scheduleDay) {
            case LUNES -> date.getDayOfWeek() == java.time.DayOfWeek.MONDAY;
            case MARTES -> date.getDayOfWeek() == java.time.DayOfWeek.TUESDAY;
            case MIERCOLES -> date.getDayOfWeek() == java.time.DayOfWeek.WEDNESDAY;
            case JUEVES -> date.getDayOfWeek() == java.time.DayOfWeek.THURSDAY;
            case VIERNES -> date.getDayOfWeek() == java.time.DayOfWeek.FRIDAY;
            case SABADO -> date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY;
            case DOMINGO -> date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
        };
    }
}

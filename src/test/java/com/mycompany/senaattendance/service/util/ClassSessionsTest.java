package com.mycompany.senaattendance.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClassSessions}: the expansion of the weekly schedules of a materia into
 * the concrete session dates of a range.
 */
class ClassSessionsTest {

    // 2026-09-14 is a Monday; the range below covers two full weeks.
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 14);
    private static final LocalDate RANGE_END = LocalDate.of(2026, 9, 27);

    @Test
    void datesInRangeExpandsAWeeklyScheduleInAscendingOrder() {
        ClassSchedule monday = schedule(DayOfWeek.LUNES, "06:00", "08:00");

        assertThat(ClassSessions.datesInRange(List.of(monday), MONDAY, RANGE_END)).containsExactly(
            LocalDate.of(2026, 9, 14),
            LocalDate.of(2026, 9, 21)
        );
    }

    @Test
    void datesInRangeMergesSchedulesAndDeduplicatesDates() {
        ClassSchedule monday = schedule(DayOfWeek.LUNES, "06:00", "08:00");
        ClassSchedule alsoMonday = schedule(DayOfWeek.LUNES, "10:00", "12:00");
        ClassSchedule wednesday = schedule(DayOfWeek.MIERCOLES, "06:00", "08:00");

        assertThat(ClassSessions.datesInRange(List.of(monday, alsoMonday, wednesday), MONDAY, RANGE_END)).containsExactly(
            LocalDate.of(2026, 9, 14),
            LocalDate.of(2026, 9, 16),
            LocalDate.of(2026, 9, 21),
            LocalDate.of(2026, 9, 23)
        );
    }

    @Test
    void datesInRangeIsEmptyWhenTheRangeIsInvalid() {
        ClassSchedule monday = schedule(DayOfWeek.LUNES, "06:00", "08:00");

        assertThat(ClassSessions.datesInRange(List.of(monday), RANGE_END, MONDAY)).isEmpty();
        assertThat(ClassSessions.datesInRange(List.of(monday), null, RANGE_END)).isEmpty();
    }

    @Test
    void programmedSessionsReturnsTheDatesMostRecentFirst() {
        ClassSchedule monday = schedule(DayOfWeek.LUNES, "06:00", "08:00");

        assertThat(ClassSessions.programmedSessions(List.of(monday), MONDAY, RANGE_END)).containsExactly(
            LocalDate.of(2026, 9, 21),
            LocalDate.of(2026, 9, 14)
        );
    }

    @Test
    void matchesComparesTheDateWithTheScheduleWeekday() {
        assertThat(ClassSessions.matches(MONDAY, DayOfWeek.LUNES)).isTrue();
        assertThat(ClassSessions.matches(MONDAY, DayOfWeek.MARTES)).isFalse();
        assertThat(ClassSessions.matches(MONDAY, null)).isFalse();
    }

    private static ClassSchedule schedule(DayOfWeek dayOfWeek, String startTime, String endTime) {
        return new ClassSchedule().dayOfWeek(dayOfWeek).startTime(LocalTime.parse(startTime)).endTime(LocalTime.parse(endTime));
    }
}

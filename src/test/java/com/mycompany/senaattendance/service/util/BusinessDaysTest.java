package com.mycompany.senaattendance.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BusinessDaysTest {

    private static final LocalDate FRIDAY = LocalDate.of(2026, 9, 11);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 9, 12);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 9, 13);
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 14);

    @Test
    void nextBusinessDayOfAFridayIsTheFollowingMonday() {
        assertThat(BusinessDays.nextBusinessDay(FRIDAY)).isEqualTo(MONDAY);
    }

    @Test
    void nextBusinessDayOfAWeekendIsTheFollowingMonday() {
        assertThat(BusinessDays.nextBusinessDay(SATURDAY)).isEqualTo(MONDAY);
        assertThat(BusinessDays.nextBusinessDay(SUNDAY)).isEqualTo(MONDAY);
    }

    @Test
    void nextBusinessDayOfAWeekdayIsTheNextDay() {
        assertThat(BusinessDays.nextBusinessDay(MONDAY)).isEqualTo(LocalDate.of(2026, 9, 15));
    }

    @Test
    void addingBusinessDaysSkipsWeekends() {
        assertThat(BusinessDays.plus(MONDAY, 5)).isEqualTo(LocalDate.of(2026, 9, 21));
        assertThat(BusinessDays.plus(FRIDAY, 1)).isEqualTo(MONDAY);
        assertThat(BusinessDays.plus(FRIDAY, 3)).isEqualTo(LocalDate.of(2026, 9, 16));
    }

    @Test
    void addingZeroBusinessDaysKeepsTheDate() {
        assertThat(BusinessDays.plus(FRIDAY, 0)).isEqualTo(FRIDAY);
    }

    @Test
    void addingNegativeBusinessDaysIsRejected() {
        assertThatThrownBy(() -> BusinessDays.plus(FRIDAY, -1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deadlineCountsTheNextBusinessDayAsDayOne() {
        // Friday failure with 5 days: Monday is day 1 and the following Friday is day 5.
        assertThat(BusinessDays.justificationDeadline(FRIDAY, 5)).isEqualTo(LocalDate.of(2026, 9, 18));
        // One configured day: the deadline is the next business day itself.
        assertThat(BusinessDays.justificationDeadline(FRIDAY, 1)).isEqualTo(MONDAY);
        assertThat(BusinessDays.justificationDeadline(SATURDAY, 1)).isEqualTo(MONDAY);
        assertThat(BusinessDays.justificationDeadline(MONDAY, 2)).isEqualTo(LocalDate.of(2026, 9, 16));
    }

    @Test
    void deadlineWithAnInvalidWindowIsRejected() {
        assertThatThrownBy(() -> BusinessDays.justificationDeadline(FRIDAY, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}

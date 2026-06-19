package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.TimeSlotTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TimeSlot.class);
        TimeSlot timeSlot1 = getTimeSlotSample1();
        TimeSlot timeSlot2 = new TimeSlot();
        assertThat(timeSlot1).isNotEqualTo(timeSlot2);

        timeSlot2.setId(timeSlot1.getId());
        assertThat(timeSlot1).isEqualTo(timeSlot2);

        timeSlot2 = getTimeSlotSample2();
        assertThat(timeSlot1).isNotEqualTo(timeSlot2);
    }
}

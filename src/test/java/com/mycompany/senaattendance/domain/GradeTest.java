package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.GradeTestSamples.*;
import static com.mycompany.senaattendance.domain.ModalityTestSamples.*;
import static com.mycompany.senaattendance.domain.ProgramTestSamples.*;
import static com.mycompany.senaattendance.domain.TimeSlotTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GradeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Grade.class);
        Grade grade1 = getGradeSample1();
        Grade grade2 = new Grade();
        assertThat(grade1).isNotEqualTo(grade2);

        grade2.setId(grade1.getId());
        assertThat(grade1).isEqualTo(grade2);

        grade2 = getGradeSample2();
        assertThat(grade1).isNotEqualTo(grade2);
    }

    @Test
    void programTest() {
        Grade grade = getGradeRandomSampleGenerator();
        Program programBack = getProgramRandomSampleGenerator();

        grade.setProgram(programBack);
        assertThat(grade.getProgram()).isEqualTo(programBack);

        grade.program(null);
        assertThat(grade.getProgram()).isNull();
    }

    @Test
    void modalityTest() {
        Grade grade = getGradeRandomSampleGenerator();
        Modality modalityBack = getModalityRandomSampleGenerator();

        grade.setModality(modalityBack);
        assertThat(grade.getModality()).isEqualTo(modalityBack);

        grade.modality(null);
        assertThat(grade.getModality()).isNull();
    }

    @Test
    void timeSlotTest() {
        Grade grade = getGradeRandomSampleGenerator();
        TimeSlot timeSlotBack = getTimeSlotRandomSampleGenerator();

        grade.setTimeSlot(timeSlotBack);
        assertThat(grade.getTimeSlot()).isEqualTo(timeSlotBack);

        grade.timeSlot(null);
        assertThat(grade.getTimeSlot()).isNull();
    }
}

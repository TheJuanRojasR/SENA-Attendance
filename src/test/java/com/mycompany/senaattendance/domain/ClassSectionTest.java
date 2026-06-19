package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.ClassExceptionTestSamples.*;
import static com.mycompany.senaattendance.domain.ClassScheduleTestSamples.*;
import static com.mycompany.senaattendance.domain.ClassSectionTestSamples.*;
import static com.mycompany.senaattendance.domain.GradeTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ClassSectionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClassSection.class);
        ClassSection classSection1 = getClassSectionSample1();
        ClassSection classSection2 = new ClassSection();
        assertThat(classSection1).isNotEqualTo(classSection2);

        classSection2.setId(classSection1.getId());
        assertThat(classSection1).isEqualTo(classSection2);

        classSection2 = getClassSectionSample2();
        assertThat(classSection1).isNotEqualTo(classSection2);
    }

    @Test
    void schedulesTest() {
        ClassSection classSection = getClassSectionRandomSampleGenerator();
        ClassSchedule classScheduleBack = getClassScheduleRandomSampleGenerator();

        classSection.addSchedules(classScheduleBack);
        assertThat(classSection.getScheduleses()).containsOnly(classScheduleBack);
        assertThat(classScheduleBack.getClassSection()).isEqualTo(classSection);

        classSection.removeSchedules(classScheduleBack);
        assertThat(classSection.getScheduleses()).doesNotContain(classScheduleBack);
        assertThat(classScheduleBack.getClassSection()).isNull();

        classSection.scheduleses(new HashSet<>(Set.of(classScheduleBack)));
        assertThat(classSection.getScheduleses()).containsOnly(classScheduleBack);
        assertThat(classScheduleBack.getClassSection()).isEqualTo(classSection);

        classSection.setScheduleses(new HashSet<>());
        assertThat(classSection.getScheduleses()).doesNotContain(classScheduleBack);
        assertThat(classScheduleBack.getClassSection()).isNull();
    }

    @Test
    void exceptionsTest() {
        ClassSection classSection = getClassSectionRandomSampleGenerator();
        ClassException classExceptionBack = getClassExceptionRandomSampleGenerator();

        classSection.addExceptions(classExceptionBack);
        assertThat(classSection.getExceptionses()).containsOnly(classExceptionBack);
        assertThat(classExceptionBack.getClassSection()).isEqualTo(classSection);

        classSection.removeExceptions(classExceptionBack);
        assertThat(classSection.getExceptionses()).doesNotContain(classExceptionBack);
        assertThat(classExceptionBack.getClassSection()).isNull();

        classSection.exceptionses(new HashSet<>(Set.of(classExceptionBack)));
        assertThat(classSection.getExceptionses()).containsOnly(classExceptionBack);
        assertThat(classExceptionBack.getClassSection()).isEqualTo(classSection);

        classSection.setExceptionses(new HashSet<>());
        assertThat(classSection.getExceptionses()).doesNotContain(classExceptionBack);
        assertThat(classExceptionBack.getClassSection()).isNull();
    }

    @Test
    void instructorTest() {
        ClassSection classSection = getClassSectionRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        classSection.setInstructor(userProfileBack);
        assertThat(classSection.getInstructor()).isEqualTo(userProfileBack);

        classSection.instructor(null);
        assertThat(classSection.getInstructor()).isNull();
    }

    @Test
    void gradeTest() {
        ClassSection classSection = getClassSectionRandomSampleGenerator();
        Grade gradeBack = getGradeRandomSampleGenerator();

        classSection.setGrade(gradeBack);
        assertThat(classSection.getGrade()).isEqualTo(gradeBack);

        classSection.grade(null);
        assertThat(classSection.getGrade()).isNull();
    }
}

package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.ApprenticeTestSamples.*;
import static com.mycompany.senaattendance.domain.GradeTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ApprenticeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Apprentice.class);
        Apprentice apprentice1 = getApprenticeSample1();
        Apprentice apprentice2 = new Apprentice();
        assertThat(apprentice1).isNotEqualTo(apprentice2);

        apprentice2.setId(apprentice1.getId());
        assertThat(apprentice1).isEqualTo(apprentice2);

        apprentice2 = getApprenticeSample2();
        assertThat(apprentice1).isNotEqualTo(apprentice2);
    }

    @Test
    void studentTest() {
        Apprentice apprentice = getApprenticeRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        apprentice.setStudent(userProfileBack);
        assertThat(apprentice.getStudent()).isEqualTo(userProfileBack);

        apprentice.student(null);
        assertThat(apprentice.getStudent()).isNull();
    }

    @Test
    void gradeTest() {
        Apprentice apprentice = getApprenticeRandomSampleGenerator();
        Grade gradeBack = getGradeRandomSampleGenerator();

        apprentice.setGrade(gradeBack);
        assertThat(apprentice.getGrade()).isEqualTo(gradeBack);

        apprentice.grade(null);
        assertThat(apprentice.getGrade()).isNull();
    }
}

package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.AttendanceTestSamples.*;
import static com.mycompany.senaattendance.domain.AuditLogTestSamples.*;
import static com.mycompany.senaattendance.domain.ClassSectionTestSamples.*;
import static com.mycompany.senaattendance.domain.JustificationTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AttendanceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Attendance.class);
        Attendance attendance1 = getAttendanceSample1();
        Attendance attendance2 = new Attendance();
        assertThat(attendance1).isNotEqualTo(attendance2);

        attendance2.setId(attendance1.getId());
        assertThat(attendance1).isEqualTo(attendance2);

        attendance2 = getAttendanceSample2();
        assertThat(attendance1).isNotEqualTo(attendance2);
    }

    @Test
    void auditLogsTest() {
        Attendance attendance = getAttendanceRandomSampleGenerator();
        AuditLog auditLogBack = getAuditLogRandomSampleGenerator();

        attendance.addAuditLogs(auditLogBack);
        assertThat(attendance.getAuditLogses()).containsOnly(auditLogBack);
        assertThat(auditLogBack.getAttendance()).isEqualTo(attendance);

        attendance.removeAuditLogs(auditLogBack);
        assertThat(attendance.getAuditLogses()).doesNotContain(auditLogBack);
        assertThat(auditLogBack.getAttendance()).isNull();

        attendance.auditLogses(new HashSet<>(Set.of(auditLogBack)));
        assertThat(attendance.getAuditLogses()).containsOnly(auditLogBack);
        assertThat(auditLogBack.getAttendance()).isEqualTo(attendance);

        attendance.setAuditLogses(new HashSet<>());
        assertThat(attendance.getAuditLogses()).doesNotContain(auditLogBack);
        assertThat(auditLogBack.getAttendance()).isNull();
    }

    @Test
    void classSectionTest() {
        Attendance attendance = getAttendanceRandomSampleGenerator();
        ClassSection classSectionBack = getClassSectionRandomSampleGenerator();

        attendance.setClassSection(classSectionBack);
        assertThat(attendance.getClassSection()).isEqualTo(classSectionBack);

        attendance.classSection(null);
        assertThat(attendance.getClassSection()).isNull();
    }

    @Test
    void studentTest() {
        Attendance attendance = getAttendanceRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        attendance.setStudent(userProfileBack);
        assertThat(attendance.getStudent()).isEqualTo(userProfileBack);

        attendance.student(null);
        assertThat(attendance.getStudent()).isNull();
    }

    @Test
    void modifiedByJustificationTest() {
        Attendance attendance = getAttendanceRandomSampleGenerator();
        Justification justificationBack = getJustificationRandomSampleGenerator();

        attendance.setModifiedByJustification(justificationBack);
        assertThat(attendance.getModifiedByJustification()).isEqualTo(justificationBack);

        attendance.modifiedByJustification(null);
        assertThat(attendance.getModifiedByJustification()).isNull();
    }
}

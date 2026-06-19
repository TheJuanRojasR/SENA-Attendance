package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.AttendanceTestSamples.*;
import static com.mycompany.senaattendance.domain.AuditLogTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuditLog.class);
        AuditLog auditLog1 = getAuditLogSample1();
        AuditLog auditLog2 = new AuditLog();
        assertThat(auditLog1).isNotEqualTo(auditLog2);

        auditLog2.setId(auditLog1.getId());
        assertThat(auditLog1).isEqualTo(auditLog2);

        auditLog2 = getAuditLogSample2();
        assertThat(auditLog1).isNotEqualTo(auditLog2);
    }

    @Test
    void modifiedByTest() {
        AuditLog auditLog = getAuditLogRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        auditLog.setModifiedBy(userProfileBack);
        assertThat(auditLog.getModifiedBy()).isEqualTo(userProfileBack);

        auditLog.modifiedBy(null);
        assertThat(auditLog.getModifiedBy()).isNull();
    }

    @Test
    void attendanceTest() {
        AuditLog auditLog = getAuditLogRandomSampleGenerator();
        Attendance attendanceBack = getAttendanceRandomSampleGenerator();

        auditLog.setAttendance(attendanceBack);
        assertThat(auditLog.getAttendance()).isEqualTo(attendanceBack);

        auditLog.attendance(null);
        assertThat(auditLog.getAttendance()).isNull();
    }
}

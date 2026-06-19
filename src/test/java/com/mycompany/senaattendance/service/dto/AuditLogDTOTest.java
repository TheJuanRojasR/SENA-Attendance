package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuditLogDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuditLogDTO.class);
        AuditLogDTO auditLogDTO1 = new AuditLogDTO();
        auditLogDTO1.setId("id1");
        AuditLogDTO auditLogDTO2 = new AuditLogDTO();
        assertThat(auditLogDTO1).isNotEqualTo(auditLogDTO2);
        auditLogDTO2.setId(auditLogDTO1.getId());
        assertThat(auditLogDTO1).isEqualTo(auditLogDTO2);
        auditLogDTO2.setId("id2");
        assertThat(auditLogDTO1).isNotEqualTo(auditLogDTO2);
        auditLogDTO1.setId(null);
        assertThat(auditLogDTO1).isNotEqualTo(auditLogDTO2);
    }
}

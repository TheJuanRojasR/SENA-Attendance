package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.DocumentTypeTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserProfile.class);
        UserProfile userProfile1 = getUserProfileSample1();
        UserProfile userProfile2 = new UserProfile();
        assertThat(userProfile1).isNotEqualTo(userProfile2);

        userProfile2.setId(userProfile1.getId());
        assertThat(userProfile1).isEqualTo(userProfile2);

        userProfile2 = getUserProfileSample2();
        assertThat(userProfile1).isNotEqualTo(userProfile2);
    }

    @Test
    void documentTypeTest() {
        UserProfile userProfile = getUserProfileRandomSampleGenerator();
        DocumentType documentTypeBack = getDocumentTypeRandomSampleGenerator();

        userProfile.setDocumentType(documentTypeBack);
        assertThat(userProfile.getDocumentType()).isEqualTo(documentTypeBack);

        userProfile.documentType(null);
        assertThat(userProfile.getDocumentType()).isNull();
    }
}

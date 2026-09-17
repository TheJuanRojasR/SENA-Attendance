package com.mycompany.senaattendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.mapper.UserProfileMapper;
import com.mycompany.senaattendance.web.rest.vm.ManagedUserVM;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for the explicit compensation of {@link UserService#registerUser}: the account and
 * its profile are two writes without a transaction on MongoDB standalone, so a profile failure
 * must roll the account back on a best-effort basis and propagate the original error.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String DOCUMENT_TYPE_ID = "document-type-id";

    private static final String DOCUMENT_NUMBER = "1234567890";

    private static final String EMAIL = "register@example.com";

    private static final String PASSWORD = "Password1!";

    private static final String CREATED_USER_ID = "created-user-id";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private ClassSectionRepository classSectionRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserRemovesTheCreatedUserWhenTheProfileSaveFails() {
        stubSuccessfulUserCreation();

        IllegalStateException storageFailure = new IllegalStateException("storage down");
        when(userProfileRepository.save(any(UserProfile.class))).thenThrow(storageFailure);

        assertThatThrownBy(() -> userService.registerUser(registrationVM(), PASSWORD)).isSameAs(storageFailure);

        ArgumentCaptor<User> deletedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(deletedUser.capture());
        assertThat(deletedUser.getValue().getId()).isEqualTo(CREATED_USER_ID);
    }

    @Test
    void registerUserKeepsTheUserWhenTheProfileIsSaved() {
        stubSuccessfulUserCreation();
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(registrationVM(), PASSWORD);

        assertThat(registered.getId()).isEqualTo(CREATED_USER_ID);
        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * Stubs the reads and the user save of a valid registration, so the test reaches the profile
     * save. The mocked save returns the same instance with an id, mirroring Spring Data behavior.
     */
    private void stubSuccessfulUserCreation() {
        DocumentType documentType = new DocumentType();
        documentType.setId(DOCUMENT_TYPE_ID);
        documentType.setInitials("CC");
        documentType.setIsActive(true);
        when(documentTypeRepository.findById(DOCUMENT_TYPE_ID)).thenReturn(Optional.of(documentType));

        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(
            Optional.of(new Authority().name(AuthoritiesConstants.USER))
        );
        when(authorityRepository.findById(AuthoritiesConstants.APPRENTICE)).thenReturn(
            Optional.of(new Authority().name(AuthoritiesConstants.APPRENTICE))
        );

        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(CREATED_USER_ID);
            return user;
        });
    }

    private static ManagedUserVM registrationVM() {
        ManagedUserVM userVM = new ManagedUserVM();
        userVM.setEmail(EMAIL);
        userVM.setDocumentTypeId(DOCUMENT_TYPE_ID);
        userVM.setDocumentNumber(DOCUMENT_NUMBER);
        userVM.setFirstName("Register");
        userVM.setFirstLastName("Tester");
        userVM.setPhoneNumber("3000000000");
        return userVM;
    }
}

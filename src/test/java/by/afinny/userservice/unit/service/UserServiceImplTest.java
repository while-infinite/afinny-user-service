package by.afinny.userservice.unit.service;

import by.afinny.userservice.dto.ChangingEmailDto;
import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.NotificationChangerDto;
import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.dto.PasswordDto;
import by.afinny.userservice.dto.SecurityDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.mapper.NotificationMapper;
import by.afinny.userservice.mapper.NotificationMapperImpl;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.AuthenticationService;
import by.afinny.userservice.service.impl.UserServiceImpl;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private ClientRepository clientRepository;
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    @Spy
    private static NotificationMapper notificationMapper = new NotificationMapperImpl();

    private final String NEW_PASSWORD = "_590813jKSdD2_";
    private final String STORED_PASSWORD = "password";
    private final String ENCODED_PASSWORD = "$2a$12$kcnGt/oJ.6RNO7kWZvAkvenoPNgBmLysdVXvaIFdGc.Jy.XQlmgZm";
    private final UUID CLIENT_ID = UUID.fromString("9b81ee52-2c0d-4bda-90b4-0b12e9d6f467");
    private final UUID USER_PROFILE_ID = UUID.fromString("dda4558c-f366-48f2-a113-e7afcd65424e");
    private final String EMPLOYER_ID = "23986498201";
    private final String MOBILE_PHONE = "00123456789";
    private final String EMAIL = "mixawet616@dufeed.com";

    private PasswordDto passwordDto;
    private NotificationChangerDto notificationChangerDto;
    private SecurityDto securityDto;
    private ChangingEmailDto changingEmailDto;
    private MobilePhoneDto mobilePhoneDto;
    private UserProfile userProfile;
    private Client client;

    @BeforeEach
    public void setUp() {
        client = Client.builder()
            .id(CLIENT_ID)
            .firstName("Анна")
            .middleName("Николаевна")
            .lastName("Смирнова")
            .countryOfResidence(true)
            .accessionDate(LocalDate.of(2021, 10, 30))
            .mobilePhone(MOBILE_PHONE)
            .employerIdentificationNumber(EMPLOYER_ID)
            .clientStatus(ClientStatus.ACTIVE)
            .passportData(PassportData.builder().passportNumber("6235104793").build())
            .build();

        userProfile = UserProfile.builder()
            .id(USER_PROFILE_ID)
            .smsNotification(false)
            .pushNotification(false)
            .emailSubscription(false)
            .password(ENCODED_PASSWORD)
            .email(EMAIL)
            .securityQuestion("Имя первого учителя")
            .securityAnswer("Лариса")
            .appRegistrationDate(LocalDate.now())
            .client(client)
            .build();

        passwordDto = PasswordDto.builder()
            .newPassword(NEW_PASSWORD).build();

        notificationChangerDto = NotificationChangerDto.builder()
            .notificationStatus(true).build();

        securityDto = SecurityDto.builder()
            .securityQuestion("Город рождения бабушки")
            .securityAnswer("Суздаль").build();

        changingEmailDto = ChangingEmailDto.builder()
            .newEmail("sxgkavkkumuh@scpulse.com").build();

        mobilePhoneDto = MobilePhoneDto.builder()
                .mobilePhone("86664122551").build();
    }

    @Test
    @DisplayName("If incoming password equals to stored then reset password")
    void changePassword_shouldResetPassword() {
        //ARRANGE
        wayPasswordsEqual();
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        userService.changePassword(passwordDto, CLIENT_ID);
        //VERIFY
        verify(authenticationService).resetPasswordByUserProfile(userProfile, NEW_PASSWORD);
    }

    @Test
    @DisplayName("If incoming password isn't equal to stored then throw exception")
    void changePassword_ifPasswordsArentEqual_thenThrow() {
        //ARRANGE
        wayPasswordsNotEqual();
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        ThrowingCallable changePasswordMethod = () -> userService.changePassword(passwordDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changePasswordMethod).isInstanceOf(RegistrationException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void changePassword_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changePasswordMethod = () -> userService.changePassword(passwordDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changePasswordMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If security data successfully changed then don't return content")
    void changeSecurityData_shouldNotReturnContent() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        userService.changeSecurityData(securityDto, CLIENT_ID);
        //VERIFY
        ArgumentCaptor<UserProfile> updatedUserProfile = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(updatedUserProfile.capture());
        verifySecurityDataSet(updatedUserProfile.getValue());
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void changeSecurityData_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changeSecurityDataMethod = () -> userService.changeSecurityData(securityDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changeSecurityDataMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If notification settings successfully got then return notifications")
    void getNotifications_shouldReturnNotifications() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        NotificationDto notifications = userService.getNotifications(CLIENT_ID);
        //VERIFY
        verifyNotifications(notifications);
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void getNotification_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable getNotificationsMethod = () -> userService.getNotifications(CLIENT_ID);
        //VERIFY
        assertThatThrownBy(getNotificationsMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If sms notification successfully changed then don't return content")
    void changeSmsNotifications_shouldNotReturnContent() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        userService.changeSmsNotification(notificationChangerDto, CLIENT_ID);
        //VERIFY
        verifySmsNotification(userProfile.getSmsNotification(), notificationChangerDto.getNotificationStatus());
    }

    @Test
    @DisplayName("If client not found then throw exception")
    void changeSmsNotifications_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changeSmsNotificationsMethod = () -> userService.changeSmsNotification(notificationChangerDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changeSmsNotificationsMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If push notification successfully changed then don't return content")
    void changePushNotifications_shouldNotReturnContent() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        userService.changePushNotification(notificationChangerDto, CLIENT_ID);
        //VERIFY
        verifyPushNotification(userProfile.getPushNotification(), notificationChangerDto.getNotificationStatus());
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void changePushNotifications_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changeSmsNotificationsMethod = () -> userService.changePushNotification(notificationChangerDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changeSmsNotificationsMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If email subscription successfully changed then don't return content")
    void changeEmailSubscription_shouldNotReturnContent() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        userService.changeEmailSubscription(notificationChangerDto, CLIENT_ID);
        //VERIFY
        verifyEmailSubscription(userProfile.getEmailSubscription(), notificationChangerDto.getNotificationStatus());
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void changeEmailSubscription_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changeEmailSubscriptionMethod = () -> userService.changeEmailSubscription(notificationChangerDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changeEmailSubscriptionMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }

    @Test
    @DisplayName("If client found then don't return content")
    void changeEmail_shouldNotReturnContent() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        userService.changeEmail(changingEmailDto, CLIENT_ID);
        //VERIFY
        verifyEmail(userProfile);
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void changeEmail_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changeEmailMethod = () -> userService.changeEmail(changingEmailDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changeEmailMethod).isInstanceOf(EntityNotFoundException.class);
        verify(userProfileRepository, never()).save(userProfile);
    }
    @Test
    @DisplayName("If client found then don't return content")
    void changeMobilePhone_shouldNotReturnContent() {
        //ARRANGE
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        //ACT
        userService.changeMobilePhone(mobilePhoneDto, CLIENT_ID);
        //VERIFY
        verifyMobilePhone(client);
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("If client wasn't found then throw exception")
    void changeMobilePhone_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable changeEmailMethod = () -> userService.changeMobilePhone(mobilePhoneDto, CLIENT_ID);
        //VERIFY
        assertThatThrownBy(changeEmailMethod).isInstanceOf(EntityNotFoundException.class);
        verify(clientRepository, never()).save(client);
    }

    @Test
    @DisplayName("If employers identification numbers differ then change")
    void modifyEmployerId_ifEmployerIdsDiffer_thenChangeEmployerId() {
        //ARRANGE
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        String newEmployerId = "09827465193";
        //ACT
        userService.modifyEmployerId(CLIENT_ID, newEmployerId);
        //VERIFY
        assertThat(client.getEmployerIdentificationNumber()).isEqualTo(newEmployerId);
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("If employers identification numbers equal then do nothing")
    void modifyEmployerId_ifEmployerIdsEqual_thenDoNothing() {
        //ARRANGE
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        //ACT
        userService.modifyEmployerId(CLIENT_ID, client.getEmployerIdentificationNumber());
        //VERIFY
        verify(clientRepository, never()).save(client);
    }

    @Test
    @DisplayName("If client hasn't been found then throw exception")
    void modifyEmployerId_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable modifyEmployerIdMethod = () -> userService.modifyEmployerId(CLIENT_ID, EMPLOYER_ID);
        //VERIFY
        assertThatThrownBy(modifyEmployerIdMethod).isInstanceOf(EntityNotFoundException.class);
    }

    private void wayPasswordsEqual() {
        passwordDto.setPassword(STORED_PASSWORD);
    }

    private void wayPasswordsNotEqual() {
        passwordDto.setPassword("");
    }

    private void verifySecurityDataSet(UserProfile userProfile) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(userProfile.getSecurityQuestion())
                .withFailMessage("Security question should be reset")
                .isEqualTo(securityDto.getSecurityQuestion());
            softAssertions.assertThat(userProfile.getSecurityAnswer())
                .withFailMessage("Security answer should be reset")
                .isEqualTo(securityDto.getSecurityAnswer());
        });
    }

    private void verifyNotifications(NotificationDto notifications) {
        assertThat(notifications).withFailMessage("Notifications shouldn't be null").isNotNull();
        verifySmsNotification(notifications.getSmsNotification(), userProfile.getSmsNotification());
        verifyPushNotification(notifications.getPushNotification(), userProfile.getPushNotification());
        verifyEmailSubscription(notifications.getEmailSubscription(), userProfile.getEmailSubscription());
    }

    private void verifySmsNotification(Boolean actualSmsNotification, Boolean expectedSmsNotification) {
        assertThat(actualSmsNotification)
            .withFailMessage("Sms notification should be " + expectedSmsNotification + " instead of " + actualSmsNotification)
            .isEqualTo(expectedSmsNotification);
    }

    private void verifyPushNotification(Boolean actualPushNotification, Boolean expectedPushNotification) {
        assertThat(actualPushNotification)
            .withFailMessage("Push notification should be " + expectedPushNotification + " instead of " + actualPushNotification)
            .isEqualTo(expectedPushNotification);
    }

    private void verifyEmailSubscription(Boolean actualEmailSubscription, Boolean expectedEmailSubscription) {
        assertThat(actualEmailSubscription)
            .withFailMessage("Email subscription should be " + expectedEmailSubscription + " instead of " + actualEmailSubscription)
            .isEqualTo(expectedEmailSubscription);
    }

    private void verifyEmail(UserProfile userProfile) {
        assertSoftly(softAssertions -> {
            String email = userProfile.getEmail();
            softAssertions.assertThat(email)
                .withFailMessage("Email shouldn't be null")
                .isNotNull();
            String newEmail = changingEmailDto.getNewEmail();
            softAssertions.assertThat(email)
                .withFailMessage("Email should be " + newEmail + " instead of " + email)
                .isEqualTo(newEmail);
        });
    }

        private void verifyMobilePhone(Client client) {
            assertSoftly(softAssertions -> {
                String phone = client.getMobilePhone();
                softAssertions.assertThat(phone)
                        .withFailMessage("Mobile phone shouldn't be null")
                        .isNotNull();
                String newMobilePhone = mobilePhoneDto.getMobilePhone();
                softAssertions.assertThat(phone)
                        .withFailMessage("Mobile phone should be " + newMobilePhone
                                + " instead of " + phone)
                        .isEqualTo(newMobilePhone);
            });
    }
}

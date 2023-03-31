package by.afinny.userservice.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.dto.LoginByPinDto;
import by.afinny.userservice.dto.LoginDto;
import by.afinny.userservice.entity.AuthenticationType;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.Fingerprint;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.BadCredentialsException;
import by.afinny.userservice.exception.WrongCredentialsException;
import by.afinny.userservice.mapper.FingerprintMapper;
import by.afinny.userservice.mapper.FingerprintMapperImpl;
import by.afinny.userservice.repository.FingerprintRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.impl.AuthenticationServiceImpl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;
    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private FingerprintRepository fingerprintRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    @Spy
    private FingerprintMapper fingerprintMapper = new FingerprintMapperImpl();

    private final UUID CLIENT_ID = UUID.fromString("4acf5c23-d7aa-4924-974d-a7284c289317");
    private final UUID USER_ID = UUID.fromString("725764ce-e246-11ec-8fea-0242ac120002");
    private final String PASSPORT_NUMBER = "3001919450";
    private final String MOBILE_PHONE = "79142790201";
    private final String STORED_PASSWORD = "password";
    private final String ENCODED_PASSWORD = "$2a$12$kcnGt/oJ.6RNO7kWZvAkvenoPNgBmLysdVXvaIFdGc.Jy.XQlmgZm";
    private final String NEW_PASSWORD = "fjkdmw098d!1";
    private final String INVALID_PASSWORD = "894ka-31elr_";
    private final String FINGERPRINT =  "FINGERPRINT";

    private UserProfile userProfile;
    private Fingerprint fingerprint;
    private LoginDto loginDto;
    private LoginByPinDto loginByPinDto;
    private FingerprintDto fingerprintDto;

    @BeforeEach
    void setUp() {
        Client client = Client.builder()
                .id(CLIENT_ID)
                .firstName("Анна")
                .middleName("Николаевна")
                .lastName("Смирнова")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(PassportData.builder().passportNumber(PASSPORT_NUMBER).build())
                .build();

        userProfile = UserProfile.builder()
                .id(USER_ID)
                .smsNotification(false)
                .pushNotification(false)
                .emailSubscription(false)
                .password(ENCODED_PASSWORD)
                .email("mixawet616@dufeed.com")
                .securityQuestion("Любимое блюдо")
                .securityAnswer("Паста карбонара")
                .appRegistrationDate(LocalDate.now())
                .client(client)
                .build();

        fingerprint = Fingerprint.builder()
                .id(UUID.fromString("c3ed67fc-e246-11ec-8fea-0242ac120002"))
                .fingerprint(FINGERPRINT)
                .client(client)
                .build();

        loginByPinDto = LoginByPinDto.builder()
                .fingerprint(FINGERPRINT)
                .clientId(CLIENT_ID)
                .build();

        fingerprintDto = FingerprintDto.builder()
                .fingerprint("fingerprint")
                .clientId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("If login by mobile phone then return client id")
    void getCredentials_ifByMobilePhoneAndValidPassword_thenReturnClientId() {
        //ARRANGE
        wayLoginWithMobilePhone(STORED_PASSWORD);
        when(userProfileRepository.findByClientMobilePhone(MOBILE_PHONE))
                .thenReturn(Optional.of(userProfile));
        //ACT
        UUID clientId = authenticationService.getCredentials(loginDto);
        //VERIFY
        assertThat(clientId).isEqualTo(CLIENT_ID);
        verify(userProfileRepository).findByClientMobilePhone(MOBILE_PHONE);
    }

    @Test
    @DisplayName("If login by passport number then return client id")
    void getCredentials_ifByPassportNumberAndValidPassword_thenReturnClientId() {
        //ARRANGE
        wayLoginWithPassportNumber(STORED_PASSWORD);
        when(userProfileRepository.findByClientPassportDataPassportNumber(PASSPORT_NUMBER))
                .thenReturn(Optional.of(userProfile));
        //ACT
        UUID clientId = authenticationService.getCredentials(loginDto);
        //VERIFY
        assertThat(clientId).isEqualTo(CLIENT_ID);
        verify(userProfileRepository).findByClientPassportDataPassportNumber(PASSPORT_NUMBER);
    }

    @Test
    @DisplayName("If login by mobile phone and received invalid password then throw")
    void getCredentials_ifByMobilePhoneAndInvalidPassword_thenThrow() {
        //ARRANGE
        wayLoginWithMobilePhone(INVALID_PASSWORD);
        when(userProfileRepository.findByClientMobilePhone(MOBILE_PHONE))
                .thenReturn(Optional.of(userProfile));
        //ACT
        ThrowingCallable getCredentialsMethod = () -> authenticationService.getCredentials(loginDto);
        //VERIFY
        assertThatThrownBy(getCredentialsMethod)
                .isInstanceOf(WrongCredentialsException.class);
    }

    @Test
    @DisplayName("If login by passport number received invalid password then throw")
    void getCredentials_ifByPassportNumberAndInvalidPassword_thenThrow() {
        //ARRANGE
        wayLoginWithPassportNumber(INVALID_PASSWORD);
        when(userProfileRepository.findByClientPassportDataPassportNumber(PASSPORT_NUMBER))
                .thenReturn(Optional.of(userProfile));
        //ACT
        ThrowingCallable getCredentialsMethod = () -> authenticationService.getCredentials(loginDto);
        //VERIFY
        assertThatThrownBy(getCredentialsMethod)
                .isInstanceOf(WrongCredentialsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {STORED_PASSWORD, INVALID_PASSWORD})
    @DisplayName("If received invalid mobile phone then throw")
    void getCredentials_ifMobilePhoneInvalid_thenThrow(String password) {
        //ARRANGE
        wayLoginWithMobilePhone(password);
        when(userProfileRepository.findByClientMobilePhone(MOBILE_PHONE))
                .thenReturn(Optional.empty());
        //ACT
        ThrowingCallable getCredentialsMethod = () -> authenticationService.getCredentials(loginDto);
        //VERIFY
        assertThatThrownBy(getCredentialsMethod)
                .isInstanceOf(WrongCredentialsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {STORED_PASSWORD, INVALID_PASSWORD})
    @DisplayName("If received invalid passport number then throw")
    void getCredentials_ifPassportNumberInvalid_thenThrow(String password) {
        //ARRANGE
        wayLoginWithPassportNumber(password);
        when(userProfileRepository.findByClientPassportDataPassportNumber(PASSPORT_NUMBER))
                .thenReturn(Optional.empty());
        //ACT
        ThrowingCallable getCredentialsMethod = () -> authenticationService.getCredentials(loginDto);
        //VERIFY
        assertThatThrownBy(getCredentialsMethod)
                .isInstanceOf(WrongCredentialsException.class);
    }

    @Test
    @DisplayName("If user found then reset password")
    void resetPasswordByMobilePhone_shouldNotReturnContent() {
        //ARRANGE
        when(userProfileRepository.findByClientMobilePhone(MOBILE_PHONE)).thenReturn(Optional.of(userProfile));
        ArgumentCaptor<UserProfile> savedUserProfile = ArgumentCaptor.forClass(UserProfile.class);
        when(userProfileRepository.save(savedUserProfile.capture())).thenAnswer(passedArgument());
        //ACT
        authenticationService.resetPasswordByMobilePhone(MOBILE_PHONE, NEW_PASSWORD);
        //VERIFY
        assertThat(passwordEncoder.matches(NEW_PASSWORD, savedUserProfile.getValue().getPassword())).isTrue();
    }

    @Test
    @DisplayName("If user not found then throw")
    void resetPasswordByMobilePhone_ifUserNotFound_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientMobilePhone(MOBILE_PHONE)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable resetPasswordByMobilePhoneMethod = () -> authenticationService
                .resetPasswordByMobilePhone(MOBILE_PHONE, NEW_PASSWORD);
        //VERIFY
        assertThatThrownBy(resetPasswordByMobilePhoneMethod)
                .isInstanceOf(WrongCredentialsException.class);
    }

    @Test
    @DisplayName("If user found then reset password")
    void resetPasswordByUserProfile_shouldNotReturnContent() {
        //ARRANGE
        ArgumentCaptor<UserProfile> savedUserProfile = ArgumentCaptor.forClass(UserProfile.class);
        when(userProfileRepository.save(savedUserProfile.capture())).thenAnswer(passedArgument());
        //ACT
        authenticationService.resetPasswordByUserProfile(userProfile, NEW_PASSWORD);
        //VERIFY
        assertThat(passwordEncoder.matches(NEW_PASSWORD, savedUserProfile.getValue().getPassword())).isTrue();
    }

    @Test
    @DisplayName("If correct data then return user id")
    void getCredentialsByPin_shouldReturnUserId() {
        //ARRANGE
        when(fingerprintRepository.findByClientIdAndFingerprint(CLIENT_ID, FINGERPRINT))
                .thenReturn(Optional.of(fingerprint));
        //ACT
        UUID userId = authenticationService.checkFingerprintForLoginById(loginByPinDto);
        //VERIFY
        assertThat(userId).isEqualTo(CLIENT_ID);

    }

    @Test
    @DisplayName("If fingerprint not found then throw")
    void getCredentialsByPin_ifFingerprintNotFound_thenThrow() {
        //ARRANGE
        when(fingerprintRepository.findByClientIdAndFingerprint(CLIENT_ID, FINGERPRINT))
                .thenReturn(Optional.empty());
        //ACT
        ThrowingCallable getCredentialsByPinMethod = () -> authenticationService.checkFingerprintForLoginById(loginByPinDto);
        //VERIFY
        assertThatThrownBy(getCredentialsByPinMethod)
                .isInstanceOf(BadCredentialsException.class)
                .hasFieldOrPropertyWithValue("errorMessage", "Incorrect fingerprint");
    }

    private void wayLoginWithMobilePhone(String password) {
        loginDto = LoginDto.builder()
                .login("+" + MOBILE_PHONE)
                .type(AuthenticationType.PHONE_NUMBER)
                .password(password).build();
    }

    private void wayLoginWithPassportNumber(String password) {
        loginDto = LoginDto.builder()
                .login(PASSPORT_NUMBER)
                .type(AuthenticationType.PASSPORT_NUMBER)
                .password(password).build();
    }

    private Answer<Object> passedArgument() {
        return invocationOnMock -> invocationOnMock.getArgument(0);
    }
}
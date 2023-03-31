package by.afinny.userservice.unit.service;

import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.dto.credit.CreditDto;
import by.afinny.userservice.dto.deposit.AccountDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.AccountExistException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.mapper.ClientMapper;
import by.afinny.userservice.mapper.ClientMapperImpl;
import by.afinny.userservice.mapper.RequestRegisterNonClientMapper;
import by.afinny.userservice.mapper.RequestRegisterNonClientMapperImpl;
import by.afinny.userservice.mapper.ResponseRegisterNonClientMapper;
import by.afinny.userservice.mapper.ResponseRegisterNonClientMapperImpl;
import by.afinny.userservice.mapper.UserProfileMapper;
import by.afinny.userservice.mapper.UserProfileMapperImpl;
import by.afinny.userservice.openfeign.credit.CreditClient;
import by.afinny.userservice.openfeign.deposit.AccountClient;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.impl.RegistrationServiceImpl;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class RegistrationServiceImplTest {

    @InjectMocks
    private RegistrationServiceImpl registrationService;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PassportDataRepository passportDataRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private CreditClient creditClient;
    @Mock
    private AccountClient accountClient;
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    @Spy
    private ClientMapper clientMapper = new ClientMapperImpl();
    @Spy
    private UserProfileMapper userProfileMapper = new UserProfileMapperImpl();
    @Spy
    private RequestRegisterNonClientMapper requestRegisterNonClientMapper = new RequestRegisterNonClientMapperImpl();
    @Spy
    private ResponseRegisterNonClientMapper responseRegisterNonClientMapper = new ResponseRegisterNonClientMapperImpl();

    private final String MOBILE_PHONE = "79182546634";
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String CLIENT_PASSWORD = "123qWeRtY456.!";

    @Captor
    private ArgumentCaptor<UserProfile> savedUserProfile;
    @Captor
    private ArgumentCaptor<ClientStatus> savedClientStatus;
    private UserProfile userProfile;
    private Client client;
    private RequestClientDto requestClientDto;
    private RequestNonClientDto requestNonClientDto;
    private PassportDto passportDto;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(CLIENT_ID)
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .clientStatus(ClientStatus.NOT_REGISTERED)
                .countryOfResidence(true)
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("7727563778")
                .passportData(PassportData.builder()
                        .passportNumber("1304323343")
                        .issuanceDate(Date.from(Instant.parse("2014-02-13T00:00:01.00Z")))
                        .nationality("Русский")
                        .birthDate(Date.from(Instant.parse("1993-11-30T00:00:01.00Z"))).build())
                .build();

        requestClientDto = RequestClientDto.builder()
                .id(CLIENT_ID)
                .mobilePhone(MOBILE_PHONE)
                .password(CLIENT_PASSWORD)
                .securityQuestion("День и месяц рождения собаки")
                .securityAnswer("3 июля")
                .email("dabhom120@eoscast.com").build();

        requestNonClientDto = RequestNonClientDto.builder()
                .mobilePhone(MOBILE_PHONE)
                .password(CLIENT_PASSWORD)
                .securityQuestion("Девичья фамилия матери")
                .securityAnswer("Семенова")
                .email("mixawet616@dufeed.com")
                .firstName("Петр")
                .middleName("Петрович")
                .lastName("Петров")
                .passportNumber("1010325260")
                .countryOfResidence(true).build();

        userProfile = UserProfile.builder()
                .id(CLIENT_ID)
                .smsNotification(true)
                .pushNotification(true)
                .emailSubscription(false)
                .password(requestClientDto.getPassword())
                .email(requestClientDto.getEmail())
                .securityQuestion(requestClientDto.getSecurityQuestion())
                .securityAnswer(requestClientDto.getSecurityAnswer())
                .appRegistrationDate(LocalDate.now())
                .client(client).build();

        passportDto = PassportDto.builder()
                .passportNumber("123456")
                .build();
    }

    @Test
    @DisplayName("If user isn't bank client then return phone and status")
    void verifyMobilePhone_ifNotClient_thenReturnMobilePhoneAndStatus() {
        //ARRANGE
        wayNotClient();
        when(clientRepository.findClientByMobilePhone(MOBILE_PHONE)).thenReturn(Optional.empty());
        //ACT
        ClientDto clientDto = registrationService.verifyMobilePhone(MOBILE_PHONE);
        //VERIFY
        verifyPhone(clientDto);
        assertThat(clientDto.getClientStatus()).isEqualTo(ClientStatus.NOT_CLIENT);
    }

    @Test
    @DisplayName("If user is a bank client then return phone, status and id")
    void verifyMobilePhone_ifClientNotRegistered_thenReturnMobilePhoneAndStatusAndId() {
        //ARRANGE
        wayNotRegistered();
        when(clientRepository.findClientByMobilePhone(MOBILE_PHONE)).thenReturn(Optional.of(client));
        //ACT
        ClientDto clientDto = registrationService.verifyMobilePhone(MOBILE_PHONE);
        //VERIFY
        verifyClient(clientDto);
        assertThat(clientDto.getClientStatus()).isEqualTo(ClientStatus.NOT_REGISTERED);
    }

    @Test
    @DisplayName("If user already has account then throws AccountExistException")
    void verifyMobilePhone_ifClientRegistered_thenThrow() {
        //ARRANGE
        wayRegisteredClient();
        when(clientRepository.findClientByMobilePhone(MOBILE_PHONE)).thenReturn(Optional.of(client));
        //ACT
        ThrowingCallable verifyMobilePhoneMethod = () -> registrationService.verifyMobilePhone(MOBILE_PHONE);
        //VERIFY
        assertThatThrownBy(verifyMobilePhoneMethod)
                .isInstanceOf(AccountExistException.class)
                .hasFieldOrPropertyWithValue("status", client.getClientStatus().toString());
    }

    @ParameterizedTest
    @MethodSource("generateProducts")
    @DisplayName("Verify defining client status related on active products and set fields")
    void registerExistingClient_ifClientHasBankingProducts_thenStatusActive(List<CreditDto> credits,
                                                                            List<AccountDto> accounts,
                                                                            ClientStatus expectedClientStatus) {
        //ARRANGE
        when(clientRepository.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(creditClient.getActiveCredits(CLIENT_ID)).thenReturn(ResponseEntity.ok(credits));
        when(accountClient.getActiveAccounts(CLIENT_ID)).thenReturn(ResponseEntity.ok(accounts));
        when(userProfileRepository.save(savedUserProfile.capture())).thenAnswer(passedArgument());
        when(passwordEncoder.encode(CLIENT_PASSWORD)).thenReturn(userProfile.getPassword());
        when(passwordEncoder.matches(CLIENT_PASSWORD, userProfile.getPassword())).thenReturn(true);

        //ACT
        registrationService.registerExistingClient(requestClientDto);

        //VERIFY
        assertThat(savedUserProfile.getValue().getClient().getClientStatus()).isEqualTo(expectedClientStatus);
        verifyUserProfileFields(savedUserProfile.getValue());
    }

    private static Stream<Arguments> generateProducts() {
        return Stream.of(Arguments.of(List.of(), List.of(), ClientStatus.NOT_ACTIVE),
                Arguments.of(List.of(new CreditDto()), List.of(), ClientStatus.ACTIVE),
                Arguments.of(List.of(), List.of(new AccountDto()), ClientStatus.ACTIVE),
                Arguments.of(List.of(new CreditDto()), List.of(new AccountDto()), ClientStatus.ACTIVE));
    }

    @Test
    @DisplayName("If user profile was found then throw Registration Exception")
    void registerExistingClient_ifClientNotFound_thenThrow() {
        //ARRANGE
        when(clientRepository.findClientById(any(UUID.class))).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable registerExistingClientMethod = () -> registrationService
                .registerExistingClient(requestClientDto);
        //VERIFY
        assertThatThrownBy(registerExistingClientMethod).isInstanceOf(RegistrationException.class);
    }

    @Test
    @DisplayName("Verify set user profile and client fields")
    void registerNonClient_shouldReturnClientDto() {
        //ARRANGE
        String passportNumber = requestNonClientDto.getPassportNumber();
        when(passportDataRepository.save(any(PassportData.class))).thenAnswer(passedArgument());
        ArgumentCaptor<Client> savedClient = ArgumentCaptor.forClass(Client.class);
        when(clientRepository.save(savedClient.capture())).thenAnswer(passedArgument());
        when(userProfileRepository.save(savedUserProfile.capture())).thenAnswer(passedArgument());
        when(passwordEncoder.encode(CLIENT_PASSWORD)).thenReturn(userProfile.getPassword());
        when(passwordEncoder.matches(CLIENT_PASSWORD, userProfile.getPassword())).thenReturn(true);
        //ACT
        registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        verifyUserProfileFields(savedUserProfile.getValue());
        verifyClientFields(savedClient.getValue());
    }

    @Test
    @DisplayName("If passport data hasn't been saved then throw exception")
    void registerNonClient_ifPassportDataNotSaved_thenThrow() {
        // ARRANGE
        when(passportDataRepository.save(any(PassportData.class))).thenThrow(RuntimeException.class);
        // ACT
        ThrowingCallable registerNonClientMethod = () -> registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        assertThatThrownBy(registerNonClientMethod).isNotNull();
    }

    @Test
    @DisplayName("If client hasn't been saved then throw exception")
    void registerNonClient_ifClientNotSaved_thenThrow() {
        // ARRANGE
        when(clientRepository.save(any(Client.class))).thenThrow(RuntimeException.class);
        // ACT
        ThrowingCallable registerNonClientMethod = () -> registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        assertThatThrownBy(registerNonClientMethod).isNotNull();
    }

    @Test
    @DisplayName("If user profile hasn't been saved then throw exception")
    void registerNonClient_ifUserProfileNotSaved_thenThrow() {
        // ARRANGE
        when(userProfileRepository.save(any(UserProfile.class))).thenThrow(RuntimeException.class);
        // ACT
        ThrowingCallable registerNonClientMethod = () -> registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        assertThatThrownBy(registerNonClientMethod).isNotNull();
    }

    @Test
    @DisplayName("if passport number exist then throw")
    void verifyPassportNumber_shouldThrow() {

        //ARRANGE
        String passportNumber = passportDto.getPassportNumber();
        when(clientRepository.findClientByPassportDataPassportNumber(passportNumber)).thenReturn(Optional.of(client));

        // ACT
        ThrowingCallable verifyPassportNumberMethod = () -> registrationService.verifyPassportNumber(passportDto);

        //VERIFY
        assertThatThrownBy(verifyPassportNumberMethod).isInstanceOf(RegistrationException.class);
    }

    @Test
    @DisplayName("if passport number was not found return ok")
    void verifyPassportNumber_shouldProceed() {

        //ARRANGE
        String passportNumber = passportDto.getPassportNumber();
        when(clientRepository.findClientByPassportDataPassportNumber(passportNumber)).thenReturn(Optional.empty());

        //ACT && VERIFY
        Assertions.assertDoesNotThrow(() -> registrationService.verifyPassportNumber(passportDto));
    }

    private void wayNotClient() {
        client.setClientStatus(ClientStatus.NOT_CLIENT);
    }

    private void wayNotRegistered() {
        client.setClientStatus(ClientStatus.NOT_REGISTERED);
    }

    private void wayRegisteredClient() {
        client.setClientStatus(ClientStatus.ACTIVE);
    }

    private void verifyPhone(ClientDto client) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(client)
                    .withFailMessage("Client shouldn't be null")
                    .isNotNull();
            String notClientMobilePhone = client.getMobilePhone();
            softAssertions.assertThat(notClientMobilePhone)
                    .withFailMessage("Mobile phone should be " + MOBILE_PHONE + " instead of "
                            + notClientMobilePhone)
                    .isEqualTo(MOBILE_PHONE);
        });
    }

    private void verifyClient(ClientDto client) {
        assertSoftly(softAssertions -> {
            UUID id = client.getId();
            softAssertions.assertThat(id)
                    .withFailMessage("Client id shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(id)
                    .withFailMessage("Client id should be " + CLIENT_ID + " instead of " + id)
                    .isEqualTo(CLIENT_ID);
        });
        verifyPhone(client);
    }

    private void verifyUserProfileFields(UserProfile userProfile) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(userProfile.getAppRegistrationDate())
                    .withFailMessage("Application registration date shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(userProfile.getSmsNotification())
                    .withFailMessage("Sms notification should be set to true")
                    .isTrue();
            softAssertions.assertThat(userProfile.getPushNotification())
                    .withFailMessage("Push notification should be set to true")
                    .isTrue();
            softAssertions.assertThat(userProfile.getEmailSubscription())
                    .withFailMessage("Email subscription should be set to false")
                    .isFalse();
            softAssertions.assertThat(passwordEncoder
                            .matches(CLIENT_PASSWORD, userProfile.getPassword()))
                    .withFailMessage("Raw password doesn't match with encoded password")
                    .isTrue();
        });
    }

    private void verifyClientFields(Client client) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(client.getPassportData())
                    .withFailMessage("Passport data should be set")
                    .isNotNull();
            ClientStatus clientStatus = client.getClientStatus();
            softAssertions.assertThat(clientStatus)
                    .withFailMessage(
                            "Client status should be " + ClientStatus.NOT_ACTIVE + " instead of " + clientStatus)
                    .isEqualTo(ClientStatus.NOT_ACTIVE);
            softAssertions.assertThat(client.getAccessionDate())
                    .withFailMessage("Accession date should be set")
                    .isNotNull();
        });
    }

    private Answer<Object> passedArgument() {
        return invocationOnMock -> invocationOnMock.getArgument(0);
    }
}

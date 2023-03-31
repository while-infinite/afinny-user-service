package by.afinny.userservice.integration.service;

import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.dto.credit.CreditDto;
import by.afinny.userservice.dto.deposit.AccountDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.openfeign.credit.CreditClient;
import by.afinny.userservice.openfeign.deposit.AccountClient;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.RegistrationService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for registration service")
public class RegistrationServiceImplIT {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;
    @MockBean
    private CreditClient creditClient;
    @MockBean
    private AccountClient accountClient;

    private final String MOBILE_PHONE = "88005553535";
    private final String MOBILE_PHONE_FOR_REGISTR_NON_CLIENT = "23347620277";
    private final String PASSPORT_NUMBER = "9055187400";
    private final String CLIENT_PASSWORD = "123";

    private Client clientRegistrated;
    private PassportData passportData;
    private RequestClientDto requestClientDto;
    private RequestNonClientDto requestNonClientDto;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder().passportNumber(PASSPORT_NUMBER).build();
        clientRegistrated = Client.builder()
                .id(UUID.randomUUID())
                .firstName("Anna")
                .middleName("Nikolaevna")
                .lastName("Smirnova")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.NOT_REGISTERED)
                .passportData(passportData)
                .build();

        requestNonClientDto = RequestNonClientDto.builder()
                .mobilePhone(MOBILE_PHONE_FOR_REGISTR_NON_CLIENT)
                .password("qwerty123")
                .securityQuestion("Девичья фамилия матери")
                .securityAnswer("Семенова")
                .email("mixawet616@dufeed.com")
                .middleName("Петрович")
                .lastName("Петров")
                .passportNumber(PASSPORT_NUMBER)
                .countryOfResidence(true).build();
        requestClientDto = RequestClientDto.builder()
                .mobilePhone(clientRegistrated.getMobilePhone())
                .password(CLIENT_PASSWORD)
                .securityQuestion("Девичья фамилия матери")
                .securityAnswer("Семенова")
                .email("tst1234@gmail.com")
                .build();
    }

    @BeforeEach
    void save() {
        passportData = passportDataRepository.save(passportData);
        clientRegistrated = clientRepository.save(clientRegistrated);
        requestClientDto.setId(clientRegistrated.getId());
    }

    @Test
    @DisplayName("If client data is correct then save user profile")
    void registerNonClient_shouldSaveClientAndPassportDataAndUserProfile() {
        //ARRANGE
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
        requestNonClientDto.setFirstName("Петр");
        //ACT
        registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        verifyThatClientRegistered(requestNonClientDto.getMobilePhone());
    }

    @Test
    @DisplayName("If client's name is too long then rollback")
    void registerNonClient_ifClientNameTooLong_thenRollbackTransaction() {
        //ARRANGE
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
        requestNonClientDto.setFirstName("EuTuKPqxqeiVENgBrNirAc28T8VTzdoS");
        //ACT
        ThrowingCallable registerNonClientMethod = () -> registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        assertThat(registerNonClientMethod).isNotNull();
        verifyThatClientNotRegistered(requestNonClientDto.getMobilePhone());
    }

    @Test
    @DisplayName("If security data is null then rollback")
    void registerNonClient_ifSecurityDataIsNull_thenRollbackTransaction() {
        //ARRANGE
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
        requestNonClientDto.setSecurityQuestion(null);
        //ACT
        ThrowingCallable registerNonClientMethod = () -> registrationService.registerNonClient(requestNonClientDto);
        //VERIFY
        assertThat(registerNonClientMethod).isNotNull();
        verifyThatClientNotRegistered(requestNonClientDto.getMobilePhone());
    }

    @Test
    @DisplayName("If user profile was found then and client status related on active then save")
    void registerExistingClient_ifClientHasBankingProducts_thenStatusActiveAndSave() {
        //ARRANGE
        when(creditClient.getActiveCredits(clientRegistrated.getId())).thenReturn(ResponseEntity.ok(List.of(new CreditDto())));
        when(accountClient.getActiveAccounts(clientRegistrated.getId())).thenReturn(ResponseEntity.ok(List.of(new AccountDto())));
        //ACT
        registrationService.registerExistingClient(requestClientDto);
        //VERIFY
        verifyThatClientRegistered(requestClientDto.getMobilePhone());

    }

    private void verifyThatClientRegistered(String clientMobilePhone) {
        assertThat(userProfileRepository.findByClientMobilePhone(clientMobilePhone))
                .isPresent();
        assertThat(clientRepository.findClientByMobilePhone(clientMobilePhone))
                .isPresent();
        assertThat(passportDataRepository.findById(requestNonClientDto.getPassportNumber()))
                .isPresent();
    }

    private void verifyThatClientNotRegistered(String clientMobilePhone) {
        assertThat(userProfileRepository.findByClientMobilePhone(clientMobilePhone))
                .isEmpty();
        assertThat(clientRepository.findClientByMobilePhone(clientMobilePhone))
                .isEmpty();
        assertThat(passportDataRepository.findById(requestNonClientDto.getPassportNumber()))
                .isEmpty();
    }
}

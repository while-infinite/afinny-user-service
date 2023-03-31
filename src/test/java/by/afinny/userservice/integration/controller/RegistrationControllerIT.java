package by.afinny.userservice.integration.controller;


import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.controller.RegistrationController;
import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.dto.credit.CreditDto;
import by.afinny.userservice.dto.deposit.AccountDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.AccountExistException;
import by.afinny.userservice.openfeign.credit.CreditClient;
import by.afinny.userservice.openfeign.deposit.AccountClient;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for registration")
public class RegistrationControllerIT {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CreditClient creditClient;
    @MockBean
    private AccountClient accountClient;


    private final String ACTIVE_CLIENT_MOBILE_PHONE = "79023428762";
    private final String NOT_REGISTERED_MOBILE_PHONE = "39487200947";

    private Client activeClient;
    private PassportData activeClientPassportData;
    private Client notRegisteredClient;
    private PassportData notRegisteredClientPassportData;
    private RequestClientDto requestClientDto;
    private RequestNonClientDto requestNonClientDto;


    @BeforeAll
    void setUp() {
        activeClientPassportData = PassportData.builder().passportNumber("5746003821").build();
        activeClient = Client.builder()
                .firstName("Арсений")
                .middleName("Фёдорович")
                .lastName("Остапенко")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2018, 3, 12))
                .mobilePhone(ACTIVE_CLIENT_MOBILE_PHONE)
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(activeClientPassportData)
                .build();

        notRegisteredClientPassportData = PassportData.builder().passportNumber("3087391516").build();
        notRegisteredClient = Client.builder()
                .firstName("Ирина")
                .middleName("Евгеньевна")
                .lastName("Иванова")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2022, 1, 26))
                .mobilePhone(NOT_REGISTERED_MOBILE_PHONE)
                .employerIdentificationNumber("71028739200")
                .clientStatus(ClientStatus.NOT_REGISTERED)
                .passportData(notRegisteredClientPassportData)
                .build();

        requestClientDto = RequestClientDto.builder()
                .mobilePhone(NOT_REGISTERED_MOBILE_PHONE)
                .password("kf^LjD89_JAKuha")
                .email("nasehap466@nifect.com")
                .securityQuestion("Имя лучшего друга детства")
                .securityAnswer("Олег").build();

        requestNonClientDto = RequestNonClientDto.builder()
                .mobilePhone("29830929948")
                .password("hbo10l$99ExQ")
                .securityQuestion("Кличка кошки племянницы")
                .securityAnswer("Персик")
                .email("xomigi2530@iconzap.com")
                .firstName("Николай")
                .lastName("Ильин")
                .middleName("Ильич")
                .passportNumber("0192873390")
                .countryOfResidence(true).build();
    }

    @BeforeEach
    void save() {
        passportDataRepository.save(activeClientPassportData);
        activeClient = clientRepository.save(activeClient);

        passportDataRepository.save(notRegisteredClientPassportData);
        notRegisteredClient = clientRepository.save(notRegisteredClient);
        requestClientDto.setId(notRegisteredClient.getId());
    }

    @Test
    @DisplayName("If client has ACTIVE, NOT_ACTIVE or CLOSED status then return conflict status")
    void verifyMobilePhone_ifClientIsActive_thenReturnConflictStatus() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                        get(RegistrationController.REGISTRATION_URL)
                                .param(RegistrationController.MOBILE_PHONE_PARAMETER, ACTIVE_CLIENT_MOBILE_PHONE))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(AccountExistException.class));
    }

    @Test
    @DisplayName("If client has NOT_REGISTERED status then return id, phone number and client status")
    void verifyMobilePhone_ifClientNotRegistered_thenReturnClientDto() throws Exception {
        //ARRANGE
        Client clientFromDb = clientRepository
                .findClientByMobilePhone(NOT_REGISTERED_MOBILE_PHONE)
                .orElseThrow();
        //ACT
        MvcResult result = mockMvc.perform(
                        get(RegistrationController.REGISTRATION_URL)
                                .param(RegistrationController.MOBILE_PHONE_PARAMETER, NOT_REGISTERED_MOBILE_PHONE))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        ClientDto clientDto = getObjectFromJson(result.getResponse().getContentAsString(), ClientDto.class);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getId()).isEqualTo(clientFromDb.getId());
            softAssertions.assertThat(clientDto.getClientStatus()).isEqualTo(clientFromDb.getClientStatus());
            softAssertions.assertThat(clientDto.getMobilePhone()).isEqualTo(clientFromDb.getMobilePhone());
        });
    }

    @Test
    @DisplayName("If client doesn't exist then return phone number and client status")
    void verifyMobilePhone_ifClientNotExists_thenReturnClientDto() throws Exception {
        //ARRANGE
        String notClientMobilePhone = "49830298376";
        //ACT
        MvcResult result = mockMvc.perform(
                        get(RegistrationController.REGISTRATION_URL)
                                .param(RegistrationController.MOBILE_PHONE_PARAMETER, notClientMobilePhone))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        ClientDto clientDto = getObjectFromJson(result.getResponse().getContentAsString(), ClientDto.class);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getId()).isNull();
            softAssertions.assertThat(clientDto.getClientStatus()).isEqualTo(ClientStatus.NOT_CLIENT);
            softAssertions.assertThat(clientDto.getMobilePhone()).isEqualTo(notClientMobilePhone);
        });
    }

    @ParameterizedTest
    @MethodSource("generateProducts")
    @DisplayName("If client has been successfully registered then return response client dto")
    void registerExistingClient_shouldReturnResponseClientDto(List<CreditDto> credits,
                                                              List<AccountDto> accounts,
                                                              ClientStatus expectedClientStatus)
            throws Exception {
        //ARRANGE
        when(creditClient.getActiveCredits(requestClientDto.getId()))
                .thenReturn(ResponseEntity.ok(credits));
        when(accountClient.getActiveAccounts(requestClientDto.getId()))
                .thenReturn(ResponseEntity.ok(accounts));
        //ACT
        mockMvc.perform(
                        patch(RegistrationController.REGISTRATION_URL + RegistrationController.CLIENT_REGISTRATION_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestClientDto)))
                .andExpect(status().isOk());
        //VERIFY
        UserProfile userProfileFromDb = userProfileRepository
                .findByClientId(requestClientDto.getId())
                .orElseThrow();
        verifyResponseClientDtoFields(requestClientDto, userProfileFromDb);
        assertThat(userProfileFromDb.getClient().getClientStatus()).isEqualTo(expectedClientStatus);
    }

    private Stream<Arguments> generateProducts() {
        return Stream.of(Arguments.of(List.of(), List.of(), ClientStatus.NOT_ACTIVE),
                Arguments.of(List.of(new CreditDto()), List.of(), ClientStatus.ACTIVE),
                Arguments.of(List.of(), List.of(new AccountDto()), ClientStatus.ACTIVE),
                Arguments.of(List.of(new CreditDto()), List.of(new AccountDto()), ClientStatus.ACTIVE));
    }

    @Test
    @DisplayName("If client with this id isn't found then return bad request status")
    void registerExistingClient_ifClientNotFound_thenReturnBadRequest() throws Exception {
        //ARRANGE
        wayWrongClientId();
        //ACT
        mockMvc.perform(
                        patch(RegistrationController.REGISTRATION_URL + RegistrationController.CLIENT_REGISTRATION_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestClientDto)))
                .andExpect(status().isBadRequest());
        //VERIFY
        Optional<UserProfile> userProfileFromDb = userProfileRepository
                .findByClientId(requestClientDto.getId());
        assertThat(userProfileFromDb.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If non-client has been successfully registered then return response non-client dto")
    void registerNonClient_shouldReturnResponseNonClientDto() throws Exception {
        //ACT
        mockMvc.perform(
                        post(RegistrationController.REGISTRATION_URL + RegistrationController.NEW_CLIENT_REGISTRATION_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestNonClientDto)))
                .andExpect(status().isOk());

        //VERIFY
        UserProfile userProfileFromDb = userProfileRepository
                .findByClientMobilePhone(requestNonClientDto.getMobilePhone())
                .orElseThrow();

        assertThat(userProfileFromDb.getClient().getClientStatus()).isEqualTo(ClientStatus.NOT_ACTIVE);
        assertThat(userProfileFromDb.getClient().getId()).isNotNull();
        verifyRequestClientDtoFieldsWithUserFromDb(requestNonClientDto, userProfileFromDb);
    }

    private void verifyResponseClientDtoFields(RequestClientDto requestClientDto, UserProfile userProfileFromDb) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(requestClientDto.getId())
                    .withFailMessage("ID")
                    .isEqualTo(userProfileFromDb.getClient().getId());
            softAssertions.assertThat(requestClientDto.getMobilePhone())
                    .withFailMessage("Mobile Phone")
                    .isEqualTo(userProfileFromDb.getClient().getMobilePhone());
            softAssertions.assertThat(requestClientDto.getSecurityQuestion())
                    .withFailMessage("Security Question")
                    .isEqualTo(userProfileFromDb.getSecurityQuestion());
            softAssertions.assertThat(requestClientDto.getSecurityAnswer())
                    .withFailMessage("Security Answer")
                    .isEqualTo(userProfileFromDb.getSecurityAnswer());
            softAssertions.assertThat(requestClientDto.getEmail())
                    .withFailMessage("Email")
                    .isEqualTo(userProfileFromDb.getEmail());
        });
    }

    private void verifyRequestClientDtoFieldsWithUserFromDb(RequestNonClientDto requestNonClientDto, UserProfile userProfileFromDb) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(requestNonClientDto.getMobilePhone())
                    .withFailMessage("Mobile Phone")
                    .isEqualTo(userProfileFromDb.getClient().getMobilePhone());
            softAssertions.assertThat(requestNonClientDto.getPassportNumber())
                    .isEqualTo(userProfileFromDb.getClient().getPassportData().getPassportNumber());
            softAssertions.assertThat(requestNonClientDto.getSecurityAnswer())
                    .withFailMessage("Security Answer")
                    .isEqualTo(userProfileFromDb.getSecurityAnswer());
            softAssertions.assertThat(requestNonClientDto.getSecurityQuestion())
                    .withFailMessage("Security Question")
                    .isEqualTo(userProfileFromDb.getSecurityQuestion());
            softAssertions.assertThat(requestNonClientDto.getEmail())
                    .withFailMessage("Email")
                    .isEqualTo(userProfileFromDb.getEmail());
            softAssertions.assertThat(requestNonClientDto.getFirstName())
                    .withFailMessage("First Name")
                    .isEqualTo(userProfileFromDb.getClient().getFirstName());
            softAssertions.assertThat(requestNonClientDto.getLastName())
                    .withFailMessage("Last Name")
                    .isEqualTo(userProfileFromDb.getClient().getLastName());
        });

    }

    private void wayWrongClientId() {
        UUID correctClientId = notRegisteredClient.getId();
        Random random = new Random();
        requestClientDto.setId(
                new UUID(correctClientId.getMostSignificantBits() + random.nextInt(),
                        random.nextInt()));
    }

    private String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private <T> T getObjectFromJson(String json, Class<T> objectClass) throws IOException {
        return objectMapper.readValue(json, objectClass);
    }

}


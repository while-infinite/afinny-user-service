package by.afinny.userservice.integration.controller;

import by.afinny.userservice.controller.InformationController;
import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration test for information controller")
public class InformationControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;

    private final String MOBILE_PHONE = "88005553535";
    private final String PASSPORT_NUMBER = "9055187400";
    private final String PASSWORD = "123";
    private final String EMAIL = "email@google.com";
    private final String SECURITY_QUESTION = "123";
    private final String SECURITY_ANSWER = "123";

    private Client client;
    private PassportData passportData;
    private UserProfile userProfile;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder().passportNumber(PASSPORT_NUMBER).build();
        client = Client.builder()
                .firstName("Anna")
                .middleName("Nikolaevna")
                .lastName("Smirnova")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(passportData)
                .build();
        userProfile = UserProfile.builder()
                .smsNotification(true)
                .pushNotification(true)
                .emailSubscription(true)
                .password(PASSWORD)
                .email(EMAIL)
                .securityQuestion(SECURITY_QUESTION)
                .securityAnswer(SECURITY_ANSWER)
                .appRegistrationDate(LocalDate.now())
                .client(client)
                .build();
    }

    @BeforeEach
    void save() {
        passportData = passportDataRepository.save(passportData);
        client = clientRepository.save(client);
        userProfile.setClient(client);
        userProfile = userProfileRepository.save(userProfile);
    }

    @Test
    @DisplayName("If client exists then return client data and status ok")
    void getClientData_shouldReturnClient_andStatusOk() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(get(InformationController.INFORMATION_URL)
                        .param(InformationController.CLIENT_ID_PARAMETER, String.valueOf(client.getId())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If client does not exist then return status BAD REQUEST")
    void getClientData_ifClientNotFound_thenReturnBadRequest() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(get(InformationController.INFORMATION_URL)
                        .param(InformationController.CLIENT_ID_PARAMETER, String.valueOf(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }
}

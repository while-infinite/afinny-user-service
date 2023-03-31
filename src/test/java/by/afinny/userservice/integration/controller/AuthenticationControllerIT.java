package by.afinny.userservice.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.controller.AuthenticationController;
import by.afinny.userservice.dto.LoginDto;
import by.afinny.userservice.entity.AuthenticationType;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.stream.Stream;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for authentication")
public class AuthenticationControllerIT {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private final String MOBILE_PHONE = "23347620277";
    private final String PASSPORT_NUMBER = "9055187400";
    private final String STORED_PASSWORD = "hon_MNEYT95";

    private LoginDto loginDto;
    private Client client;
    private PassportData passportData;
    private UserProfile userProfile;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder().passportNumber(PASSPORT_NUMBER).build();
        client = Client.builder()
                .firstName("Анна")
                .middleName("Николаевна")
                .lastName("Смирнова")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(passportData)
                .build();
        userProfile = UserProfile.builder()
                .smsNotification(false)
                .pushNotification(false)
                .emailSubscription(false)
                .password(encoder.encode(STORED_PASSWORD))
                .email("mixawet616@dufeed.com")
                .securityQuestion("Любимое блюдо")
                .securityAnswer("Паста карбонара")
                .appRegistrationDate(LocalDate.now())
                .build();
    }

    @BeforeEach
    void save() {
        passportDataRepository.save(passportData);
        client = clientRepository.save(client);
        userProfile.setClient(client);
        userProfile = userProfileRepository.save(userProfile);
        client.setId(userProfile.getClient().getId());
    }

    @ParameterizedTest
    @MethodSource("generateLoginAndType")
    @DisplayName("If credentials are incorrect then return unauthorized status")
    void authenticateUser_ifUserNotExists_thenReturnUnauthorized(String login, AuthenticationType type) throws Exception {
        //ARRANGE
        createLoginDto(login, type, "qwerty000");
        //ACT & VERIFY
        mockMvc.perform(MockMvcRequestBuilders.post(AuthenticationController.AUTHENTICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("generateLoginAndType")
    @DisplayName("If credentials are correct then return client id")
    void authenticateUser_ifUserExists_thenReturnOk(String login, AuthenticationType type) throws Exception {
        //ARRANGE
        createLoginDto(login, type, STORED_PASSWORD);
        //ACT
        MvcResult result = mockMvc.perform(post(AuthenticationController.AUTHENTICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        assertThat(result.getResponse().getContentAsString()).isEqualTo(asJsonString(client.getId()));
    }

    private Stream<Arguments> generateLoginAndType() {
        return Stream.of(
                Arguments.of(MOBILE_PHONE, AuthenticationType.PHONE_NUMBER),
                Arguments.of(PASSPORT_NUMBER, AuthenticationType.PASSPORT_NUMBER));
    }

    @Test
    @DisplayName("Set new password for user with this phone number")
    void setNewPassword_shouldReturnOkStatus() throws Exception {
        //ARRANGE
        String newPassword = "ehQ*yTI*TB";
        //ACT
        mockMvc.perform(patch(AuthenticationController.AUTHENTICATION_URL + AuthenticationController.URL_PASSWORD)
                        .param(AuthenticationController.MOBILE_PHONE_PARAM, MOBILE_PHONE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword))
                .andExpect(status().isOk());
        //VERIFY
        UserProfile userProfileInDb = userProfileRepository
                .findByClientMobilePhone(MOBILE_PHONE)
                .orElseThrow();
        assertThat(encoder.matches(newPassword, userProfileInDb.getPassword())).isTrue();
    }

    @Test
    @DisplayName("If user with this phone number doesn't exist then return unauthorized")
    void setNewPassword_ifUserNotExists_thenReturnUnauthorizedStatus() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(patch(AuthenticationController.AUTHENTICATION_URL + AuthenticationController.URL_PASSWORD)
                        .param(AuthenticationController.MOBILE_PHONE_PARAM, "79893458827")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("ehQ*yTI*TB"))
                .andExpect(status().isBadRequest());
    }

    private void createLoginDto(String login, AuthenticationType type, String password) {
        loginDto = LoginDto.builder()
                .login(login)
                .type(type)
                .password(password).build();
    }

    private String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}

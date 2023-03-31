package by.afinny.userservice.integration.controller;

import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.controller.UserController;
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
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/truncate.sql")
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for user")
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ObjectMapper objectMapper;

    private final String STORED_PASSWORD = "qwerty123";
    private final String MOBILE_PHONE = "79024786639";
    private final String PASSPORT_NUMBER = "0190783622";

    private UUID clientId;
    private UserProfile userProfile;
    private PassportData passportData;
    private Client client;
    private PasswordDto passwordDto;
    private SecurityDto securityDto;
    private NotificationChangerDto notificationChangerDto;
    private ChangingEmailDto changingEmailDto;
    private MobilePhoneDto mobilePhoneDto;

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
        passwordDto = PasswordDto.builder()
                .newPassword("ZEb6bC%=n9_rY4d").build();
        securityDto = SecurityDto.builder()
                .securityQuestion("Имя лучшего друга детства")
                .securityAnswer("Алиса").build();
        notificationChangerDto = NotificationChangerDto.builder()
                .notificationStatus(true).build();
        changingEmailDto = ChangingEmailDto.builder()
                .newEmail("xomigi2530@iconzap.com").build();
        mobilePhoneDto = MobilePhoneDto.builder()
                .mobilePhone("8666412211").build();
    }

    @BeforeEach
    void save() {
        passportDataRepository.save(passportData);
        client = clientRepository.save(client);
        userProfile.setClient(client);
        userProfile = userProfileRepository.save(userProfile);
        clientId = userProfile.getClient().getId();
        client.setId(clientId);

    }

    @Test
    @DisplayName("If entered password equals to stored then update password and don't return content")
    void changePassword_shouldNotReturnContent() throws Exception {
        //ARRANGE
        wayPasswordsEqual();
        //ACT
        mockMvc.perform(patch(UserController.USER_URL + UserController.CHANGE_PASSWORD_URL)
                        .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(passwordDto)))
                .andExpect(status().isOk());
        //VERIFY
        UserProfile userProfileFromDb = getUserProfileFromDb();
        assertThat(encoder.matches(passwordDto.getNewPassword(), userProfileFromDb.getPassword()))
                .isTrue();
    }

    @Test
    @DisplayName("If entered password doesn't equal to stored then return bad request status")
    void changePassword_ifPasswordsNotEqual_thenReturnBadRequestStatus() throws Exception {
        //ARRANGE
        wayPasswordsDifferent();
        //ACT
        mockMvc.perform(patch(UserController.USER_URL + UserController.CHANGE_PASSWORD_URL)
                        .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(passwordDto)))
                .andExpect(status().isBadRequest());
        //VERIFY
        UserProfile userProfileFromDb = getUserProfileFromDb();
        assertThat(encoder.matches(passwordDto.getNewPassword(), userProfileFromDb.getPassword()))
                .isFalse();
        assertThat(userProfileFromDb.getPassword()).isEqualTo(userProfile.getPassword());
    }

    @Test
    @DisplayName("If successfully changed security data then don't return content")
    void changeSecurityData_shouldNotReturnContent() throws Exception {
        //ACT
        mockMvc.perform(patch(UserController.USER_URL + UserController.CHANGE_SECURITY_DATA_URL)
                        .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(securityDto)))
                .andExpect(status().isOk());
        //VERIFY]
        UserProfile userProfileFromDb = getUserProfileFromDb();
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(userProfileFromDb.getSecurityQuestion())
                    .isEqualTo(securityDto.getSecurityQuestion());
            softAssertions.assertThat(userProfileFromDb.getSecurityAnswer())
                    .isEqualTo(securityDto.getSecurityAnswer());
        });
    }

    @Test
    @DisplayName("Should return SMS-, PUSH-, EMAIL- notifications and email")
    void getNotifications_shouldReturnNotifications() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(
                        get(UserController.USER_URL + UserController.GET_NOTIFICATION_SETTINGS_URL)
                                .param(UserController.CLIENT_ID_PARAM, clientId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        NotificationDto notificationDto = getObjectFromJson(
                result.getResponse().getContentAsString(),
                NotificationDto.class);
        UserProfile userProfileFromDb = getUserProfileFromDb();
        verifyNotifications(notificationDto, userProfileFromDb);
    }

    @Test
    @DisplayName("Should update client SMS-notification setting")
    void changeSmsNotifications_shouldNotReturnContent() throws Exception {
        //ARRANGE
        Boolean smsNotificationBeforeUpdating = userProfile.getSmsNotification();
        //ACT
        mockMvc.perform(
                        patch(UserController.USER_URL + UserController.CHANGE_SMS_SETTINGS_URL)
                                .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(notificationChangerDto)))
                .andExpect(status().isOk());
        //VERIFY
        Boolean smsNotificationAfterUpdating = getUserProfileFromDb().getSmsNotification();
        assertThat(smsNotificationAfterUpdating)
                .isEqualTo(notificationChangerDto.getNotificationStatus());
        assertThat(smsNotificationAfterUpdating)
                .isNotEqualTo(smsNotificationBeforeUpdating);
    }

    @Test
    @DisplayName("Should update client PUSH-notification setting")
    void changePushNotifications_shouldNotReturnContent() throws Exception {
        //ARRANGE
        Boolean pushNotificationBeforeUpdating = userProfile.getPushNotification();
        //ACT
        mockMvc.perform(
                        patch(UserController.USER_URL + UserController.CHANGE_PUSH_SETTINGS_URL)
                                .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(notificationChangerDto)))
                .andExpect(status().isOk());
        //VERIFY
        Boolean pushNotificationAfterUpdating = getUserProfileFromDb().getPushNotification();
        assertThat(pushNotificationAfterUpdating)
                .isEqualTo(notificationChangerDto.getNotificationStatus());
        assertThat(pushNotificationAfterUpdating)
                .isNotEqualTo(pushNotificationBeforeUpdating);
    }

    @Test
    @DisplayName("Should update client EMAIL-subscription setting")
    void changeEmailSubscription_shouldNotReturnContent() throws Exception {
        //ARRANGE
        Boolean emailSubscriptionBeforeUpdating = userProfile.getEmailSubscription();
        //ACT
        mockMvc.perform(
                        patch(UserController.USER_URL + UserController.CHANGE_EMAIL_SETTINGS_URL)
                                .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(notificationChangerDto)))
                .andExpect(status().isOk());
        //VERIFY
        Boolean emailSubscriptionAfterUpdating = getUserProfileFromDb().getEmailSubscription();
        assertThat(emailSubscriptionAfterUpdating)
                .isEqualTo(notificationChangerDto.getNotificationStatus());
        assertThat(emailSubscriptionAfterUpdating)
                .isNotEqualTo(emailSubscriptionBeforeUpdating);
    }

    @Test
    @DisplayName("update email to new")
    void changeEmail_shouldNotReturnContent() throws Exception {
        //ACT
        mockMvc.perform(
                        patch(UserController.USER_URL + UserController.CHANGE_EMAIL_URL)
                                .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(changingEmailDto)))
                .andExpect(status().isOk());
        //VERIFY
        UserProfile userProfileFromDb = getUserProfileFromDb();
        assertThat(userProfileFromDb.getEmail()).isEqualTo(changingEmailDto.getNewEmail());
    }

    @Test
    @DisplayName("update mobile phone to new")
    void changeMobilePhone_shouldNotReturnContent() throws Exception {
        //ACT
        mockMvc.perform(
                        patch(UserController.USER_URL + UserController.CHANGE_MOBILE_PHONE_URL)
                                .param(UserController.CLIENT_ID_PARAM, clientId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(mobilePhoneDto)))
                .andExpect(status().isOk());
        //VERIFY
        Client clientFromDb = getClientFromDb();
        assertThat(clientFromDb.getMobilePhone()).isEqualTo(mobilePhoneDto.getMobilePhone());
    }

    @NotNull
    private UserProfile getUserProfileFromDb() {
        return userProfileRepository
                .findByClientId(clientId)
                .orElseThrow();
    }

    @NotNull
    private Client getClientFromDb() {
        return clientRepository
                .findClientById(clientId)
                .orElseThrow();
    }

    private void verifyNotifications(NotificationDto notificationDto, UserProfile userProfileFromDb) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(userProfileFromDb.getSmsNotification())
                    .isEqualTo(notificationDto.getSmsNotification());
            softAssertions.assertThat(userProfileFromDb.getPushNotification())
                    .isEqualTo(notificationDto.getPushNotification());
            softAssertions.assertThat(userProfileFromDb.getEmailSubscription())
                    .isEqualTo(notificationDto.getEmailSubscription());
            softAssertions.assertThat(userProfileFromDb.getEmail())
                    .isEqualTo(notificationDto.getEmail());
        });
    }

    private void wayPasswordsEqual() {
        passwordDto.setPassword(STORED_PASSWORD);
    }

    private void wayPasswordsDifferent() {
        passwordDto.setPassword("");
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private <T> T getObjectFromJson(String json, Class<T> objectClass) throws IOException {
        return objectMapper.readValue(json, objectClass);
    }
}

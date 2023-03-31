package by.afinny.userservice.unit.controller;

import by.afinny.userservice.controller.UserController;
import by.afinny.userservice.dto.ChangingEmailDto;
import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.NotificationChangerDto;
import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.dto.PasswordDto;
import by.afinny.userservice.dto.SecurityDto;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @MockBean
    private UserService userService;

    private final UUID CLIENT_ID = UUID.fromString("7a17188d-bf9d-4e9a-86a3-e59cd5962881");

    private PasswordDto passwordDto;
    private SecurityDto securityDto;
    private NotificationChangerDto notificationChangerDto;
    private NotificationDto notificationDto;
    private ChangingEmailDto changingEmailDto;
    private MobilePhoneDto mobilePhoneDto;
    private MockMvc mockMvc;

    @BeforeAll
    public void setUp() {
        passwordDto = PasswordDto.builder()
            .password("{tSLj)6>{-X8gS9")
            .newPassword("K?J:jg&t89!fBGAp").build();

        securityDto = SecurityDto.builder()
            .securityQuestion("Любимый вид спорта")
            .securityAnswer("Пляжный волейбол").build();

        notificationChangerDto = NotificationChangerDto.builder()
            .notificationStatus(true).build();

        notificationDto = NotificationDto.builder()
            .smsNotification(false)
            .pushNotification(false)
            .emailSubscription(true)
            .email("kejebar122@doerma.com").build();

        changingEmailDto = ChangingEmailDto.builder()
            .newEmail("raniwaf191@cupbest.com").build();

        mobilePhoneDto = MobilePhoneDto.builder()
                .mobilePhone("86664122111").build();

        mockMvc = standaloneSetup(new UserController(userService))
            .setControllerAdvice(ExceptionHandlerController.class).build();
    }

    @Test
    @DisplayName("If successfully changed password then don't return content")
    void changePassword_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/password")
                    .param("clientId", CLIENT_ID.toString())
                    .contentType("application/json")
                    .content(asJsonString(passwordDto)))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If password changing has been failed then return BAD REQUEST")
    void changePassword_ifChangingFailed_thenReturnBadRequest() throws Exception {
        //ARRANGE
        doThrow(new RegistrationException("400", "Incorrect password")).when(userService)
                .changePassword(any(PasswordDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/password")
                    .param("clientId", String.valueOf(CLIENT_ID))
                    .contentType("application/json")
                    .content(asJsonString(passwordDto)))
            .andExpect(status().isBadRequest())
            .andReturn();
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If successfully changed security data then don't return content")
    void changeSecurityData_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/controls")
                    .param("clientId", CLIENT_ID.toString())
                    .contentType("application/json")
                    .content(asJsonString(securityDto)))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If successfully changed security data then return INTERNAL SERVER ERROR")
    void changeSecurityData_ifChangingFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(userService)
            .changeSecurityData(any(SecurityDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/controls")
                    .param("clientId", CLIENT_ID.toString())
                    .contentType("application/json")
                    .content(asJsonString(securityDto)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If notifications were successfully got then return notifications")
    void getNotifications_shouldReturnNotifications() throws Exception {
        //ARRANGE
        when(userService.getNotifications(CLIENT_ID)).thenReturn(notificationDto);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/user/settings/notifications/all")
                    .param("clientId", CLIENT_ID.toString()))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
        verifyBody(asJsonString(notificationDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If notifications getting has been failed then return INTERNAL SERVER ERROR")
    void getNotifications_ifGettingFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        when(userService.getNotifications(CLIENT_ID)).thenThrow(RuntimeException.class);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/user/settings/notifications/all")
                    .param("clientId", String.valueOf(CLIENT_ID)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If successfully changed SMS-notifications then don't return content")
    void changeSmsNotification_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/notifications/sms")
                    .param("clientId", CLIENT_ID.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(notificationChangerDto)))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If SMS-notifications changing has been failed then return INTERNAL SERVER ERROR")
    void changeSmsNotification_ifChangingFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(userService)
            .changeSmsNotification(any(NotificationChangerDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/notifications/sms")
                    .param("clientId", CLIENT_ID.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(notificationChangerDto)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If successfully changed PUSH-notifications then don't return content")
    void changePushNotification_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/notifications/push")
                    .param("clientId", String.valueOf(CLIENT_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(notificationChangerDto)))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If PUSH-notifications changing has been failed then return INTERNAL SERVER ERROR")
    void changePushNotification_ifChangingFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(userService)
            .changePushNotification(any(NotificationChangerDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/notifications/push")
                    .param("clientId", CLIENT_ID.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(notificationChangerDto)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If successfully changed EMAIL-subscription then don't return content")
    void changeEmailSubscription_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/notifications/email")
                .param("clientId", CLIENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notificationChangerDto)))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If EMAIL-subscription changing has been failed then return INTERNAL SERVER ERROR")
    void changeEmailSubscription_ifChangingFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(userService)
            .changeEmailSubscription(any(NotificationChangerDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/notifications/email")
                .param("clientId", CLIENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notificationChangerDto)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If email successfully changed then don't return content")
    void changeEmail_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/email")
                .param("clientId", CLIENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(changingEmailDto)))
            .andExpect(status().isOk())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If email changing failed then return INTERNAL SERVER ERROR")
    void changeEmail_ifChangingFailed_thenThrow() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(userService)
            .changeEmail(any(ChangingEmailDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/email")
                .param("clientId", CLIENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(changingEmailDto)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If mobile phone successfully changed then don't return content")
    void changeMobilePhone_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/phone")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(mobilePhoneDto)))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    @Test
    @DisplayName("If phone changing failed then return INTERNAL SERVER ERROR")
    void changeMobilePhone_ifChangingFailed_thenThrow() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(userService)
                .changeMobilePhone(any(MobilePhoneDto.class), eq(CLIENT_ID));
        //ACT
        MvcResult result = mockMvc.perform(patch("/auth/user/settings/phone")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(mobilePhoneDto)))
                .andExpect(status().isInternalServerError())
                .andReturn();
        //VERIFY
        verifyClientIdParameter(result.getRequest().getParameter("clientId"));
    }

    private void verifyClientIdParameter(String clientId) {
        UUID clientIdParameter = UUID.fromString(clientId);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientIdParameter)
                .withFailMessage("Client id parameter should be set")
                .isNotNull();
            softAssertions.assertThat(clientIdParameter)
                .withFailMessage("Client id parameter should be " + CLIENT_ID + " instead of " + clientIdParameter)
                .isEqualTo(CLIENT_ID);
        });
    }

    private String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}

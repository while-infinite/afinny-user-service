package by.afinny.userservice.unit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import by.afinny.userservice.controller.AuthenticationController;
import by.afinny.userservice.dto.LoginByPinDto;
import by.afinny.userservice.dto.LoginDto;
import by.afinny.userservice.exception.BadCredentialsException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AuthenticationController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationControllerTest {

    @MockBean
    private AuthenticationService authenticationService;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String MOBILE_PHONE = "79182546634";
    private final String NEW_PASSWORD = "ofhrnn0_12";
    private final UUID USER_ID = UUID.fromString("725764ce-e246-11ec-8fea-0242ac120002");

    private MockMvc mockMvc;
    private LoginDto loginDto;
    private LoginByPinDto loginByPinDto;

    @BeforeAll
    public void setUp() {
        mockMvc = standaloneSetup(new AuthenticationController(authenticationService))
                .setControllerAdvice(ExceptionHandlerController.class).build();

        loginDto = LoginDto.builder()
                .login("+79182546634")
                .password("koeksxj22k!").build();

        loginByPinDto = LoginByPinDto.builder()
                .fingerprint("FINGERPRINT")
                .clientId(USER_ID)
                .build();
    }

    @Test
    @DisplayName("If user authenticated successfully then return client id and status OK")
    void authenticateUser_shouldReturnClientId() throws Exception {
        //ARRANGE
        when(authenticationService.getCredentials(any(LoginDto.class))).thenReturn(CLIENT_ID);
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(post(AuthenticationController.AUTHENTICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(CLIENT_ID), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If authentication failed then return status UNAUTHORIZED")
    void authenticateUser_ifAuthenticationFailed_thenReturnStatusUnauthorized() throws Exception {
        //ARRANGE
        BadCredentialsException badCredentialsException = new BadCredentialsException("401", "Invalid Password");
        when(authenticationService.getCredentials(any(LoginDto.class))).thenThrow(badCredentialsException);
        //ACT & VERIFY
        mockMvc.perform(post(AuthenticationController.AUTHENTICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("If password successfully updated then return status OK")
    void setNewPassword_shouldNotReturnContent() throws Exception {
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(patch(AuthenticationController.AUTHENTICATION_URL + AuthenticationController.URL_PASSWORD)
                        .param("mobilePhone", MOBILE_PHONE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        verifyMobilePhoneParameter(result.getRequest().getParameter("mobilePhone"));
    }

    @Test
    @DisplayName("If reset failed then return status BAD REQUEST")
    void setNewPassword_ifResetFailed_thenReturnStatusBAD_REQUEST() throws Exception {
        //ARRANGE
        RegistrationException registrationException = new RegistrationException("400", "Bad Request");
        doThrow(registrationException).when(authenticationService)
                .resetPasswordByMobilePhone(eq(MOBILE_PHONE), anyString());
        //ACT & VERIFY
        mockMvc.perform(patch(AuthenticationController.AUTHENTICATION_URL + AuthenticationController.URL_PASSWORD)
                        .param("mobilePhone", MOBILE_PHONE)
                        .content(NEW_PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If user authenticated successfully then return client id and status OK")
    void authenticateUserByPin_shouldReturnClientId() throws Exception {
        //ARRANGE
        when(authenticationService.checkFingerprintForLoginById(any(LoginByPinDto.class))).thenReturn(USER_ID);
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(post(AuthenticationController.AUTHENTICATION_URL + AuthenticationController.URL_PIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginByPinDto)))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(USER_ID), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If authentication failed then return status UNAUTHORIZED")
    void authenticateUserByPin_ifAuthenticationFailed_thenReturnStatusUnauthorized() throws Exception {
        //ARRANGE
        BadCredentialsException badCredentialsException = new BadCredentialsException("401", "Invalid Password");
        when(authenticationService.checkFingerprintForLoginById(any(LoginByPinDto.class))).thenThrow(badCredentialsException);
        //ACT & VERIFY
        mockMvc.perform(post(AuthenticationController.AUTHENTICATION_URL + AuthenticationController.URL_PIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginByPinDto)))
                .andExpect(status().isUnauthorized());
    }

    private void verifyMobilePhoneParameter(String mobilePhone) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(mobilePhone)
                    .withFailMessage("Mobile phone parameter should be set")
                    .isNotNull();
            softAssertions.assertThat(mobilePhone)
                    .withFailMessage("Mobile phone parameter should be " + MOBILE_PHONE + " instead of " + mobilePhone)
                    .isEqualTo(MOBILE_PHONE);
        });
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}

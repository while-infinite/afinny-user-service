package by.afinny.userservice.unit.controller;

import by.afinny.userservice.controller.VerificationController;
import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.VerificationDto;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.VerificationCodeException;
import by.afinny.userservice.exception.dto.VerificationErrorDto;
import by.afinny.userservice.service.VerificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(VerificationController.class)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VerificationService verificationService;

    private final String MOBILE_PHONE = "79024327692";
    private final String EMAIL = "mdn1@fed.com";
    private final String VERIFICATION_CODE = "398019";
    private final String PASSPORT_NUMBER = "2239963256";

    private VerificationDto verificationDto;
    private MobilePhoneDto mobilePhoneDto;
    private PassportDto passportDto;

    @BeforeAll
    void setUp() {
        verificationDto = VerificationDto.builder()
                .verificationCode(VERIFICATION_CODE).build();

        mobilePhoneDto = MobilePhoneDto.builder()
                .mobilePhone(MOBILE_PHONE).build();

        passportDto = PassportDto.builder()
                .passportNumber(PASSPORT_NUMBER).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {MOBILE_PHONE, EMAIL})
    @DisplayName("If verification code has been successfully sent then don't return content")
    void sendVerificationCode_shouldNotReturnContent(String receiver) throws Exception {
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(
                        patch("/security/session")
                                .param("receiver", receiver))
                .andExpect(status().isOk())
                .andReturn();
        verifyReceiverParameter(receiver, result.getRequest().getParameter("receiver"));
    }

    @ParameterizedTest
    @ValueSource(strings = {MOBILE_PHONE, EMAIL})
    @DisplayName("If verification code sending failed then return INTERNAL SERVER ERROR")
    void sendVerificationCode_ifSendingFailed_thenReturnInternalServerError(String receiver) throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(verificationService).createAndSendVerificationCode(receiver);
        //ACT & VERIFY
        mockMvc.perform(
                        patch("/security/session")
                                .param("receiver", receiver))
                .andExpect(status().isInternalServerError());
    }

    @ParameterizedTest
    @ValueSource(strings = {MOBILE_PHONE, EMAIL})
    @DisplayName("If verification code successfully checked then don't return content")
    void checkVerificationCode_shouldNotReturnContent(String receiver) throws Exception {
        //ACT & VERIFY
        verificationDto.setMobilePhone(receiver);
        MvcResult result = mockMvc.perform(post("/security/session/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationDto)))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(verificationDto), result.getRequest().getContentAsString());
    }

    @ParameterizedTest
    @ValueSource(strings = {MOBILE_PHONE, EMAIL})
    @DisplayName("If verification failed then return BAD REQUEST")
    void checkVerificationCode_ifVerificationCodeInvalid_thenThrow(String receiver) throws Exception {
        //ARRANGE
        verificationDto.setMobilePhone(receiver);
        VerificationCodeException verificationCodeException = new VerificationCodeException
                (Integer.toString(HttpStatus.BAD_REQUEST.value()), "Verification code is invalid!");

        doThrow(verificationCodeException).when(verificationService)
                .checkVerificationCode(any(VerificationDto.class));
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(post("/security/session/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationDto)))
                .andExpect(status().isBadRequest()).andReturn();
        VerificationErrorDto verificationErrorDto = new VerificationErrorDto(verificationCodeException.getErrorMessage());
        verifyBody(asJsonString(verificationErrorDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If timestamp successfully set then don't return content")
    void setUserBlockTimestamp_shouldNotReturnContent() throws Exception {
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(patch("/security/session/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(mobilePhoneDto)))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(mobilePhoneDto), result.getRequest().getContentAsString());
    }

    @Test
    @DisplayName("If setting has been failed then return INTERNAL SERVER ERROR")
    void setUserBlockTimestamp_ifSettingFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(verificationService)
                .setUserBlockTimestamp(any(MobilePhoneDto.class));
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(patch("/security/session/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(mobilePhoneDto)))
                .andExpect(status().isInternalServerError())
                .andReturn();
        verifyBody(asJsonString(mobilePhoneDto), result.getRequest().getContentAsString());
    }

    @Test
    @DisplayName("If successfully then return mobile phone")
    void findMobilePhoneByPassport_shouldReturnMobilePhone() throws Exception {
        //ARRANGE
        when(verificationService.getMobilePhone(any(PassportDto.class))).thenReturn(mobilePhoneDto);

        //ACT & VERIFY
        MvcResult result = mockMvc.perform(post("/security/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(passportDto)))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(mobilePhoneDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If wasn't successfully then return status Bad request")
    void findMobilePhoneByPassport_ifNotSuccess_thenReturnBadRequest() throws Exception {
        //ARRANGE
        doThrow(EntityNotFoundException.class).when(verificationService).getMobilePhone(any(PassportDto.class));

        //ACT & VERIFY
        mockMvc.perform(post("/security/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(passportDto)))
                .andExpect(status().isBadRequest());
    }

    private void verifyReceiverParameter(String expectedReceiver, String actualReceiver) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actualReceiver)
                    .withFailMessage("Receiver parameter should be set")
                    .isNotNull();
            softAssertions.assertThat(actualReceiver)
                    .withFailMessage("Receiver parameter should be " + expectedReceiver + " instead of "
                            + actualReceiver)
                    .isEqualTo(expectedReceiver);
        });
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }
}

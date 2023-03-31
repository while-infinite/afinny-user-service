package by.afinny.userservice.integration.controller;

import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.controller.VerificationController;
import by.afinny.userservice.dto.VerificationDto;
import by.afinny.userservice.entity.Verification;
import by.afinny.userservice.exception.VerificationCodeException;
import by.afinny.userservice.repository.SmsBlockSendingRepository;
import by.afinny.userservice.repository.VerificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.data.TemporalUnitLessThanOffset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for verification")
public class VerificationControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private VerificationRepository verificationRepository;
    @Autowired
    private SmsBlockSendingRepository smsBlockSendingRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private final String MOBILE_PHONE = "79024327692";
    private final String CORRECT_VERIFICATION_CODE = "914023";
    private final LocalDateTime NOT_EXPIRED_TIME = LocalDateTime.now().plusMinutes(15);
    private final LocalDateTime EXPIRED_TIME = LocalDateTime.now().minusMinutes(15);

    private Verification verification;
    private VerificationDto verificationDto;

    @BeforeAll
    void setUp() {
        verification = Verification.builder()
                .mobilePhone(MOBILE_PHONE)
                .wrongAttemptsCounter(0)
                .verificationCode(CORRECT_VERIFICATION_CODE).build();

        verificationDto = VerificationDto.builder()
                .mobilePhone(MOBILE_PHONE).build();
    }

    @Test
    @DisplayName("If verification code has been successfully sent then don't return content")
    void sendVerificationCode_shouldNotReturnContent() throws Exception {
        //ACT
        mockMvc.perform(
                        patch(VerificationController.VERIFICATION_URL)
                                .param(VerificationController.RECEIVER_PARAM, MOBILE_PHONE))
                .andExpect(status().isOk()).andReturn();
        //VERIFY
        Verification verificationFromDb = verificationRepository
                .findByMobilePhone(MOBILE_PHONE)
                .orElseThrow();
        assertThat(verificationFromDb.getMobilePhone()).isEqualTo(MOBILE_PHONE);
    }

    @ParameterizedTest
    @MethodSource("generateBlockAndCodeExpirations")
    @DisplayName("If there is resending then update code and block expirations, code and don't return content")
    void sendVerificationCode_ifResending_thenUpdateExpirationAndCodeAndNotReturnContent(
            LocalDateTime codeExpiration,
            LocalDateTime blockExpiration) throws Exception {
        //ARRANGE
        verification.setCodeExpiration(codeExpiration);
        verification.setUserBlockExpiration(blockExpiration);
        Verification previousVerification = verificationRepository.save(verification);
        //ACT
        mockMvc.perform(
                        patch(VerificationController.VERIFICATION_URL)
                                .param(VerificationController.RECEIVER_PARAM, MOBILE_PHONE))
                .andExpect(status().isOk());
        //VERIFY
        Verification verificationFromDb = verificationRepository
                .findByMobilePhone(MOBILE_PHONE)
                .orElseThrow();

        verifyVerification(previousVerification, verificationFromDb);
    }

    private Stream<Arguments> generateBlockAndCodeExpirations() {
        return Stream.of(
                Arguments.of(
                        Named.of("Not expired", NOT_EXPIRED_TIME),
                        Named.of("Not expired", NOT_EXPIRED_TIME)),
                Arguments.of(
                        Named.of("Not expired", NOT_EXPIRED_TIME),
                        Named.of("Expired", EXPIRED_TIME)),
                Arguments.of(
                        Named.of("Expired", EXPIRED_TIME),
                        Named.of("Not expired", NOT_EXPIRED_TIME)),
                Arguments.of(
                        Named.of("Expired", EXPIRED_TIME),
                        Named.of("Expired", EXPIRED_TIME)));
    }

    @Test
    @DisplayName("If verification code is correct and not expired then don't return content")
    void checkVerificationCode_ifCodeIsCorrectAndNotExpired_thenNotReturnContent() throws Exception {
        //ARRANGE
        verification.setCodeExpiration(NOT_EXPIRED_TIME);
        verificationRepository.save(verification);

        verificationDto.setVerificationCode(CORRECT_VERIFICATION_CODE);
        //ACT
        mockMvc.perform(post(VerificationController.VERIFICATION_URL + VerificationController.CHECK_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationDto)))
                .andExpect(status().isOk());
        //VERIFY
        assertThat(verificationRepository.findByMobilePhone(MOBILE_PHONE)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource({"generateCodeAndExpiration"})
    @DisplayName("If verification code is incorrect or expired then return bad request status")
    void checkVerificationCode_ifCodeIsInvalid_thenReturnBadRequestStatus(
            String verificationCode,
            LocalDateTime codeExpiration) throws Exception {
        //ARRANGE
        verification.setCodeExpiration(codeExpiration);
        verificationRepository.save(verification);

        verificationDto.setVerificationCode(verificationCode);
        //ACT
        mockMvc.perform(post(VerificationController.VERIFICATION_URL + VerificationController.CHECK_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(VerificationCodeException.class));
        //VERIFY
        assertThat(verificationRepository
                .findByMobilePhone(MOBILE_PHONE))
                .isPresent();
    }

    @ParameterizedTest
    @MethodSource("generateCodeAndExpiration")
    @DisplayName("If receiver is blocked then return not acceptable status")
    void checkVerificationCode_ifReceiverBlocked_thenReturnNotAcceptableStatus(String verificationCode,
                                                                               LocalDateTime codeExpiration) throws Exception {
        //ARRANGE
        verification.setCodeExpiration(codeExpiration);
        verification.setUserBlockExpiration(NOT_EXPIRED_TIME);
        verificationRepository.save(verification);
        verificationDto.setVerificationCode(verificationCode);

        //ACT
        mockMvc.perform(post(VerificationController.VERIFICATION_URL + VerificationController.CHECK_VERIFICATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verificationDto)))
                .andExpect(status().isNotAcceptable());

        //VERIFY
        assertThat(verificationRepository
                .findByMobilePhone(MOBILE_PHONE))
                .isPresent();
    }

    private Stream<Arguments> generateCodeAndExpiration() {
        return Stream.of(
                Arguments.of(
                        Named.of("Correct", CORRECT_VERIFICATION_CODE),
                        Named.of("Expired", EXPIRED_TIME)),
                Arguments.of(
                        Named.of("Incorrect", "000000"),
                        Named.of("Not expired", NOT_EXPIRED_TIME)),
                Arguments.of(
                        Named.of("Incorrect", "000000"),
                        Named.of("Expired", EXPIRED_TIME)));
    }

    private void verifyVerification(Verification previousVerification, Verification verificationFromDb) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(verificationFromDb.getVerificationCode())
                    .isNotEqualTo(previousVerification.getVerificationCode());
            softAssertions.assertThat(verificationFromDb.getCodeExpiration())
                    .isAfter(previousVerification.getCodeExpiration());
            softAssertions.assertThat(verificationFromDb.getUserBlockExpiration())
                    .isCloseTo(previousVerification.getUserBlockExpiration(),
                            new TemporalUnitLessThanOffset(1, ChronoUnit.MILLIS));
        });
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}

package by.afinny.userservice.integration.service;

import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.dto.VerificationDto;
import by.afinny.userservice.entity.Verification;
import by.afinny.userservice.exception.BlockedReceiverException;
import by.afinny.userservice.repository.VerificationRepository;
import by.afinny.userservice.service.SenderService;
import by.afinny.userservice.service.VerificationService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for verification")
public class VerificationServiceImplIT {

    @Autowired
    private VerificationService verificationService;
    @Autowired
    private VerificationRepository verificationRepository;
    @MockBean
    private SenderService senderService;

    private final String RECEIVER = "79508987236";

    @Test
    @DisplayName("Should create and save verification")
    void createAndSendVerificationCode_shouldSaveVerification() {
        //ACT
        verificationService.createAndSendVerificationCode(RECEIVER);
        //VERIFY
        assertThat(verificationRepository.findByMobilePhone(RECEIVER)).isPresent();
    }

    @Test
    @DisplayName("If receiver is too long then rollback transaction")
    void createAndSendVerificationCode_ifReceiverTooLong_thenRollback() {
        String incorrectReceiver = "79033298030239843948220112986343481023";
        //ACT
        ThrowingCallable createAndSaveVerificationCodeMethod =
                () -> verificationService.createAndSendVerificationCode(incorrectReceiver);
        //VERIFY
        assertThat(createAndSaveVerificationCodeMethod).isNotNull();
        assertThat(verificationRepository.findByMobilePhone(RECEIVER)).isEmpty();
    }

    @Test
    @DisplayName("If sender service has failure then rollback transaction and don't save anything")
    void createAndSendVerificationCode_ifSendingFailed_thenRollback() {
        //ARRANGE
        doThrow(RuntimeException.class)
                .when(senderService)
                .sendMessageToMobilePhone(eq(RECEIVER), anyString());
        //ACT
        ThrowingCallable createAndSendVerificationCodeMethod =
                () -> verificationService.createAndSendVerificationCode(RECEIVER);
        //VERIFY
        assertThat(createAndSendVerificationCodeMethod).isNotNull();
        assertThat(verificationRepository.findByMobilePhone(RECEIVER)).isEmpty();
    }

    @Test
    @DisplayName("If receiver is blocked then throw exception and return left block seconds")
    void checkVerificationCode_ifReceiverIsBlocked_thenReturnLeftSecondsCount() {
        //ARRANGE
        String verificationCode = "493882";
        createAndSaveVerification(verificationCode);
        VerificationDto verificationDto = VerificationDto.builder()
                .mobilePhone(RECEIVER)
                .verificationCode(verificationCode).build();
        //ACT
        ThrowingCallable checkVerificationCodeMethod =
                () -> verificationService.checkVerificationCode(verificationDto);
        //VERIFY
        assertThatThrownBy(checkVerificationCodeMethod)
                .isInstanceOf(BlockedReceiverException.class)
                .hasFieldOrProperty(BlockedReceiverException.BLOCK_SECONDS_FIELD);
    }

    private void createAndSaveVerification(String verificationCode) {
        Verification sentVerification = Verification.builder()
                .mobilePhone(RECEIVER)
                .verificationCode(verificationCode)
                .codeExpiration(LocalDateTime.now().plusMinutes(15))
                .userBlockExpiration(LocalDateTime.now().plusMinutes(15))
                .wrongAttemptsCounter(0)
                .userBlockExpiration(LocalDateTime.now().plusMinutes(20)).build();
        verificationRepository.save(sentVerification);
    }
}

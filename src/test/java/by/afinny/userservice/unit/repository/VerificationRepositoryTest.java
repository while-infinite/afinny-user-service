package by.afinny.userservice.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import by.afinny.userservice.entity.Verification;
import by.afinny.userservice.exception.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import by.afinny.userservice.repository.VerificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Sql(
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
public class VerificationRepositoryTest {

    @Autowired
    private VerificationRepository verificationRepository;

    private final String MOBILE_PHONE = "23422262398";

    private Verification verificationByMobilePhone;

    @BeforeAll
    void setUp() {
        verificationByMobilePhone = Verification.builder()
            .mobilePhone(MOBILE_PHONE)
            .verificationCode("287100")
            .codeExpiration(LocalDateTime.now().plusMinutes(10))
                .userBlockExpiration(LocalDateTime.now().plusMinutes(15))
                .wrongAttemptsCounter(0)
                .userBlockExpiration(LocalDateTime.now().plusMinutes(20))
            .build();
    }

    @AfterEach
    void cleanUp() {
        verificationRepository.deleteAll();
    }

    @Test
    @DisplayName("If verification by mobile phone exists then return verification")
    void findByReceiverAndVerificationCodeAndType_ifByMobilePhone_thenReturnVerification() {
        //ARRANGE
        verificationRepository.save(verificationByMobilePhone);
        //ACT
        Verification foundVerification = verificationRepository
                .findByMobilePhone(MOBILE_PHONE)
                .orElseThrow(() -> new EntityNotFoundException("Verification wasn't found"));
        //VERIFY
        verifyVerification(foundVerification);
    }

    @Test
    @DisplayName("If verification by mobile phone with this mobile phone and code doesn't found then return empty")
    void findByReceiverAndVerificationCodeAndType_ifNotFoundByMobilePhone_thenEmpty() {
        //ARRANGE
        verificationRepository.save(verificationByMobilePhone);
        //ACT
        Optional<Verification> verification = verificationRepository
                .findByMobilePhone("79618739417");
        //VERIFY
        assertThat(verification.isEmpty()).isTrue();
    }

        private void verifyVerification(Verification verification) {
            assertSoftly(softAssertions -> {
            softAssertions.assertThat(verification.getMobilePhone()).isEqualTo("23422262398");
            softAssertions.assertThat(verification.getCodeExpiration()).isNotNull();
        });
    }
}

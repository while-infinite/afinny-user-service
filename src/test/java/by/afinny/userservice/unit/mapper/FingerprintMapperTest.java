package by.afinny.userservice.unit.mapper;

import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.entity.Fingerprint;
import by.afinny.userservice.mapper.FingerprintMapperImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class FingerprintMapperTest {

    @InjectMocks
    private FingerprintMapperImpl fingerprintMapper;

    private FingerprintDto fingerprintDto;
    private Fingerprint fingerprint;

    @BeforeAll
    void setUp() {
        fingerprintDto = FingerprintDto.builder()
                .clientId(UUID.randomUUID())
                .fingerprint("fingerprint")
                .build();
    }

    @Test
    @DisplayName("Verify fingerprint dto fields setting")
    void toFingerprint_shouldReturnFingerprint() {
        //ACT
        fingerprint = fingerprintMapper.toFingerprint(fingerprintDto);
        //VERIFY
        verifyBody(fingerprint);
    }

    private void verifyBody(Fingerprint fingerprint) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(fingerprint.getFingerprint()).isEqualTo(fingerprintDto.getFingerprint());
            softAssertions.assertThat(fingerprint.getClient().getId()).isEqualTo(fingerprintDto.getClientId());
        });
    }
}
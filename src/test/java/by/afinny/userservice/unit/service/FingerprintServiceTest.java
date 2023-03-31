package by.afinny.userservice.unit.service;

import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.entity.Fingerprint;
import by.afinny.userservice.mapper.FingerprintMapper;
import by.afinny.userservice.mapper.FingerprintMapperImpl;
import by.afinny.userservice.repository.FingerprintRepository;
import by.afinny.userservice.service.impl.FingerprintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class FingerprintServiceTest {

    @InjectMocks
    private FingerprintServiceImpl fingerprintService;
    @Mock
    private FingerprintRepository fingerprintRepository;

    @Spy
    private FingerprintMapper fingerprintMapper = new FingerprintMapperImpl();

    private final String FINGERPRINT =  "FINGERPRINT";

    private FingerprintDto fingerprintDto;
    private Fingerprint fingerprint;

    @BeforeEach
    void setUp() {
        fingerprintDto = FingerprintDto.builder()
                .fingerprint(FINGERPRINT)
                .clientId(UUID.randomUUID())
                .build();

        fingerprint = Fingerprint.builder()
                .id(UUID.fromString("c3ed67fc-e246-11ec-8fea-0242ac120002"))
                .fingerprint(FINGERPRINT)
                .build();
    }

    @Test
    @DisplayName("if received fingerprint successfully then save fingerprint")
    void createFingerprint_shouldSave() {
        //ACT
        fingerprintService.createFingerprint(fingerprintDto);
        //VERIFY
        verify(fingerprintRepository, times(1)).save(any(Fingerprint.class));
    }

    @Test
    @DisplayName("if fingerprint already exists then throw")
    void createFingerprint_ifFingerprintExists_thenThrow() {
        //ARRANGE
        when(fingerprintRepository.findByClientIdAndFingerprint(fingerprintDto.getClientId(),fingerprintDto.getFingerprint()))
                .thenReturn(Optional.of(fingerprint));
        //ACT
        fingerprintService.createFingerprint(fingerprintDto);
        //VERIFY
        verify(fingerprintRepository, never()).save(any(Fingerprint.class));
    }
}
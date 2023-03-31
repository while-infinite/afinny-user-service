package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.entity.Fingerprint;
import by.afinny.userservice.mapper.FingerprintMapper;
import by.afinny.userservice.repository.FingerprintRepository;
import by.afinny.userservice.service.FingerprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FingerprintServiceImpl implements FingerprintService {

    private final FingerprintRepository fingerprintRepository;
    private final FingerprintMapper fingerprintMapper;

    @Override
    public void createFingerprint(FingerprintDto fingerprintDto) {
        log.info("createFingerprint() invoked");
        if (verifyFingerprint(fingerprintDto)) {
            Fingerprint fingerprint = fingerprintMapper.toFingerprint(fingerprintDto);
            fingerprintRepository.save(fingerprint);
        }
    }

    private boolean verifyFingerprint(FingerprintDto fingerprintDto) {
        return fingerprintRepository.findByClientIdAndFingerprint(fingerprintDto.getClientId(),
                fingerprintDto.getFingerprint()).isEmpty();
    }
}

package by.afinny.userservice.controller;

import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.service.FingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("fingerprint")
public class FingerprintController {

    public static final String URL_FINGERPRINT = "/fingerprint";

    private final FingerprintService fingerprintService;

    @PostMapping()
    public ResponseEntity<Void> createFingerprint(@RequestBody FingerprintDto fingerprintDto) {
        fingerprintService.createFingerprint(fingerprintDto);
        return ResponseEntity.ok().build();
    }
}

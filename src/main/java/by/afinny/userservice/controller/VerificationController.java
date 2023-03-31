package by.afinny.userservice.controller;

import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.SmsBlockExpirationDto;
import by.afinny.userservice.dto.VerificationDto;
import by.afinny.userservice.service.VerificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("security/session")
public class VerificationController {

    public static final String VERIFICATION_URL = "/security/session";
    public static final String CHECK_VERIFICATION_URL = "/verification";
    public static final String RECEIVER_PARAM = "receiver";

    private final VerificationService verificationService;

    @PatchMapping
    public ResponseEntity<SmsBlockExpirationDto> sendVerificationCode(@RequestParam String receiver) {

        SmsBlockExpirationDto smsBlockExpirationDto = verificationService.createAndSendVerificationCode(receiver);
        return ResponseEntity.ok(smsBlockExpirationDto);
    }

    @PostMapping("verification")
    public ResponseEntity<Void> checkVerificationCode(@RequestBody VerificationDto verificationDto) {

        verificationService.checkVerificationCode(verificationDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("verification")
    public ResponseEntity<Void> setUserBlockTimestamp(@RequestBody MobilePhoneDto mobilePhone) {

        verificationService.setUserBlockTimestamp(mobilePhone);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<MobilePhoneDto> findMobilePhoneByPassport(@RequestBody PassportDto passportNumber) {

        MobilePhoneDto mobilePhone = verificationService.getMobilePhone(passportNumber);
        return ResponseEntity.ok(mobilePhone);
    }
}

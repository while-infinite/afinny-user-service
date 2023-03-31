package by.afinny.userservice.controller;

import by.afinny.userservice.dto.LoginByPinDto;
import by.afinny.userservice.dto.LoginDto;
import by.afinny.userservice.service.AuthenticationService;
import java.util.UUID;
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
@RequestMapping("login")
public class AuthenticationController {

    public static final String AUTHENTICATION_URL = "/login";
    public static final String URL_PASSWORD = "/password";
    public static final String URL_PIN = "/pin";
    public static final String MOBILE_PHONE_PARAM = "mobilePhone";

    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<UUID> authenticateUser(@RequestBody LoginDto loginDto) {
        UUID clientId = authenticationService.getCredentials(loginDto);
        return ResponseEntity.ok(clientId);
    }

    @PatchMapping("password")
    public ResponseEntity<Void> setNewPassword(@RequestParam(name = "mobilePhone") String mobilePhone,
                                               @RequestBody String newPassword) {
        authenticationService.resetPasswordByMobilePhone(mobilePhone, newPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("pin")
    public ResponseEntity<UUID> authenticateUserByPin(@RequestBody LoginByPinDto loginByPinDto) {
        UUID userId = authenticationService.checkFingerprintForLoginById(loginByPinDto);
        return ResponseEntity.ok(userId);
    }
}

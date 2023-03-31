package by.afinny.userservice.controller;

import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("registration")
@RequiredArgsConstructor
@Validated
public class RegistrationController {

    public static final String REGISTRATION_URL = "/registration";
    public static final String CLIENT_REGISTRATION_URL = "/user-profile";
    public static final String NEW_CLIENT_REGISTRATION_URL = "/user-profile/new";
    public static final String MOBILE_PHONE_PARAMETER = "mobilePhone";

    private final RegistrationService registrationService;

    @GetMapping
    public ResponseEntity<ClientDto> verifyMobilePhone(@RequestParam String mobilePhone) {

        ClientDto result = registrationService.verifyMobilePhone(mobilePhone);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("user-profile")
    public ResponseEntity<Void> registerExistingClient(@RequestBody RequestClientDto registeringUser) {

        registrationService.registerExistingClient(registeringUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("user-profile/new")
    public ResponseEntity<Void> registerNonClient(@RequestBody RequestNonClientDto requestNonClientDto) {

        registrationService.registerNonClient(requestNonClientDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("user-profile/verification")
    public ResponseEntity<Void> verifyPassportNumber(@RequestBody PassportDto passportNumber) {

        registrationService.verifyPassportNumber(passportNumber);
        return ResponseEntity.ok().build();
    }
}


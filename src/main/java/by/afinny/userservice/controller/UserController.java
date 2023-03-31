package by.afinny.userservice.controller;

import by.afinny.userservice.dto.ChangingEmailDto;
import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.NotificationChangerDto;
import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.dto.PasswordDto;
import by.afinny.userservice.dto.SecurityDto;
import by.afinny.userservice.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("auth/user/settings")
@RequiredArgsConstructor
public class UserController {

    public final static String USER_URL = "/auth/user/settings";
    public final static String CHANGE_PASSWORD_URL = "/password";
    public final static String CHANGE_SECURITY_DATA_URL = "/controls";
    public final static String GET_NOTIFICATION_SETTINGS_URL = "/notifications/all";
    public final static String CHANGE_SMS_SETTINGS_URL = "/notifications/sms";
    public final static String CHANGE_PUSH_SETTINGS_URL = "/notifications/push";
    public final static String CHANGE_EMAIL_SETTINGS_URL = "/notifications/email";
    public final static String CHANGE_EMAIL_URL = "/email";
    public final static String CLIENT_ID_PARAM = "clientId";
    public final static String CHANGE_MOBILE_PHONE_URL = "/phone";

    private final UserService userService;

    @PatchMapping("password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordDto passwordDto,
        @RequestParam UUID clientId) {

        userService.changePassword(passwordDto, clientId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("controls")
    public ResponseEntity<Void> changeSecurityData(@RequestBody SecurityDto securityDto,
        @RequestParam UUID clientId) {

        userService.changeSecurityData(securityDto, clientId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("notifications/all")
    public ResponseEntity<NotificationDto> getNotificationSettings(@RequestParam UUID clientId) {

        NotificationDto result = userService.getNotifications(clientId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("notifications/sms")
    public ResponseEntity<Void> changeSmsNotificationSettings(@RequestBody NotificationChangerDto notificationChangerDTO,
        @RequestParam UUID clientId) {

        userService.changeSmsNotification(notificationChangerDTO, clientId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("notifications/push")
    public ResponseEntity<Void> changePushNotificationSettings(@RequestBody NotificationChangerDto notificationChangerDTO,
        @RequestParam UUID clientId) {

        userService.changePushNotification(notificationChangerDTO, clientId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("notifications/email")
    public ResponseEntity<Void> changeEmailSubscription(@RequestBody NotificationChangerDto notificationChangerDTO,
        @RequestParam UUID clientId) {

        userService.changeEmailSubscription(notificationChangerDTO, clientId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("email")
    public ResponseEntity<Void> changeEmail(@RequestBody ChangingEmailDto changingEmailDto,
        @RequestParam UUID clientId) {

        userService.changeEmail(changingEmailDto, clientId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("phone")
    public ResponseEntity<Void> changeMobilePhone(@RequestBody @Valid MobilePhoneDto mobilePhoneDto,
                                                  @RequestParam UUID clientId) {

        userService.changeMobilePhone(mobilePhoneDto, clientId);
        return ResponseEntity.ok().build();
    }
}


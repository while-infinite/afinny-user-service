package by.afinny.userservice.service;

import by.afinny.userservice.dto.ChangingEmailDto;
import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.NotificationChangerDto;
import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.dto.PasswordDto;
import by.afinny.userservice.dto.SecurityDto;
import java.util.UUID;

public interface UserService {

    void changePassword(PasswordDto passwordDto, UUID clientId);

    void changeSecurityData(SecurityDto securityDto, UUID clientId);

    NotificationDto getNotifications(UUID clientId);

    void changeEmailSubscription(NotificationChangerDto notificationChangerDTO, UUID clientId);

    void changeSmsNotification(NotificationChangerDto notificationChangerDTO, UUID clientId);

    void changePushNotification(NotificationChangerDto notificationChangerDTO, UUID clientId);

    void changeEmail(ChangingEmailDto changingEmailDto, UUID clientId);

    void modifyEmployerId(UUID clientId, String employerId);

    void changeMobilePhone(MobilePhoneDto mobilePhoneDto, UUID clientId);
}

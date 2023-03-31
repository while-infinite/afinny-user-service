package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.ChangingEmailDto;
import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.NotificationChangerDto;
import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.dto.PasswordDto;
import by.afinny.userservice.dto.SecurityDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.mapper.NotificationMapper;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.AuthenticationService;
import by.afinny.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final AuthenticationService authenticationService;
    private final UserProfileRepository userProfileRepository;
    private final ClientRepository clientRepository;
    private final NotificationMapper notificationMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(PasswordDto passwords, UUID clientId) {
        log.info("change password() is invoked");
        UserProfile userProfile = getVerifiedUserByClientId(clientId);

        ensurePasswordsAreEqual(passwords.getPassword(), userProfile.getPassword());
        String newPassword = passwords.getNewPassword();
        authenticationService.resetPasswordByUserProfile(userProfile, newPassword);
    }

    @Override
    public void changeSecurityData(SecurityDto securityDto, UUID clientId) {
        log.info("changeSecurityData() is invoked");

        UserProfile userProfile = getVerifiedUserByClientId(clientId);
        userProfile.setSecurityQuestion(securityDto.getSecurityQuestion());
        userProfile.setSecurityAnswer(securityDto.getSecurityAnswer());

        userProfileRepository.save(userProfile);
    }

    @Override
    public NotificationDto getNotifications(UUID clientId) {
        log.info("getNotifications() method is invoked");
        UserProfile userProfile = getVerifiedUserByClientId(clientId);

        return notificationMapper.userProfileToNotificationDto(userProfile);
    }

    @Override
    public void changeSmsNotification(NotificationChangerDto notificationChangerDto, UUID clientId) {
        log.info("changeSmsNotification() method is invoked");
        UserProfile userProfile = getVerifiedUserByClientId(clientId);
        Boolean newNotification = notificationChangerDto.getNotificationStatus();
        if (newNotification == null) {
            throw new EntityNotFoundException("Invalid notification status");
        }
        userProfile.setSmsNotification(newNotification);
        userProfileRepository.save(userProfile);
    }

    @Override
    public void changePushNotification(NotificationChangerDto notificationChangerDto, UUID clientId) {
        log.info("changePushNotification() method is invoked");
        UserProfile userProfile = getVerifiedUserByClientId(clientId);
        Boolean newNotification = notificationChangerDto.getNotificationStatus();
        if (newNotification == null) {
            throw new EntityNotFoundException("Invalid notification status");
        }
        userProfile.setPushNotification(newNotification);
        userProfileRepository.save(userProfile);
    }

    @Override
    public void changeEmailSubscription(NotificationChangerDto notificationChangerDTO, UUID clientId) {
        log.info("setEmailNotification() method is invoked");
        UserProfile userProfile = getVerifiedUserByClientId(clientId);
        boolean newEmailNotificationStatus = notificationChangerDTO.getNotificationStatus();
        userProfile.setEmailSubscription(newEmailNotificationStatus);
        userProfileRepository.save(userProfile);
    }

    @Override
    public void changeEmail(ChangingEmailDto changingEmailDto, UUID clientId) {
        log.info("updateEmail() method is invoked");
        UserProfile userProfile = getVerifiedUserByClientId(clientId);

        String newEmail = changingEmailDto.getNewEmail();
        userProfile.setEmail(newEmail);
        userProfileRepository.save(userProfile);
    }

    @Override
    public void modifyEmployerId(UUID clientId, String employerId) {
        log.info("ModifyEmployerId() method is invoked");
        Client client = getVerifiedClientById(clientId);
        String storedEmployerId = client.getEmployerIdentificationNumber();

        if (areEmployerIdentificationNumbersDifferent(employerId, storedEmployerId)) {
            log.info("Updating employer identification number from " + storedEmployerId + " to " + employerId);
            client.setEmployerIdentificationNumber(employerId);
            clientRepository.save(client);
        } else {
            log.info("Employer identification numbers are equals");
        }
    }

    @Override
    @Transactional
    public void changeMobilePhone(MobilePhoneDto mobilePhoneDto, UUID clientId) {
        log.info("changeMobilePhone() method is invoked");
        Client client = getVerifiedClientById(clientId);

        String newMobilePhone = mobilePhoneDto.getMobilePhone();
        client.setMobilePhone(newMobilePhone);
        clientRepository.save(client);
    }

    private boolean areEmployerIdentificationNumbersDifferent(String employerId, String storedEmployerId) {
        return storedEmployerId == null || !storedEmployerId.equals(employerId);
    }

    private UserProfile getVerifiedUserByClientId(UUID clientId) {
        return userProfileRepository.findByClientId(clientId)
            .orElseThrow(() -> new EntityNotFoundException("User profile is not found with id: " + clientId));
    }

    private Client getVerifiedClientById(UUID id) {
        return clientRepository.findClientById(id)
            .orElseThrow(() -> new EntityNotFoundException("Client is not found with id: " + id));
    }

    private void ensurePasswordsAreEqual(String incomingPassword, String storedPassword) {
        if (!passwordEncoder.matches(incomingPassword, storedPassword)) {
            log.debug(incomingPassword + " doesn't match with " + storedPassword);
            throw new RegistrationException(
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "Incorrect password"
            );
        }
    }
}

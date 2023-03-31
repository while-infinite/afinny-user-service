package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.LoginByPinDto;
import by.afinny.userservice.dto.LoginDto;
import by.afinny.userservice.entity.AuthenticationType;
import by.afinny.userservice.entity.Fingerprint;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.BadCredentialsException;
import by.afinny.userservice.exception.WrongCredentialsException;
import by.afinny.userservice.repository.FingerprintRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserProfileRepository userProfileRepository;
    private final FingerprintRepository fingerprintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UUID getCredentials(LoginDto dto) {
        log.info("getCredentials() invoked");
        String login = dto.getLogin();
        String password = dto.getPassword();
        AuthenticationType authenticationType = dto.getType();
        UserProfile userProfile;

        if (isMobilePhone(authenticationType)) {
            log.debug("login identified as mobile phone");
            login = getMobilePhone(login);
            userProfile = getVerifiedUserByMobilePhone(login);
        } else {
            log.debug("Login identified as passport number");
            userProfile = getVerifiedUserByPassportNumber(login);
        }

        if (!isPasswordEquals(password, userProfile.getPassword())) {
            log.debug("Password aren't equals");
            throw new WrongCredentialsException();
        }
        return userProfile.getClient().getId();
    }

    @Override
    public void resetPasswordByMobilePhone(String mobilePhone, String newPassword) {
        log.info("resetPasswordByMobilePhone() invoked");
        UserProfile foundUserProfile = getVerifiedUserByMobilePhone(mobilePhone);
        resetPasswordByUserProfile(foundUserProfile, newPassword);
    }

    @Override
    public void resetPasswordByUserProfile(UserProfile userProfile, String newPassword) {
        log.info("resetPasswordByUserProfile() invoked");
        String newPasswordEncoded = encodePassword(newPassword);
        log.info("Encoded password: " + newPasswordEncoded);

        userProfile.setPassword(newPasswordEncoded);
        userProfileRepository.save(userProfile);
    }

    @Override
    public UUID checkFingerprintForLoginById(LoginByPinDto loginByPinDto) {
        log.info("getCredentialsByPin() invoked");
        Fingerprint fingerprint = getFingerprintByClientId(loginByPinDto.getClientId(), loginByPinDto.getFingerprint());
        return fingerprint.getClient().getId();
    }

    private UserProfile getVerifiedUserByMobilePhone(String mobilePhone) {
        return userProfileRepository.findByClientMobilePhone(mobilePhone)
                .orElseThrow(WrongCredentialsException::new);
    }

    private UserProfile getVerifiedUserByPassportNumber(String passportNumber) {
        return userProfileRepository.findByClientPassportDataPassportNumber(passportNumber)
                .orElseThrow(WrongCredentialsException::new);
    }

    private boolean isPasswordEquals(String password, String storedPassword) {
        return passwordEncoder.matches(password, storedPassword);
    }

    private String getMobilePhone(String login) {
        return login.replace("+", "");
    }

    private boolean isMobilePhone(AuthenticationType authenticationType) {
        return authenticationType.equals(AuthenticationType.PHONE_NUMBER);
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private Fingerprint getFingerprintByClientId(UUID clientId, String fingerprint) {
        return fingerprintRepository.findByClientIdAndFingerprint(clientId, fingerprint)
                .orElseThrow(() -> new BadCredentialsException(
                        Integer.toString(HttpStatus.BAD_REQUEST.value()),
                        "Incorrect fingerprint"));
    }
}

package by.afinny.userservice.service;

import by.afinny.userservice.dto.LoginByPinDto;
import by.afinny.userservice.dto.LoginDto;
import by.afinny.userservice.entity.UserProfile;

import java.util.UUID;

public interface AuthenticationService {

    UUID getCredentials(LoginDto dto);

    void resetPasswordByMobilePhone(String mobilePhone, String newPassword);

    void resetPasswordByUserProfile(UserProfile userProfile, String newPassword);

    UUID checkFingerprintForLoginById(LoginByPinDto loginByPinDto);
}

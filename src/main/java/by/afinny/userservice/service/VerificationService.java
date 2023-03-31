package by.afinny.userservice.service;

import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.SmsBlockExpirationDto;
import by.afinny.userservice.dto.VerificationDto;

public interface VerificationService {

    SmsBlockExpirationDto createAndSendVerificationCode(String receiver);

    void checkVerificationCode(VerificationDto verificationDto);

    void setUserBlockTimestamp(MobilePhoneDto mobilePhone);

    MobilePhoneDto getMobilePhone(PassportDto passportNumber);
}


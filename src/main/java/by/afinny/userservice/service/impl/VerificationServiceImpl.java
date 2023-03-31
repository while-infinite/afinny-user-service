package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.SmsBlockExpirationDto;
import by.afinny.userservice.dto.VerificationDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.SmsBlockSending;
import by.afinny.userservice.entity.Verification;
import by.afinny.userservice.exception.BlockedReceiverException;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.VerificationCodeException;
import by.afinny.userservice.mapper.MobilePhoneMapper;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.SmsBlockSendingRepository;
import by.afinny.userservice.repository.VerificationRepository;
import by.afinny.userservice.service.SenderService;
import by.afinny.userservice.service.VerificationService;
import by.afinny.userservice.util.VerificationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final SenderService senderService;
    private final VerificationRepository verificationRepository;
    private final SmsBlockSendingRepository smsBlockSendingRepository;
    private final ClientRepository clientRepository;
    private final MobilePhoneMapper mobilePhoneMapper;

    private final static int CODE_EXPIRATION_IN_MINUTES = 15;
    private final static int BLOCK_EXPIRATION_IN_MINUTES = 10;
    private final static int MAX_WRONG_ATTEMPTS_COUNTER = 3;
    private final static int MAX_SEND_ATTEMPTS_COUNTER = 5;
    private final static int BLOCK_TIME_IN_SECONDS = 600;

    @Transactional
    @Override
    public SmsBlockExpirationDto createAndSendVerificationCode(String receiver) {
        log.info("createAndSendVerificationCode invoked()");
        SmsBlockSending smsBlockSending = smsBlockSendingRepository.findById(receiver)
                .map(this::setSmsBlockExpiration)
                .orElseGet(() -> createSmsBlockSending(receiver));
        Verification verification = verificationRepository.findByMobilePhone(receiver)
                .map(this::setVerificationCodeAndExpiration)
                .orElseGet(() -> createVerification(receiver));
        sendVerification(verification);
        Long smsBlockExpiration = VerificationUtil.calculateRemainingSeconds(smsBlockSending.getSmsBlockExpiration());
        return new SmsBlockExpirationDto(Long.toString(smsBlockExpiration),verification.getVerificationCode());
    }

    @Transactional(noRollbackFor = {BlockedReceiverException.class, VerificationCodeException.class})
    @Override
    public void checkVerificationCode(VerificationDto verificationDto) {
        Verification sentVerification = getVerificationByReceiver(verificationDto.getMobilePhone());
        verifyReceiverNotBlocked(sentVerification.getUserBlockExpiration());
        checkWrongAttemptsCounter(sentVerification);
        ensureThatCodeIsValid(verificationDto.getVerificationCode(), sentVerification);
        ensureThatCodeIsExpire(sentVerification);
        verificationRepository.delete(sentVerification);
    }

    @Transactional
    @Override
    public void setUserBlockTimestamp(MobilePhoneDto mobilePhone) {
        log.info("setUserBlockTimestamp() invoked");
        Verification verification = getVerificationByReceiver(mobilePhone.getMobilePhone());
        verification.setUserBlockExpiration(LocalDateTime.now().plusMinutes(BLOCK_EXPIRATION_IN_MINUTES));
        verificationRepository.save(verification);
    }

    @Override
    public MobilePhoneDto getMobilePhone(PassportDto passportNumber) {
        log.info("getMobilePhone() method invoke");
        Client client = getClientByPassportNumber(passportNumber.getPassportNumber());
        return mobilePhoneMapper.toMobilePhoneDto(client);
    }

    private Verification createVerification(String receiver) {
        Verification verification = Verification.builder()
                .mobilePhone(receiver)
                .wrongAttemptsCounter(0)
                .build();
        return setVerificationCodeAndExpiration(verification);
    }

    private SmsBlockSending createSmsBlockSending(String receiver) {
        SmsBlockSending smsBlockSending = SmsBlockSending.builder()
                .mobilePhone(receiver)
                .sendingCount(0)
                .build();
        return setSmsBlockExpiration(smsBlockSending);
    }

    private Verification setVerificationCodeAndExpiration(Verification verification) {
        verification.setVerificationCode(VerificationUtil.generateVerificationCode());
        verification.setCodeExpiration(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_IN_MINUTES));
        return verificationRepository.save(verification);
    }

    private SmsBlockSending setSmsBlockExpiration(SmsBlockSending smsBlockSending) {
        verifyReceiverNotBlocked(smsBlockSending.getSmsBlockExpiration());
        Integer sendingCount = smsBlockSending.getSendingCount();
        LocalDateTime smsBlockExpiration = sendingCount < MAX_SEND_ATTEMPTS_COUNTER ?
                VerificationUtil.generateSmsBlockExpiration(sendingCount) : LocalDateTime.now().plusSeconds(BLOCK_TIME_IN_SECONDS);
        smsBlockSending.setSendingCount(++sendingCount);
        smsBlockSending.setSmsBlockExpiration(smsBlockExpiration);
        return smsBlockSendingRepository.save(smsBlockSending);
    }

    private void sendVerification(Verification verification) {
        String message = VerificationUtil.createVerificationMessage(verification.getVerificationCode());
        senderService.sendMessageToMobilePhone(verification.getMobilePhone(), message);
    }

    private void verifyReceiverNotBlocked(LocalDateTime blockExpiration) {
        long remainingBlockSeconds = VerificationUtil.calculateRemainingSeconds(blockExpiration);
        if (VerificationUtil.isRemain(remainingBlockSeconds)) {
            throw new BlockedReceiverException(remainingBlockSeconds);
        }
    }

    private void ensureThatCodeIsValid(String verificationCode, Verification sentVerification) {
        if (VerificationUtil.isCodeInvalid(verificationCode, sentVerification.getVerificationCode())) {
            Integer wrongAttemptsCounter = sentVerification.getWrongAttemptsCounter() + 1;
            sentVerification.setWrongAttemptsCounter(wrongAttemptsCounter);
            verificationRepository.save(sentVerification);
            throw new VerificationCodeException(Integer.toString(HttpStatus.BAD_REQUEST.value()),
                   "Verification code is invalid!");
        }
    }

    private void ensureThatCodeIsExpire(Verification sentVerification) {
        if (VerificationUtil.isExpire(sentVerification.getCodeExpiration())) {
            throw new VerificationCodeException(Integer.toString(HttpStatus.BAD_REQUEST.value()),
                    "Verification code expired!");
        }
    }

    private void checkWrongAttemptsCounter(Verification sentVerification) {
        int wrongAttemptsCounter = sentVerification.getWrongAttemptsCounter() + 1;
        if (wrongAttemptsCounter > MAX_WRONG_ATTEMPTS_COUNTER) {
            LocalDateTime userBlockExpiration = VerificationUtil.generateUserBlockExpiration();
            long remainingSeconds = VerificationUtil.calculateRemainingSeconds(userBlockExpiration);
            sentVerification.setUserBlockExpiration(userBlockExpiration);
            sentVerification.setWrongAttemptsCounter(0);
            verificationRepository.save(sentVerification);
            throw new BlockedReceiverException(remainingSeconds);
        }
    }

    private Verification getVerificationByReceiver(String receiver) {
        log.info("getVerificationByReceiver() invoked");
        return verificationRepository
                .findByMobilePhone(receiver)
                .orElseThrow(() -> new EntityNotFoundException("There is no code sent to " + receiver));
    }

    private Client getClientByPassportNumber(String passportNumber) {
        return clientRepository.findClientByPassportDataPassportNumber(passportNumber)
                .orElseThrow(() -> new EntityNotFoundException("Incorrect number"));
    }
}
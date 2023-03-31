package by.afinny.userservice.unit.service;

import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.VerificationDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.SmsBlockSending;
import by.afinny.userservice.entity.Verification;
import by.afinny.userservice.exception.BlockedReceiverException;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.VerificationCodeException;
import by.afinny.userservice.mapper.MobilePhoneMapperImpl;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.SmsBlockSendingRepository;
import by.afinny.userservice.repository.VerificationRepository;
import by.afinny.userservice.service.impl.SenderServiceImpl;
import by.afinny.userservice.service.impl.VerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class VerificationServiceImplTest {

    @InjectMocks
    private VerificationServiceImpl verificationService;
    @Mock
    private SenderServiceImpl senderService;
    @Mock
    private VerificationRepository verificationRepository;
    @Mock
    private SmsBlockSendingRepository smsBlockSendingRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private MobilePhoneMapperImpl mobilePhoneMapper;

    @Captor
    private ArgumentCaptor<Verification> verificationCaptor;
    @Captor
    private ArgumentCaptor<SmsBlockSending> smsBlockSendingCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private final String MOBILE_PHONE = "79023455502";
    private final String VERIFICATION_CODE = "310761";
    private final String PASSPORT_NUMBER = "2239963256";

    private Verification verification;
    private MobilePhoneDto mobilePhoneDto;
    private PassportDto passportDto;
    private Client client;
    private SmsBlockSending smsBlockSending;

    @BeforeEach
    void setUp() {
        verification = Verification.builder()
                .codeExpiration(LocalDateTime.now().plusMinutes(10))
                .wrongAttemptsCounter(0)
                .mobilePhone("79729782315")
                .verificationCode(VERIFICATION_CODE).build();

        mobilePhoneDto = MobilePhoneDto.builder()
                .mobilePhone(MOBILE_PHONE).build();

        passportDto = PassportDto.builder()
                .passportNumber(PASSPORT_NUMBER).build();

        client = Client.builder()
                .mobilePhone(MOBILE_PHONE)
                .passportData(PassportData.builder().passportNumber(PASSPORT_NUMBER).build())
                .build();

        smsBlockSending = SmsBlockSending.builder()
                .smsBlockExpiration(LocalDateTime.now().minusMinutes(10))
                .mobilePhone("79729782315")
                .sendingCount(0)
                .build();
    }

    @Test
    @DisplayName("If verification code has been successfully sent to mobile phone don't return content")
    void sendVerificationCode_byMobilePhone_shouldNotReturnContent() {
        //ARRANGE
        when(smsBlockSendingRepository.save(smsBlockSendingCaptor.capture())).thenAnswer(passedArgument());
        when(verificationRepository.save(verificationCaptor.capture())).thenAnswer(passedArgument());

        //ACT
        verificationService.createAndSendVerificationCode(MOBILE_PHONE);

        //VERIFY
        Verification verification = verificationCaptor.getValue();
        verifyVerification(verification);
        verify(senderService).sendMessageToMobilePhone(eq(MOBILE_PHONE), messageCaptor.capture());
        verifySentMessage(verification.getVerificationCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {MOBILE_PHONE})
    @DisplayName("If verification code sending failed then throw exception")
    void sendVerificationCode_ifSendingFailed_thenReturnInternalServerError(String receiver) {
        //ARRANGE
        when(verificationRepository.save(verification)).thenThrow(RuntimeException.class);
        //ACT
        ThrowingCallable sendVerificationCodeMethod = () -> verificationService.createAndSendVerificationCode(receiver);
        //VERIFY
        assertThatThrownBy(sendVerificationCodeMethod).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @DisplayName("If verification code is correct and not expired then don't return content")
    @ValueSource(strings = {MOBILE_PHONE})
    void checkVerificationCode_ifCodeIsCorrectAndNotExpired_thenNotReturnContent(String receiver) {
        //ARRANGE
        wayCodeNotExpired();
        when(verificationRepository
                .findByMobilePhone(eq(receiver)))
                .thenReturn(Optional.of(verification));
        //ACT
        verificationService.checkVerificationCode(createVerificationDto(receiver));
        //VERIFY
        verify(verificationRepository).delete(verification);
    }

    @ParameterizedTest
    @DisplayName("If verification code is correct but wrong attempts counter then throw exception")
    @ValueSource(strings = {MOBILE_PHONE})
    void checkVerificationCode_ifWrongAttemptsCounter_thenThrow(String receiver) {
        //ARRANGE
        wayWrongAttemptsCounter();
        when(verificationRepository
                .findByMobilePhone(eq(receiver)))
                .thenReturn(Optional.of(verification));

        //ACT
        ThrowingCallable checkVerificationCodeMethod = () -> verificationService
                .checkVerificationCode(createVerificationDto(receiver));

        //VERIFY
        assertThatThrownBy(checkVerificationCodeMethod)
                .isInstanceOf(BlockedReceiverException.class);
        verify(verificationRepository, never()).delete(verification);
        verify(verificationRepository, atLeastOnce()).save(verification);
    }

    @ParameterizedTest
    @DisplayName("If verification code is correct but user blocked then throw exception")
    @ValueSource(strings = {MOBILE_PHONE})
    void checkVerificationCode_ifUserBlocked_thenThrow(String receiver) {
        //ARRANGE
        wayUserBlocked();
        when(verificationRepository
                .findByMobilePhone(eq(receiver)))
                .thenReturn(Optional.of(verification));

        //ACT
        ThrowingCallable checkVerificationCodeMethod = () -> verificationService
                .checkVerificationCode(createVerificationDto(receiver));

        //VERIFY
        assertThatThrownBy(checkVerificationCodeMethod)
                .isInstanceOf(BlockedReceiverException.class);
        verify(verificationRepository, never()).delete(verification);
    }

    @ParameterizedTest
    @DisplayName("If verification code is correct but expired then throw exception")
    @ValueSource(strings = {MOBILE_PHONE})
    void checkVerificationCode_ifCodeExpired_thenThrow(String receiver) {
        //ARRANGE
        wayCodeExpired();
        when(verificationRepository
                .findByMobilePhone(eq(receiver)))
                .thenReturn(Optional.of(verification));
        //ACT
        ThrowingCallable checkVerificationCodeMethod = () -> verificationService
                .checkVerificationCode(createVerificationDto(receiver));
        //VERIFY
        assertThatThrownBy(checkVerificationCodeMethod)
                .isInstanceOf(VerificationCodeException.class);
        verify(verificationRepository, never()).delete(verification);
    }

    @ParameterizedTest
    @ValueSource(strings = {MOBILE_PHONE})
    @DisplayName("If verification code is invalid then throw exception")
    void checkVerificationCode_ifCodeIsInvalid_throwVerificationCodeException(String receiver) {
        //ARRANGE
        when(verificationRepository
                .findByMobilePhone(eq(receiver)))
                .thenReturn(Optional.empty());
        //ACT
        ThrowingCallable checkVerificationCodeMethod = () -> verificationService
                .checkVerificationCode(createVerificationDto(receiver));
        // VERIFY
        assertThatThrownBy(checkVerificationCodeMethod).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If successfully set block then don't return content")
    void setUserBlockTimestamp_shouldNotReturnContent() {
        //ARRANGE
        when(verificationRepository.findByMobilePhone(MOBILE_PHONE))
                .thenReturn(Optional.of(verification));
        //ACT
        verificationService.setUserBlockTimestamp(mobilePhoneDto);
        //VERIFY
        assertThat(verification.getUserBlockExpiration()).isNotNull();
    }

    @Test
    @DisplayName("If set has been failed then throw exception")
    void setUserBlockTimestamp_ifSettingFailed_thenThrow() {
        //ARRANGE
        when(verificationRepository.findByMobilePhone(MOBILE_PHONE))
                .thenReturn(Optional.empty());
        //ACT
        ThrowingCallable setUserBlockTimestampMethod = () -> verificationService.setUserBlockTimestamp(mobilePhoneDto);
        //VERIFY
        assertThatThrownBy(setUserBlockTimestampMethod).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If Client with this passport number found then return mobile phone dto")
    void getMobilePhone_shouldReturnMobilePhone() {
        //ARRANGE
        when(clientRepository.findClientByPassportDataPassportNumber(PASSPORT_NUMBER)).thenReturn(Optional.of(client));
        when(mobilePhoneMapper.toMobilePhoneDto(client)).thenReturn(mobilePhoneDto);
        //ACT
        MobilePhoneDto resultMobilePhoneDto = verificationService.getMobilePhone(passportDto);
        //VERIFY
        verifyMobilePhoneDto(resultMobilePhoneDto);
    }

    @Test
    @DisplayName("If Client with this passport number not found then throw exception")
    void getMobilePhone_ifNotFound_throwEntityNotFoundException() {
        //ARRANGE
        when(clientRepository.findClientByPassportDataPassportNumber(PASSPORT_NUMBER)).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable getMobilePhoneMethod = () -> verificationService.getMobilePhone(passportDto);
        //VERIFY
        assertThatThrownBy(getMobilePhoneMethod).isInstanceOf(EntityNotFoundException.class);
    }

    private void verifyMobilePhoneDto(MobilePhoneDto resultMobilePhoneDto) {
        assertThat(resultMobilePhoneDto.toString()).isEqualTo(mobilePhoneDto.toString());
    }

    private void wayCodeExpired() {
        verification.setCodeExpiration(LocalDateTime.now().minusMinutes(10));
    }

    private void wayWrongAttemptsCounter() {
        verification.setWrongAttemptsCounter(3);
    }

    private void wayUserBlocked() {
        verification.setUserBlockExpiration(LocalDateTime.now().plusMinutes(10));
    }

    private void wayCodeNotExpired() {
        verification.setCodeExpiration(LocalDateTime.now().plusMinutes(10));
    }

    private VerificationDto createVerificationDto(String receiver) {
        return VerificationDto.builder()
                .mobilePhone(receiver)
                .verificationCode(VERIFICATION_CODE).build();
    }

    private void verifySentMessage(String verificationCode) {
        assertThat(messageCaptor.getValue())
                .withFailMessage("Created message should contains " + verificationCode + " substring")
                .contains(verificationCode);
    }

    private void verifyVerification(Verification verification) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(verification)
                    .withFailMessage("Verification shouldn't be null")
                    .isNotNull();
            String actualReceiver = verification.getMobilePhone();
            softAssertions.assertThat(actualReceiver)
                    .withFailMessage("Receiver should be " + "79023455502" + " instead of " + actualReceiver)
                    .isEqualTo("79023455502");
            softAssertions.assertThat(verification.getVerificationCode())
                    .withFailMessage("Verification code shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(verification.getCodeExpiration())
                    .withFailMessage("Code expiration should be established")
                    .isNotNull();
        });
    }

    private Answer<Object> passedArgument() {
        return invocationOnMock -> invocationOnMock.getArgument(0);
    }
}

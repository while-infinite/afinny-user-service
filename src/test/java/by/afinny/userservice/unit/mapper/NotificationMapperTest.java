package by.afinny.userservice.unit.mapper;

import by.afinny.userservice.dto.NotificationDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.mapper.NotificationMapperImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class NotificationMapperTest {

    @InjectMocks
    private NotificationMapperImpl notificationMapper;

    private UserProfile userProfile;
    private NotificationDto notificationDto;

    @BeforeAll
    void setUp() {
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);
        userProfile = UserProfile.builder()
            .id(UUID.fromString("fa4729c2-ef3b-45a6-b5a1-e7763125518a"))
            .smsNotification(false)
            .pushNotification(false)
            .emailSubscription(false)
            .password(encoder.encode("rkfd4-4mxfrl2))-21D"))
            .email("raniwaf191@cupbest.com")
            .securityQuestion("Имя первого учителя")
            .securityAnswer("Лариса")
            .appRegistrationDate(LocalDate.now())
            .client(Client.builder()
                .id(UUID.fromString("6cd9bcdc-ed74-4545-8862-088718512d4c"))
                .firstName("Анна")
                .middleName("Николаевна")
                .lastName("Смирнова")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .mobilePhone("79225439201")
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(PassportData.builder().passportNumber("6235104793").build())
                .build())
            .build();
    }

    @Test
    @DisplayName("Verify notification dto fields setting")
    void userProfileToNotificationDto_shouldReturnNotificationDto() {
        //ACT
        notificationDto = notificationMapper.userProfileToNotificationDto(userProfile);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(notificationDto.getSmsNotification()).isEqualTo(userProfile.getSmsNotification());
            softAssertions.assertThat(notificationDto.getPushNotification()).isEqualTo(userProfile.getPushNotification());
            softAssertions.assertThat(notificationDto.getEmail()).isEqualTo(userProfile.getEmail());
            softAssertions.assertThat(notificationDto.getEmailSubscription()).isEqualTo(userProfile.getEmailSubscription());
        });
    }
}


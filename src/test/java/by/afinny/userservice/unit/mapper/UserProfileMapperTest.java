package by.afinny.userservice.unit.mapper;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.dto.ResponseClientDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import java.time.LocalDate;
import java.util.UUID;

import by.afinny.userservice.mapper.UserProfileMapperImpl;
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

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserProfileMapperTest {

    @InjectMocks
    private UserProfileMapperImpl userProfileMapper;

    private UserProfile userProfile;
    private RequestClientDto requestClientDto;

    @BeforeAll
    void setUp() {
        requestClientDto = RequestClientDto.builder()
            .id(UUID.fromString("6cd9bcdc-ed74-4545-8862-088718512d4c"))
            .mobilePhone("79023454101")
            .password("rkfd4-4mxfrl2))-21D")
            .securityQuestion("День и месяц рождения собаки")
            .securityAnswer("3 июля")
            .email("dabhom120@eoscast.com").build();

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
    @DisplayName("Verify user profile fields setting")
    void requestClientDtoToUserProfile_shouldReturnUserProfile() {
        UserProfile userProfile = userProfileMapper.requestClientDtoToUserProfile(requestClientDto);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(userProfile.getClient().getId()).isEqualTo(requestClientDto.getId());
            softAssertions.assertThat(userProfile.getClient().getMobilePhone()).isEqualTo(requestClientDto.getMobilePhone());
            softAssertions.assertThat(userProfile.getId()).isNull();
            softAssertions.assertThat(userProfile.getPassword()).isEqualTo(requestClientDto.getPassword());
            softAssertions.assertThat(userProfile.getEmail()).isEqualTo(requestClientDto.getEmail());
            softAssertions.assertThat(userProfile.getSecurityQuestion()).isEqualTo(requestClientDto.getSecurityQuestion());
            softAssertions.assertThat(userProfile.getSecurityAnswer()).isEqualTo(requestClientDto.getSecurityAnswer());
        });
    }

    @Test
    @DisplayName("Verify response client dto fields setting")
    void userProfileToResponseClientDto_shouldReturnUserProfile() {
        ResponseClientDto responseClientDto = userProfileMapper.userProfileToResponseClientDto(userProfile);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(responseClientDto.getClientStatus()).isEqualTo(userProfile.getClient().getClientStatus());
            softAssertions.assertThat(responseClientDto.getMobilePhone()).isEqualTo(userProfile.getClient().getMobilePhone());
            softAssertions.assertThat(responseClientDto.getId()).isEqualTo(userProfile.getClient().getId());
            softAssertions.assertThat(responseClientDto.getPassword()).isEqualTo(userProfile.getPassword());
            softAssertions.assertThat(responseClientDto.getSecurityQuestion()).isEqualTo(userProfile.getSecurityQuestion());
            softAssertions.assertThat(responseClientDto.getSecurityAnswer()).isEqualTo(userProfile.getSecurityAnswer());
            softAssertions.assertThat(responseClientDto.getEmail()).isEqualTo(userProfile.getEmail());
            softAssertions.assertThat(responseClientDto.getSmsNotification()).isEqualTo(userProfile.getSmsNotification());
            softAssertions.assertThat(responseClientDto.getPushNotification()).isEqualTo(userProfile.getPushNotification());
            softAssertions.assertThat(responseClientDto.getAppRegistrationDate()).isEqualTo(userProfile.getAppRegistrationDate());
        });
    }

    @Test
    @DisplayName("Verify response client data dto fields setting")
    void userProfileToResponseClientDataDto_shouldReturnClientData() {
        ResponseClientDataDto responseClientDataDto = userProfileMapper.userProfileToResponseClientDataDto(userProfile);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(responseClientDataDto.getEmail()).isEqualTo(userProfile.getEmail());
            softAssertions.assertThat(responseClientDataDto.getFirstName()).isEqualTo(userProfile.getClient().getFirstName());
            softAssertions.assertThat(responseClientDataDto.getLastName()).isEqualTo(userProfile.getClient().getLastName());
            softAssertions.assertThat(responseClientDataDto.getMiddleName()).isEqualTo(userProfile.getClient().getMiddleName());
            softAssertions.assertThat(responseClientDataDto.getMobilePhone()).isEqualTo(userProfile.getClient().getMobilePhone());
        });
    }
}
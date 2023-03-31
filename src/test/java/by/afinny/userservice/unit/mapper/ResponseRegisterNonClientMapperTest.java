package by.afinny.userservice.unit.mapper;

import by.afinny.userservice.dto.ResponseNonClientDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.mapper.ResponseRegisterNonClientMapperImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ResponseRegisterNonClientMapperTest {

    @InjectMocks
    private ResponseRegisterNonClientMapperImpl responseMapper;

    private Client client;
    private UserProfile userProfile;
    private PassportData passportData;
    private ResponseNonClientDto responseNonClientDto;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder()
            .passportNumber("6235104793").build();

        client = Client.builder()
            .id(UUID.fromString("6cd9bcdc-ed74-4545-8862-088718512d4c"))
            .firstName("Анна")
            .middleName("Николаевна")
            .lastName("Смирнова")
            .countryOfResidence(true)
            .accessionDate(LocalDate.of(2021, 10, 30))
            .mobilePhone("79225439201")
            .employerIdentificationNumber("771245423")
            .clientStatus(ClientStatus.ACTIVE)
            .passportData(passportData).build();

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
            .client(client)
            .build();
    }

    @Test
    @DisplayName("Verify response non client dto fields setting")
    void toResponseClientDto_shouldReturnResponseNonClientDto() {
        //ACT
        responseNonClientDto = responseMapper.toResponseClientDto(client, userProfile, passportData);
        //VERIFY
        verifyClientFields();
        verifyUserProfileFields();
        verifyPassportDataFields();
    }

    private void verifyClientFields() {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(responseNonClientDto.getId()).isEqualTo(client.getId());
            softAssertions.assertThat(responseNonClientDto.getClientStatus()).isEqualTo(client.getClientStatus());
            softAssertions.assertThat(responseNonClientDto.getMobilePhone()).isEqualTo(client.getMobilePhone());
            softAssertions.assertThat(responseNonClientDto.getFirstName()).isEqualTo(client.getFirstName());
            softAssertions.assertThat(responseNonClientDto.getMiddleName()).isEqualTo(client.getMiddleName());
            softAssertions.assertThat(responseNonClientDto.getLastName()).isEqualTo(client.getLastName());
            softAssertions.assertThat(responseNonClientDto.getCountryOfResidence()).isEqualTo(client.getCountryOfResidence());
            softAssertions.assertThat(responseNonClientDto.getAccessionDate()).isEqualTo(client.getAccessionDate());
        });
    }

    private void verifyUserProfileFields() {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(responseNonClientDto.getSmsNotification()).isEqualTo(userProfile.getSmsNotification());
            softAssertions.assertThat(responseNonClientDto.getPushNotification()).isEqualTo(userProfile.getPushNotification());
            softAssertions.assertThat(responseNonClientDto.getPassword()).isEqualTo(userProfile.getPassword());
            softAssertions.assertThat(responseNonClientDto.getSecurityQuestion()).isEqualTo(userProfile.getSecurityQuestion());
            softAssertions.assertThat(responseNonClientDto.getSecurityAnswer()).isEqualTo(userProfile.getSecurityAnswer());
            softAssertions.assertThat(responseNonClientDto.getEmail()).isEqualTo(userProfile.getEmail());
            softAssertions.assertThat(responseNonClientDto.getAppRegistrationDate()).isEqualTo(userProfile.getAppRegistrationDate());
        });
    }

    private void verifyPassportDataFields() {
        assertThat(responseNonClientDto.getPassportNumber()).isEqualTo(passportData.getPassportNumber());
    }
}


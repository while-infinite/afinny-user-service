package by.afinny.userservice.unit.mapper;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.mapper.RequestRegisterNonClientMapperImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class RequestRegisterNonClientMapperTest {

    @InjectMocks
    private RequestRegisterNonClientMapperImpl requestMapper;

    private Client client;
    private UserProfile userProfile;
    private PassportData passportData;
    private RequestNonClientDto requestNonClientDto;

    @BeforeAll
    void setUp() {
        requestNonClientDto = RequestNonClientDto.builder()
            .mobilePhone("345654009348")
            .password("io4DEKScx")
            .securityQuestion("Девичья фамилия матери")
            .securityAnswer("Семенова")
            .email("mixawet616@dufeed.com")
            .firstName("Петр")
            .middleName("Петрович")
            .lastName("Петров")
            .passportNumber("1010325260")
            .countryOfResidence(true).build();
    }

    @Test
    @DisplayName("Verify user profile fields setting")
    void dtoToUserProfile_shouldReturnUserProfile() {
        //ACT
        userProfile = requestMapper.dtoToUserProfile(requestNonClientDto);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(userProfile.getPassword()).isEqualTo(requestNonClientDto.getPassword());
            softAssertions.assertThat(userProfile.getEmail()).isEqualTo(requestNonClientDto.getEmail());
            softAssertions.assertThat(userProfile.getSecurityQuestion()).isEqualTo(requestNonClientDto.getSecurityQuestion());
            softAssertions.assertThat(userProfile.getSecurityAnswer()).isEqualTo(requestNonClientDto.getSecurityAnswer());
        });
    }

    @Test
    @DisplayName("Verify client fields setting")
    void dtoToClient_shouldReturnClient() {
        //ACT
        client = requestMapper.dtoToClient(requestNonClientDto);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(client.getFirstName()).isEqualTo(requestNonClientDto.getFirstName());
            softAssertions.assertThat(client.getLastName()).isEqualTo(requestNonClientDto.getLastName());
            softAssertions.assertThat(client.getMiddleName()).isEqualTo(requestNonClientDto.getMiddleName());
            softAssertions.assertThat(client.getCountryOfResidence()).isEqualTo(requestNonClientDto.getCountryOfResidence());
            softAssertions.assertThat(client.getMobilePhone()).isEqualTo(requestNonClientDto.getMobilePhone());
        });
    }
    @Test
    @DisplayName("Verify passport data fields setting")
    void dtoToPassportData_shouldReturnPassportData() {
        //ACT
        passportData = requestMapper.dtoToPassportData(requestNonClientDto);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(passportData.getPassportNumber()).isEqualTo(requestNonClientDto.getPassportNumber());
        });
    }
}


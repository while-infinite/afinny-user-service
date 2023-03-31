package by.afinny.userservice.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@Sql(
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"/schema-h2.sql"}
)
@ActiveProfiles("test")
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;

    private final String MOBILE_PHONE = "79324561141";
    private final String PASSPORT_NUMBER = "5715244303";

    private UserProfile userProfile;
    private PassportData passportData;
    private Client client;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder()
            .passportNumber(PASSPORT_NUMBER).build();
        client = Client.builder()
            .id(UUID.fromString("ea1119c7-5c56-455d-99e2-df8a1b8f014d"))
            .firstName("Иван")
            .middleName("Иванович")
            .lastName("Иванов")
            .countryOfResidence(true)
            .accessionDate(LocalDate.now())
            .mobilePhone(MOBILE_PHONE)
            .employerIdentificationNumber("78219402113")
            .clientStatus(ClientStatus.CLOSED)
            .passportData(passportData).build();
        userProfile = UserProfile.builder()
            .id(UUID.fromString("03e5d7a2-d616-48a6-beda-253283812750"))
            .smsNotification(true)
            .pushNotification(false)
            .emailSubscription(false)
            .password("slkf23kri_1")
            .email("raniwaf191@cupbest.com")
            .securityQuestion("Улица, на которой родился")
            .securityAnswer("Ленина")
            .appRegistrationDate(LocalDate.now())
            .client(client).build();
    }

    @AfterEach
    void cleanUp() {
        userProfileRepository.deleteAll();
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
    }

    @Test
    @DisplayName("If user profile with client with this mobile phone doesn't exist then return empty")
    void findByClientMobilePhone_ifUserProfileNotExists_thenReturnEmpty() {
        //ACT
        Optional<UserProfile> client = userProfileRepository.findByClientMobilePhone(MOBILE_PHONE);
        //VERIFY
        assertThat(client.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If user profile with client with this mobile phone exists then return user profile")
    void findByClientMobilePhone_ifUserProfileExists_thenReturnUserProfile() {
        //ARRANGE
        passportDataRepository.save(passportData);
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        userProfile.setId(savedUserProfile.getId());
        client.setId(savedUserProfile.getClient().getId());
        //ACT
        UserProfile foundUserProfile = userProfileRepository.findByClientMobilePhone(MOBILE_PHONE)
            .orElseThrow(() -> new EntityNotFoundException("User profile with mobile phone " + MOBILE_PHONE + " wasn't found"));
        //VERIFY
        verifyUserProfile(foundUserProfile);
    }

    @Test
    @DisplayName("If user profile with this passport number doesn't exist then return empty")
    void findByClientPassportDataPassportNumber_ifUserProfileNotExists_thenReturnEmpty() {
        //ACT
        Optional<UserProfile> userProfile = userProfileRepository.findByClientPassportDataPassportNumber(PASSPORT_NUMBER);
        //VERIFY
        assertThat(userProfile.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If user profile with this passport number exists then return user profile")
    void findByClientPassportDataPassportNumber_ifUserProfileExists_thenReturnUserProfile() {
        //ARRANGE
        passportDataRepository.save(passportData);
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        userProfile.setId(savedUserProfile.getId());
        client.setId(savedUserProfile.getClient().getId());
        //ACT
        UserProfile foundUserProfile = userProfileRepository.findByClientPassportDataPassportNumber(PASSPORT_NUMBER)
            .orElseThrow(() -> new EntityNotFoundException("User profile with passport number " + PASSPORT_NUMBER + " wasn't found"));
        //VERIFY
        verifyUserProfile(foundUserProfile);
    }

    @Test
    @DisplayName("If user profile with this client id doesn't exist then return empty")
    void findByClientId_ifUserProfileNotExists_thenReturnUserProfile() {
        //ACT
        Optional<UserProfile> userProfile = userProfileRepository.findByClientId(UUID.fromString("65cb4f3c-3f7f-4311-a097-0aba2f289768"));
        //VERIFY
        assertThat(userProfile.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If user profile with this client id exists then return user profile")
    void findByClientId_ifUserProfileExists_thenReturnUserProfile() {
        //ARRANGE
        passportDataRepository.save(passportData);
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        userProfile.setId(savedUserProfile.getId());
        UUID clientId = savedUserProfile.getClient().getId();
        client.setId(clientId);
        //ACT
        UserProfile foundUserProfile = userProfileRepository.findByClientId(clientId)
            .orElseThrow(() -> new EntityNotFoundException("User profile with passport number " + PASSPORT_NUMBER + " wasn't found"));
        //VERIFY
        verifyUserProfile(foundUserProfile);
    }

    private void verifyUserProfile(UserProfile foundUserProfile) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundUserProfile.getId()).isEqualTo(userProfile.getId());
            softAssertions.assertThat(foundUserProfile.getSmsNotification()).isEqualTo(userProfile.getSmsNotification());
            softAssertions.assertThat(foundUserProfile.getPushNotification()).isEqualTo(userProfile.getPushNotification());
            softAssertions.assertThat(foundUserProfile.getEmailSubscription()).isEqualTo(userProfile.getEmailSubscription());
            softAssertions.assertThat(foundUserProfile.getPassword()).isEqualTo(userProfile.getPassword());
            softAssertions.assertThat(foundUserProfile.getEmail()).isEqualTo(userProfile.getEmail());
            softAssertions.assertThat(foundUserProfile.getSecurityQuestion()).isEqualTo(userProfile.getSecurityQuestion());
            softAssertions.assertThat(foundUserProfile.getSecurityAnswer()).isEqualTo(userProfile.getSecurityAnswer());
            softAssertions.assertThat(foundUserProfile.getAppRegistrationDate()).isEqualTo(userProfile.getAppRegistrationDate());
            softAssertions.assertThat(foundUserProfile.getClient().getId()).isEqualTo(userProfile.getClient().getId());
        });
    }
}


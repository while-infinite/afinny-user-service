package by.afinny.userservice.unit.service;

import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.mapper.UserProfileMapper;
import by.afinny.userservice.mapper.UserProfileMapperImpl;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.impl.InformationServiceImpl;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class InformationServiceTest {

    @InjectMocks
    private InformationServiceImpl informationService;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Spy
    private UserProfileMapper userProfileMapper = new UserProfileMapperImpl();

    private final String MOBILE_PHONE = "79182546634";
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String CLIENT_PASSWORD = "123qWeRtY456.!";

    private UserProfile userProfile;
    private Client client;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(CLIENT_ID)
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .countryOfResidence(true)
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("7727563778")
                .build();

        userProfile = UserProfile.builder()
                .id(CLIENT_ID)
                .smsNotification(true)
                .pushNotification(true)
                .emailSubscription(false)
                .password(CLIENT_PASSWORD)
                .email("dabhom120@eoscast.com")
                .appRegistrationDate(LocalDate.now())
                .client(client).build();
    }

    @Test
    @DisplayName("If client exists then return client data")
    void getClientData_shouldReturnClientDAta() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(userProfile));
        //ACT
        ResponseClientDataDto clientData = informationService.getClientData(CLIENT_ID);
        //VERIFY
        verifyClientData(clientData);
    }

    @Test
    @DisplayName("If client wasn't found then throw EntityNotFoundException")
    void getClientData_ifClientNotExists_thenThrow() {
        //ARRANGE
        when(userProfileRepository.findByClientId(CLIENT_ID))
                .thenThrow(EntityNotFoundException.class);
        //ACT
        ThrowableAssert.ThrowingCallable getClientDataMethod = () -> informationService.getClientData(CLIENT_ID);
        //VERIFY
        assertThatThrownBy(getClientDataMethod).isNotNull();
    }

    private void verifyClientData(ResponseClientDataDto clientData) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientData).isNotNull();
            softAssertions.assertThat(clientData.getEmail()).isEqualTo(userProfile.getEmail());
            softAssertions.assertThat(clientData.getMiddleName()).isEqualTo(userProfile.getClient().getMiddleName());
            softAssertions.assertThat(clientData.getLastName()).isEqualTo(userProfile.getClient().getLastName());
            softAssertions.assertThat(clientData.getFirstName()).isEqualTo(userProfile.getClient().getFirstName());
            softAssertions.assertThat(clientData.getMobilePhone()).isEqualTo(userProfile.getClient().getMobilePhone());
        });
    }
}
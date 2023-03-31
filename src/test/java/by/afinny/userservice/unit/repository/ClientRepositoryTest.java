package by.afinny.userservice.unit.repository;

import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@Sql(
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;

    private final String MOBILE_PHONE = "79034561558";
    private final String PASSPORT_NUMBER = "5715244303";

    private Client client;
    private PassportData passportData;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder()
                .passportNumber(PASSPORT_NUMBER).build();
        client = Client.builder()
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .countryOfResidence(true)
                .accessionDate(LocalDate.now())
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("78219402113")
                .clientStatus(ClientStatus.CLOSED)
                .passportData(passportData).build();
    }

    @AfterEach
    void cleanUp() {
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
    }

    @Test
    @DisplayName("If client with this mobile phone doesn't exist then return empty")
    void findClientByMobilePhone_ifClientNotExists_thenReturnEmpty() {
        //ACT
        Optional<Client> client = clientRepository.findClientByMobilePhone(MOBILE_PHONE);
        //VERIFY
        assertThat(client.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If client with this mobile phone exists then return the client")
    void findClientByMobilePhone_ifClientExists_thenReturnClient() {
        //ARRANGE
        passportDataRepository.save(passportData);
        UUID clientId = clientRepository.save(client).getId();
        client.setId(clientId);
        //ACT
        Client foundClient = clientRepository.findClientByMobilePhone(MOBILE_PHONE)
                .orElseThrow(() -> new EntityNotFoundException("Client with mobile phone " + MOBILE_PHONE + " wasn't found"));
        //VERIFY
        verifyClient(foundClient);
    }

    @Test
    @DisplayName("If client with this id doesn't exist then return empty")
    void findClientById_ifClientNotExists_thenReturnEmpty() {
        //ACT
        Optional<Client> client = clientRepository.findClientById(UUID.fromString("03e5d7a2-d616-48a6-beda-253283812750"));
        //VERIFY
        assertThat(client.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If client with this id exists then return the client")
    void findClientById_iClientExists_thenReturnClient() {
        //ARRANGE
        passportDataRepository.save(passportData);
        UUID clientId = clientRepository.save(client).getId();
        client.setId(clientId);
        //ACT
        Client foundClient = clientRepository.findClientById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client with mobile phone " + MOBILE_PHONE + " wasn't found"));
        //VERIFY
        verifyClient(foundClient);
    }

    @Test
    @DisplayName("If client with this passport number doesn't exist then return empty")
    void findClientByPassportDataPassportNumber_ifClientNotExists_thenReturnEmpty() {
        //ACT
        Optional<Client> client = clientRepository.findClientByPassportDataPassportNumber(PASSPORT_NUMBER);
        //VERIFY
        assertThat(client.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If client with this passport number exists then return the client")
    void findClientByPassportDataPassportNumber_ifClientExists_thenReturnClient() {
        //ARRANGE
        passportDataRepository.save(passportData);
        UUID clientId = clientRepository.save(client).getId();
        client.setId(clientId);
        //ACT
        Client foundClient = clientRepository.findClientByPassportDataPassportNumber(PASSPORT_NUMBER)
                .orElseThrow(() -> new EntityNotFoundException("Client with passport number " + PASSPORT_NUMBER + " wasn't found"));
        //VERIFY
        verifyClient(foundClient);
    }

    private void verifyClient(Client foundClient) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundClient.getId()).isEqualTo(client.getId());
            softAssertions.assertThat(foundClient.getMobilePhone()).isEqualTo(MOBILE_PHONE);
            softAssertions.assertThat(foundClient.getFirstName()).isEqualTo(client.getFirstName());
            softAssertions.assertThat(foundClient.getMiddleName()).isEqualTo(client.getMiddleName());
            softAssertions.assertThat(foundClient.getLastName()).isEqualTo(client.getLastName());
            softAssertions.assertThat(foundClient.getClientStatus()).isEqualTo(client.getClientStatus());
            softAssertions.assertThat(foundClient.getCountryOfResidence()).isEqualTo(client.getCountryOfResidence());
            softAssertions.assertThat(foundClient.getAccessionDate()).isEqualTo(client.getAccessionDate());
            softAssertions.assertThat(foundClient.getEmployerIdentificationNumber()).isEqualTo(client.getEmployerIdentificationNumber());
            softAssertions.assertThat(foundClient.getPassportData().getPassportNumber()).isEqualTo(client.getPassportData().getPassportNumber());
        });
    }
}


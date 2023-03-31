package by.afinny.userservice.unit.repository;

import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.Fingerprint;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.FingerprintRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@ActiveProfiles("test")
class FingerprintRepositoryTest {

    @Autowired
    private FingerprintRepository fingerprintRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;

    private Client client;
    private Fingerprint fingerprint;
    private PassportData passportData;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder()
                .passportNumber("5715244303")
                .build();

        client = Client.builder()
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .countryOfResidence(true)
                .accessionDate(LocalDate.now())
                .mobilePhone("79034561558")
                .employerIdentificationNumber("782194021130000000000000000000")
                .clientStatus(ClientStatus.CLOSED)
                .passportData(passportData)
                .build();

        fingerprint = Fingerprint.builder()
                .fingerprint("FINGERPRINT")
                .client(client)
                .build();
    }

    @AfterEach
    void cleanUp() {
        fingerprintRepository.deleteAll();
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
    }

    @Test
    @DisplayName("If fingerprint with this client id doesn't exist then return empty")
    void findByClientId_ifFingerprintNotExists_thenReturnEmpty() {
        //ACT
        Optional<Fingerprint> fingerprint = fingerprintRepository
                .findByClientIdAndFingerprint(UUID.fromString("e1b25352-e259-11ec-8fea-0242ac120002"), "dummy");
        //VERIFY
        assertThat(fingerprint.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If fingerprint with this client id exists then return the fingerprint")
    void findByClientId_ifFingerprintExists(){
        //ARRANGE
        passportDataRepository.save(passportData);
        UUID clientId = clientRepository.save(client).getId();
        client.setId(clientId);
        fingerprintRepository.save(fingerprint);
        //ACT
        Fingerprint foundFingerprint = fingerprintRepository
                .findByClientIdAndFingerprint(clientId, fingerprint.getFingerprint())
                .orElseThrow(() -> new EntityNotFoundException("Fingerprint with client id " + clientId + " wasn't found"));
        //VERIFY
        verifyClient(foundFingerprint);
    }

    private void verifyClient(Fingerprint foundFingerprint) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundFingerprint.getFingerprint()).isEqualTo(fingerprint.getFingerprint());
            softAssertions.assertThat(foundFingerprint.getClient().getId()).isEqualTo(fingerprint.getClient().getId());
        });
    }
}
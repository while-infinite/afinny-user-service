package by.afinny.userservice.unit.kafka;

import by.afinny.userservice.dto.kafka.EmployerEvent;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.kafka.EmployerTopicListeners;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "kafka.enabled=true"
)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:29092", "port=29092"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/schema-h2.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisplayName("Test for employer topic listener")
public class EmployerTopicListenersTest {

    @Autowired
    private EmployerTopicListeners employerTopicListeners;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;

    private Client client;
    private PassportData passportData;
    private EmployerEvent event;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder().passportNumber("6235104794").build();
        client = Client.builder()
                .firstName("Анна")
                .middleName("Николаевна")
                .lastName("Смирнова")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .employerIdentificationNumber("0293583223")
                .mobilePhone("79818430014")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(passportData)
                .build();
    }

    @BeforeEach
    void save() {
        passportDataRepository.save(passportData);
        client = clientRepository.save(client);
    }

    @AfterEach
    void cleanUp() {
        clientRepository.deleteAll();
        passportDataRepository.deleteAll();
    }

    @Test
    @DisplayName("If new employer's id is different from stored then update")
    void onRequestUpdateEmployerIdEvent_shouldInvokeModifyEmployerId() {
        //ARRANGE
        String newEmployerId = "93919751211";
        event = EmployerEvent.builder()
                .clientId(client.getId())
                .employerIdentificationNumber(newEmployerId).build();
        //ACT
        employerTopicListeners.onRequestUpdateEmployerIdEvent(new GenericMessage<>(event));
        //VERIFY
        Client clientFromDb = clientRepository.findClientById(this.client.getId()).orElseThrow();
        assertThat(clientFromDb.getEmployerIdentificationNumber().trim()).isEqualTo(newEmployerId);
    }

}

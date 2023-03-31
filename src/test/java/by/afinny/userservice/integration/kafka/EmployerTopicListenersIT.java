package by.afinny.userservice.integration.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import by.afinny.userservice.integration.config.annotation.TestWithKafkaContainer;
import by.afinny.userservice.dto.kafka.EmployerEvent;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.kafka.EmployerTopicListeners;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@TestWithKafkaContainer
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Integration test for employer topic listener")
class EmployerTopicListenersIT {

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
        passportData = PassportData.builder().passportNumber("6235104793").build();
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
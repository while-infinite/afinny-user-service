package by.afinny.userservice.unit.mapper;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.PassportData;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import by.afinny.userservice.mapper.ClientMapperImpl;
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
class ClientMapperTest {

    @InjectMocks
    private ClientMapperImpl clientMapper;

    private Client client;
    private ClientDto clientDto;

    @BeforeAll
    void setUp() {
        client = Client.builder()
            .id(UUID.fromString("839931e0-e9bf-46d2-a63c-233b02ec05f0"))
            .firstName("Иван")
            .middleName("Иванович")
            .lastName("Иванов")
            .countryOfResidence(true)
            .mobilePhone("235800123255")
            .employerIdentificationNumber("7727563778")
            .passportData(PassportData.builder()
                .passportNumber("1304323343")
                .issuanceDate(Date.from(Instant.parse("2014-02-13T00:00:01.00Z")))
                .nationality("Русский")
                .birthDate(Date.from(Instant.parse("1993-11-30T00:00:01.00Z"))).build())
            .build();
    }

    @Test
    @DisplayName("Verify client dto fields setting")
    void clientToDto_shouldReturnClientDto() {
        //ACT
        clientDto = clientMapper.clientToDto(client);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getId()).isEqualTo(client.getId());
            softAssertions.assertThat(clientDto.getMobilePhone()).isEqualTo(client.getMobilePhone());
            softAssertions.assertThat(clientDto.getClientStatus()).isEqualTo(client.getClientStatus());
        });
    }
}
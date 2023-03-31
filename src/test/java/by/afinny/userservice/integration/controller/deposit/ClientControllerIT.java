package by.afinny.userservice.integration.controller.deposit;

import by.afinny.userservice.controller.deposit.ClientController;
import by.afinny.userservice.dto.ClientByPhoneDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.mapper.ClientMapper;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration test for client controller")
public class ClientControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private final String MOBILE_PHONE = "23347620277";
    private final String INCORRECT_PHONE = "88005553535";
    private final String PASSPORT_NUMBER = "9055187400";
    private final String STORED_PASSWORD = "hon_MNEYT95";

    private Client client;
    private PassportData passportData;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder().passportNumber(PASSPORT_NUMBER).build();
        client = Client.builder()
                .id(UUID.randomUUID())
                .firstName("Anna")
                .middleName("Nikolaevna")
                .lastName("Smirnova")
                .countryOfResidence(true)
                .accessionDate(LocalDate.of(2021, 10, 30))
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("771245423")
                .clientStatus(ClientStatus.ACTIVE)
                .passportData(passportData)
                .build();
    }

    @BeforeEach
    void save() {
        passportDataRepository.save(passportData);
        clientRepository.save(client);
    }

    @Test
    @DisplayName("if client print correct phone, then return OK and client by phone dto")
    void getClientByMobilePhone_ifMobilePhoneExist_thenReturnOkStatus() throws Exception {
        //ARRANGE
        Client client = clientRepository.findClientByMobilePhone(MOBILE_PHONE).orElseThrow();
        //ACT
        MvcResult result = mockMvc.perform(get(ClientController.GET_CLIENT_URL)
                        .param(ClientController.MOBILE_PHONE_PARAMETER, MOBILE_PHONE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //VERIFY
        ClientByPhoneDto clientByPhoneDto = getObjectFromJson(result.getResponse().getContentAsString()
                , ClientByPhoneDto.class);
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientByPhoneDto.getClientId()).isEqualTo(client.getId());
            softAssertions.assertThat(clientByPhoneDto.getFirstName()).isEqualTo(client.getFirstName());
            softAssertions.assertThat(clientByPhoneDto.getMiddleName()).isEqualTo(client.getMiddleName());
            softAssertions.assertThat(clientByPhoneDto.getLastName()).isEqualTo(client.getLastName());
        });
    }

    @Test
    @DisplayName("if client print incorrect or not exist phone, then return is bad request")
    void getClientByMobilePhone_ifMobilePhoneDoesNotExistOrIncorrect_thenReturnBadRequest() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(get(ClientController.GET_CLIENT_URL)
                        .param(ClientController.MOBILE_PHONE_PARAMETER, INCORRECT_PHONE))
                .andExpect(status().isBadRequest());
    }

    private <T> T getObjectFromJson(String json, Class<T> objectClass) throws IOException {
        return objectMapper.readValue(json, objectClass);
    }
}

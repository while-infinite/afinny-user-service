package by.afinny.userservice.integration.controller;

import by.afinny.userservice.controller.FingerprintController;
import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration test for finger print controller")
public class FingerprintControllerIT {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PassportDataRepository passportDataRepository;

    private final String MOBILE_PHONE = "23347620277";
    private final String PASSPORT_NUMBER = "9055187400";

    private Client client;
    private PassportData passportData;
    private FingerprintDto fingerprintDto;

    @BeforeAll
    void setUp() {
        passportData = PassportData.builder().passportNumber(PASSPORT_NUMBER).build();
        client = Client.builder()
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
        fingerprintDto = FingerprintDto.builder()
                .fingerprint("fingerPrint")
                .build();
    }

    @BeforeEach
    void save() {
        passportData = passportDataRepository.save(passportData);
        client = clientRepository.save(client);
        fingerprintDto.setClientId(client.getId());
    }

    @Test
    @DisplayName("If fingerprint successfully created then return status OK")
    public void creatFingerprint_ifFingerprintCreateSuccessfully_thenReturnOk() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(post(FingerprintController.URL_FINGERPRINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(fingerprintDto)))
                .andExpect(status().isOk());
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}

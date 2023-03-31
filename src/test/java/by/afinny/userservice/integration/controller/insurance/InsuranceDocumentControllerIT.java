package by.afinny.userservice.integration.controller.insurance;

import by.afinny.userservice.controller.insurance.InsuranceDocumentController;
import by.afinny.userservice.document.AutoInsuranceDocument;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.integration.config.annotation.TestWithMongoContainer;
import by.afinny.userservice.repository.mongodb.InsuranceDocumentRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithMongoContainer
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration test for insurance document controller")
public class InsuranceDocumentControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InsuranceDocumentRepository insuranceDocumentRepository;

    private final String MOBILE_PHONE = "23347620277";
    private final String PASSPORT_NUMBER = "9055187400";

    private Client client;
    private PassportData passportData;
    private AutoInsuranceDocument autoInsuranceDocument;
    private AutoInsuranceDocumentDto autoInsuranceDocumentDtoCorrect;
    private AutoInsuranceDocumentDto autoInsuranceDocumentDtoCorrectIncorrect;
    private MockMultipartFile file;


    @BeforeAll
    void setUp() throws IOException {
        file = new MockMultipartFile("passportOwnerPage",
                "passport_owner_page.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes());
        autoInsuranceDocumentDtoCorrect = AutoInsuranceDocumentDto.builder()
                .documentName("passportOwnerPage")
                .file(file)
                .build();
        autoInsuranceDocumentDtoCorrectIncorrect = AutoInsuranceDocumentDto.builder()
                .documentName("passportOwnerPage")
                .build();
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
        autoInsuranceDocument = createInsuranceDocument(client.getId(), autoInsuranceDocumentDtoCorrect);
    }

    @BeforeEach
    void save() {
        insuranceDocumentRepository.save(autoInsuranceDocument);
    }

    @Test
    @DisplayName("If successfully upload document then don't return content")
    void uploadInsuranceDocument_shouldNotReturnContent() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(post(InsuranceDocumentController.NEW_DOCUMENT_URL)
                        .param("clientId", client.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("file", autoInsuranceDocumentDtoCorrect))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If insurance document upload document saving has been failed then return INTERNAL SERVER ERROR")
    void uploadInsuranceDocument_ifDocumentsNotSaved_thenReturnInternalServerError() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(post(InsuranceDocumentController.NEW_DOCUMENT_URL)
                        .param("clientId", client.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("file", autoInsuranceDocumentDtoCorrectIncorrect))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If insurance document has succeed deleted then return No Content")
    void deleteInsuranceDocument_ifDocumentDelete_thenReturnNoContent() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(delete(InsuranceDocumentController.DELETE_DOCUMENT_URL,
                        autoInsuranceDocument.getId().toString()))
                .andExpect(status().isNoContent());
    }


    private AutoInsuranceDocument createInsuranceDocument(UUID clientId, AutoInsuranceDocumentDto autoInsuranceDocumentDto)
            throws IOException {
        return AutoInsuranceDocument.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .documentName(autoInsuranceDocumentDto.getDocumentName())
                .creationDate(LocalDate.now())
                .file(autoInsuranceDocumentDto.getFile().getBytes())
                .build();
    }
}

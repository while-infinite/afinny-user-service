package by.afinny.userservice.integration.controller;

import by.afinny.userservice.controller.DocumentController;
import by.afinny.userservice.document.VerificationDocument;
import by.afinny.userservice.dto.ResponseVerificationDocumentsDto;
import by.afinny.userservice.dto.VerificationDocumentsDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.integration.config.annotation.TestWithMongoContainer;
import by.afinny.userservice.repository.mongodb.VerificationDocumentRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithMongoContainer
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration test for document controller")
public class DocumentControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private VerificationDocumentRepository documentRepository;

    private final String MOBILE_PHONE = "23347620277";
    private final String PASSPORT_NUMBER = "9055187400";

    private Client client;
    private PassportData passportData;
    private VerificationDocument page3;
    private VerificationDocument registrationPage;
    private ResponseVerificationDocumentsDto responseVerificationDocumentsDto;
    private VerificationDocumentsDto verificationDocumentsDto;
    private MockMultipartFile page3File;
    private MockMultipartFile registrationPageFile;

    @BeforeAll
    void setUp() throws IOException {
        page3File = new MockMultipartFile("page3",
                "page3_passport.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "page3!".getBytes());

        registrationPageFile = new MockMultipartFile("registrationPage",
                "registrationPage_passport.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "registrationPage!".getBytes());

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

        page3 = VerificationDocument.builder()
                .id(UUID.randomUUID())
                .clientId(client.getId())
                .creationDate(LocalDate.now())
                .documentName("page3")
                .fileFormat(".jpg")
                .file(page3File.getBytes())
                .build();

        registrationPage = VerificationDocument.builder()
                .id(UUID.randomUUID())
                .clientId(client.getId())
                .creationDate(LocalDate.now())
                .documentName("registrationPage")
                .fileFormat(".jpg")
                .file(registrationPageFile.getBytes())
                .build();

        verificationDocumentsDto = VerificationDocumentsDto.builder()
                .page3(page3File)
                .registrationPage(registrationPageFile)
                .build();

        responseVerificationDocumentsDto = ResponseVerificationDocumentsDto.builder()
                .page3(page3)
                .registrationPage(registrationPage)
                .build();
    }

    @BeforeEach
    void save() {
        documentRepository.deleteAll();
        page3 = documentRepository.save(page3);
        registrationPage = documentRepository.save(registrationPage);
    }

    @Test
    @DisplayName("If successfully upload document then will be returned status OK")
    void uploadingDocument_ifDocumentsDontExist_returnStatusOk() throws Exception {
        //ARRANGE
        documentRepository.deleteAll();
        //ACT&VERIFY
        mockMvc.perform(post(DocumentController.VERIFICATION_DOCUMENTS_URL +
                        DocumentController.UPLOADING_DOCUMENT_URL)
                        .param("clientId", client.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", verificationDocumentsDto))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("if document exist then delete and return status no content 204")
    void deleteDocument_shouldDelete() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(delete(DocumentController.VERIFICATION_DOCUMENTS_URL +
                        DocumentController.DELETE_DOCUMENT_URL, page3.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("If documents exist then return response verification documents dto")
    void getDocuments_shouldReturnDocumentsDto() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(
                        get(DocumentController.VERIFICATION_DOCUMENTS_URL +
                                DocumentController.GET_DOCUMENT_URL, client.getId()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(responseVerificationDocumentsDto),
                result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If documents exist then will be returned 409 CONFLICT")
    void uploadingDocuments_ifDocumentsExist_shouldReturnConflict() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(post(DocumentController.VERIFICATION_DOCUMENTS_URL +
                        DocumentController.UPLOADING_DOCUMENT_URL)
                        .param("clientId", client.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", verificationDocumentsDto))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("If documents are null then will be returned 400 BAD REQUEST")
    void uploadingDocuments_ifDocumentsAreNull_shouldReturnBadRequest() throws Exception {
        //ARRANGE
        VerificationDocumentsDto nullDocsDto = VerificationDocumentsDto.builder()
                .page3(null)
                .registrationPage(null)
                .build();
        //ACT&VERIFY
        mockMvc.perform(post(DocumentController.VERIFICATION_DOCUMENTS_URL +
                        DocumentController.UPLOADING_DOCUMENT_URL)
                        .param("clientId", client.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", nullDocsDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If documents are empty then will be returned 400 BAD REQUEST")
    void uploadingDocuments_ifDocumentsAreEmpty_shouldReturnBadRequest() throws Exception {
        //ARRANGE
        MultipartFile emptyPage3 = new MockMultipartFile("page3", new byte[]{});
        MultipartFile emptyRegistrationPage = new MockMultipartFile("registrationPage", new byte[]{});
        VerificationDocumentsDto emptyDocsDto = VerificationDocumentsDto.builder()
                .page3(emptyPage3)
                .registrationPage(emptyRegistrationPage)
                .build();
        //ACT&VERIFY
        mockMvc.perform(post(DocumentController.VERIFICATION_DOCUMENTS_URL +
                        DocumentController.UPLOADING_DOCUMENT_URL)
                        .param("clientId", client.getId().toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", emptyDocsDto))
                .andExpect(status().isBadRequest());
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}

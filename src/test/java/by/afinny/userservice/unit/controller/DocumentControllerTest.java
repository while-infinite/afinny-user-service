package by.afinny.userservice.unit.controller;

import by.afinny.userservice.controller.DocumentController;
import by.afinny.userservice.document.VerificationDocument;
import by.afinny.userservice.dto.ResponseVerificationDocumentsDto;
import by.afinny.userservice.dto.VerificationDocumentsDto;
import by.afinny.userservice.exception.DocumentsAlreadyExistException;
import by.afinny.userservice.exception.IncorrectParameterException;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.DocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(DocumentController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentControllerTest {

    @MockBean
    private DocumentService documentService;

    private final UUID CLIENT_ID = UUID.fromString("9b81ee52-2c0d-4bda-90b4-0b12e9d6f467");
    private final UUID documentId = UUID.randomUUID();

    private MockMvc mockMvc;
    private MockMultipartFile page3;
    private MockMultipartFile registrationPage;
    private VerificationDocumentsDto verificationDocumentsDto;
    private ResponseVerificationDocumentsDto responseDocumentsDto;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(new DocumentController(documentService))
                .setControllerAdvice(ExceptionHandlerController.class).build();
        page3 = new MockMultipartFile("page3",
                "passport_page_3.jpg",
                "image/jpeg",
                "Hello, World!".getBytes());
        registrationPage = new MockMultipartFile("registrationPage ",
                "passport_registration_page.jpg",
                "image/jpeg",
                "Hello, World!".getBytes());
        verificationDocumentsDto = VerificationDocumentsDto.builder()
                .page3(page3)
                .registrationPage(registrationPage)
                .build();
        VerificationDocument document = VerificationDocument.builder()
                .clientId(CLIENT_ID)
                .id(documentId)
                .build();
        responseDocumentsDto = ResponseVerificationDocumentsDto.builder()
                .page3(document)
                .registrationPage(document)
                .build();
    }

    @Test
    @DisplayName("If successfully saved documents then don't return content")
    void uploadingDocuments_shouldNotReturnContent() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(post("/auth/verification-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .sessionAttr("files", verificationDocumentsDto))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If documents saving has been failed then return INTERNAL SERVER ERROR")
    void uploadingDocuments_ifDocumentsNotSaved_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(documentService).uploadingDocuments(any(UUID.class), any(), any());
        //ACT&VERIFY
        mockMvc.perform(post("/auth/verification-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .sessionAttr("files", verificationDocumentsDto))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if document deleted return ok")
    void deleteDocument() throws Exception {
        mockMvc.perform(
                delete(DocumentController.VERIFICATION_DOCUMENTS_URL + "/" + documentId))
                .andExpect(status().is(204));
    }

    @Test
    @DisplayName("getting documents dto")
    void getDocuments_shouldReturnDocumentsDto () throws Exception {
        //ARRANGE
        when(documentService.getDocuments(CLIENT_ID)).thenReturn(responseDocumentsDto);

        //ACT
        MvcResult result = mockMvc.perform(
                get(DocumentController.VERIFICATION_DOCUMENTS_URL + "/" + CLIENT_ID))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(responseDocumentsDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If documents are null then return BAD REQUEST")
    void uploadingDocuments_ifDocumentsNull_thenReturnBadRequest() throws Exception {
        //ARRANGE
        VerificationDocumentsDto documentsDto = VerificationDocumentsDto.builder()
                .page3(null)
                .registrationPage(null)
                .build();
        doThrow(IncorrectParameterException.class)
                .when(documentService).uploadingDocuments(CLIENT_ID, null, null);
        //ACT&VERIFY
        mockMvc.perform(post("/auth/verification-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", documentsDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If documents are empty then return BAD REQUEST")
    void uploadingDocuments_ifDocumentsEmpty_thenReturnBadRequest() throws Exception {
        //ARRANGE
        MultipartFile emptyPage3 = new MockMultipartFile("passport_page_3.jpg", new byte[]{});
        MultipartFile emptyRegistrationPage = new MockMultipartFile("passport_registration_page.jpg", new byte[]{});
        VerificationDocumentsDto documentsDto = VerificationDocumentsDto.builder()
                .page3(emptyPage3)
                .registrationPage(emptyRegistrationPage)
                .build();
        doThrow(IncorrectParameterException.class)
                .when(documentService).uploadingDocuments(CLIENT_ID, emptyPage3, emptyRegistrationPage);
        //ACT&VERIFY
        mockMvc.perform(post("/auth/verification-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", documentsDto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If documents have already been existed then return CONFLICT")
    void uploadingDocuments_ifDocumentsAlreadyExisted_thenReturnConflict() throws Exception {
        //ARRANGE
        doThrow(DocumentsAlreadyExistException.class)
                .when(documentService).uploadingDocuments(CLIENT_ID, page3, registrationPage);
        //ACT&VERIFY
        mockMvc.perform(post("/auth/verification-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .flashAttr("files", verificationDocumentsDto))
                .andExpect(status().isConflict());
    }

    private String asJsonString(Object obj) throws JsonProcessingException {

        return new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {

        assertThat(actualBody).isEqualTo(expectedBody);
    }
}

package by.afinny.userservice.unit.controller.insurance;

import by.afinny.userservice.controller.DocumentController;
import by.afinny.userservice.controller.insurance.InsuranceDocumentController;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.insurance.InsuranceDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(InsuranceDocumentController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InsuranceDocumentControllerTest {

    @MockBean
    private InsuranceDocumentService insuranceDocumentService;

    private final UUID CLIENT_ID = UUID.fromString("9b81ee52-2c0d-4bda-90b4-0b12e9d6f467");

    private MockMvc mockMvc;
    private MockMultipartFile file;
    private AutoInsuranceDocumentDto autoInsuranceDocumentDto;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(new InsuranceDocumentController(insuranceDocumentService))
                .setControllerAdvice(ExceptionHandlerController.class).build();
        file = new MockMultipartFile("passportOwnerPage",
                "passport_owner_page.jpg",
                "image/jpeg",
                "Hello, World!".getBytes());
        autoInsuranceDocumentDto = AutoInsuranceDocumentDto.builder()
                .documentName("passportOwnerPage")
                .file(file)
                .build();
    }

    @Test
    @DisplayName("If successfully saved auto insurance documents then don't return content")
    void uploadingAutoInsuranceDocuments_shouldNotReturnContent() throws Exception {
        //ACT&VERIFY
        mockMvc.perform(post("/auth/insurance-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .sessionAttr("file", autoInsuranceDocumentDto))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If auto insurance documents saving has been failed then return INTERNAL SERVER ERROR")
    void uploadingAutoInsuranceDocuments_ifDocumentsNotSaved_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(insuranceDocumentService).uploadingDocument(any(UUID.class), any());
        //ACT&VERIFY
        mockMvc.perform(post("/auth/insurance-documents/new")
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .sessionAttr("file", autoInsuranceDocumentDto))
                .andExpect(status().isInternalServerError());
    }

}

package by.afinny.userservice.unit.controller;

import by.afinny.userservice.controller.insurance.InsuranceDocumentController;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.insurance.InsuranceDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(InsuranceDocumentController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InsuranceDocumentControllerTest {

    @MockBean
    private InsuranceDocumentService insuranceDocumentService;

    private final UUID documentId = UUID.randomUUID();

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(new InsuranceDocumentController(insuranceDocumentService))
                .setControllerAdvice(ExceptionHandlerController.class).build();
    }

    @Test
    @DisplayName("if document deleted return ok")
    void deleteDocument() throws Exception {
        mockMvc.perform(delete("/auth/insurance-documents/" + documentId))
                .andExpect(status().is(204));
    }

}
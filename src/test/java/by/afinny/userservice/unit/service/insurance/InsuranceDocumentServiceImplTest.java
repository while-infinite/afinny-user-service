package by.afinny.userservice.unit.service.insurance;

import by.afinny.userservice.document.AutoInsuranceDocument;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;
import by.afinny.userservice.repository.mongodb.InsuranceDocumentRepository;
import by.afinny.userservice.service.insurance.impl.InsuranceDocumentServiceImpl;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class InsuranceDocumentServiceImplTest {

    @InjectMocks
    private InsuranceDocumentServiceImpl insuranceDocumentServiceImpl;
    @Mock
    private InsuranceDocumentRepository insuranceDocumentRepository;

    private final UUID CLIENT_ID = UUID.randomUUID();

    private MockMultipartFile file;
    private AutoInsuranceDocumentDto autoInsuranceDocumentDto;

    @Captor
    private ArgumentCaptor<AutoInsuranceDocument> autoInsuranceCaptor;

    @BeforeEach
    public void setUp() throws IOException {
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
    @DisplayName("Verifying that the submitted auto insurance documents have been saved")
    void uploadingAutoInsuranceDocuments_shouldSaveDocuments() throws IOException {
        //ACT
        insuranceDocumentServiceImpl.uploadingDocument(CLIENT_ID, autoInsuranceDocumentDto);
        //VERIFY
        verify(insuranceDocumentRepository).save(autoInsuranceCaptor.capture());
        assertThat(autoInsuranceCaptor.getValue().getFile()).isEqualTo(autoInsuranceDocumentDto.getFile().getBytes());
    }

    @Test
    @DisplayName("Verifying that the submitted auto insurance documents were not saved")
    void uploadingAutoInsuranceDocuments_ifDocumentsNotSaved_thenThrow() {
        //ARRANGE
        when(insuranceDocumentRepository.save(any(AutoInsuranceDocument.class))).thenThrow(RuntimeException.class);
        //ACT
        ThrowableAssert.ThrowingCallable uploadingAutoInsuranceDocumentsMethod = () ->
                insuranceDocumentServiceImpl.uploadingDocument(CLIENT_ID, autoInsuranceDocumentDto);
        //VERIFY
        assertThatThrownBy(uploadingAutoInsuranceDocumentsMethod).isNotNull();
    }

}

package by.afinny.userservice.unit.service;

import by.afinny.userservice.document.AutoInsuranceDocument;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.repository.mongodb.InsuranceDocumentRepository;
import by.afinny.userservice.service.insurance.impl.InsuranceDocumentServiceImpl;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class InsuranceDocumentServiceImplTest {

    @InjectMocks
    private InsuranceDocumentServiceImpl insuranceDocumentService;
    @Mock
    private InsuranceDocumentRepository insuranceDocumentRepository;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID DOCUMENT_ID = UUID.randomUUID();

    private MockMultipartFile file;
    private AutoInsuranceDocumentDto autoInsuranceDocumentDto;
    private AutoInsuranceDocument autoInsuranceDocument;

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
        try {
            autoInsuranceDocument = AutoInsuranceDocument.builder()
                    .id(UUID.randomUUID())
                    .clientId(CLIENT_ID)
                    .documentName(autoInsuranceDocumentDto.getDocumentName())
                    .creationDate(LocalDate.now())
                    .file(autoInsuranceDocumentDto.getFile().getBytes())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("if document exist then delete and return ok")
    void deleteDocument_shouldDelete() {
        //ARRANGE
        Optional<AutoInsuranceDocument> optionalDocument = Optional.of(autoInsuranceDocument);
        when(insuranceDocumentRepository.findById(DOCUMENT_ID)).thenReturn(optionalDocument);

        //ACT
        insuranceDocumentService.deleteDocument(DOCUMENT_ID);

        //VERIFY
        verify(insuranceDocumentRepository).deleteById(DOCUMENT_ID);
    }

    @Test
    @DisplayName("If document for deleting was not found then throw exception")
    void deleteDocument_ifDocumentNotFound_thenThrow(){
        //ARRANGE
        when(insuranceDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowableAssert.ThrowingCallable changeLimitMethod = () -> insuranceDocumentService.deleteDocument(DOCUMENT_ID);

        //VERIFY
        assertThatThrownBy(changeLimitMethod).isInstanceOf(EntityNotFoundException.class);
        verify(insuranceDocumentRepository, never()).deleteById(DOCUMENT_ID);
    }

}
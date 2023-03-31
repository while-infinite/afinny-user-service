package by.afinny.userservice.unit.service;

import by.afinny.userservice.document.VerificationDocument;
import by.afinny.userservice.dto.ResponseVerificationDocumentsDto;
import by.afinny.userservice.exception.DocumentsAlreadyExistException;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.IncorrectParameterException;
import by.afinny.userservice.repository.mongodb.VerificationDocumentRepository;
import by.afinny.userservice.service.impl.DocumentServiceImpl;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class DocumentServiceImplTest {

    @InjectMocks
    private DocumentServiceImpl documentService;
    @Mock
    private VerificationDocumentRepository documentRepository;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID DOCUMENT_ID = UUID.randomUUID();

    private MockMultipartFile page3;
    private MockMultipartFile registrationPage;
    private VerificationDocument documentPage3;
    private VerificationDocument documentRegistrationPage;

    private List<VerificationDocument> verifications;
    @Captor
    private ArgumentCaptor<List<VerificationDocument>> verificationsCaptor;

    @BeforeEach
    public void setUp() throws IOException {
        page3 = new MockMultipartFile("page3",
                "passport_page_3.jpg",
                "image/jpeg",
                "Hello, World!".getBytes());
        registrationPage = new MockMultipartFile("registrationPage ",
                "passport_registration_page.jpg",
                "image/jpeg",
                "Hello, World!".getBytes());
        verifications = List.of(createVerification(CLIENT_ID, page3), createVerification(CLIENT_ID, registrationPage));
        documentPage3 = VerificationDocument.builder()
                .id(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .documentName("page3")
                .build();
        documentRegistrationPage = VerificationDocument.builder()
                .id(UUID.randomUUID())
                .clientId(CLIENT_ID)
                .documentName("registrationPage")
                .build();
    }

    @Test
    @DisplayName("If verifying documents are null then throw exception")
    void uploadingDocuments_ifDocumentsNull_thenThrow() {
        //ACT
        ThrowingCallable uploadingDocumentsMethod =
                () -> documentService.uploadingDocuments(CLIENT_ID, null, null);
        //ASSERT
        assertThatThrownBy(uploadingDocumentsMethod)
                .isNotNull()
                .isInstanceOf(IncorrectParameterException.class)
                .hasMessage("Verification documents are null or empty");
    }

    @Test
    @DisplayName("If verifying documents are empty then throw exception")
    void uploadingDocuments_ifDocumentsEmpty_thenThrow() {
        //ARRANGE
        MultipartFile emptyPage3 = new MockMultipartFile("passport_page_3.jpg", new byte[]{});
        MultipartFile emptyRegistrationPage =
                new MockMultipartFile("passport_registration_page.jpg", new byte[]{});
        //ACT
        ThrowingCallable uploadingDocumentsMethod =
                () -> documentService.uploadingDocuments(CLIENT_ID, emptyPage3, emptyRegistrationPage);
        //ASSERT
        assertThatThrownBy(uploadingDocumentsMethod)
                .isNotNull()
                .isInstanceOf(IncorrectParameterException.class)
                .hasMessage("Verification documents are null or empty");
    }

    @Test
    @DisplayName("If verifying documents have already been uploaded then throw exception")
    void uploadingDocuments_ifDocumentsAlreadyUploaded_thenThrow() {
        //ARRANGE
        when(documentRepository.findByClientId(CLIENT_ID))
                .thenReturn(List.of(documentPage3, documentRegistrationPage));
        //ACT
        ThrowingCallable uploadingDocumentsMethod =
                () -> documentService.uploadingDocuments(CLIENT_ID, page3, registrationPage);
        //ASSERT
        assertThatThrownBy(uploadingDocumentsMethod)
                .isNotNull()
                .isInstanceOf(DocumentsAlreadyExistException.class)
                .hasMessage("Verification documents have already been uploaded");
    }

    @Test
    @DisplayName("Verifying that the submitted documents for verification have been saved")
    void uploadingDocuments_shouldSaveDocuments() throws Exception {
        //ACT
        documentService.uploadingDocuments(CLIENT_ID, page3, registrationPage);
        //VERIFY
        verify(documentRepository).saveAll(verificationsCaptor.capture());
        verifications.get(0).setId(verificationsCaptor.getValue().get(0).getId());
        verifications.get(1).setId(verificationsCaptor.getValue().get(1).getId());
        assertThat(verificationsCaptor.getValue().toString()).isEqualTo(verifications.toString());
    }

    @Test
    @DisplayName("Verifying that the submitted documents for verification have been saved")
    void uploadingDocuments_ifDocumentsNotSaved_thenThrow() {
        //ARRANGE
        when(documentRepository.saveAll(any(List.class))).thenThrow(RuntimeException.class);
        //ACT
        ThrowingCallable uploadingDocumentsMethod = () -> documentService.uploadingDocuments(CLIENT_ID, page3, registrationPage);
        //VERIFY
        assertThatThrownBy(uploadingDocumentsMethod).isNotNull();
    }

    @Test
    @DisplayName("if document exist then delete and return ok")
    void deleteDocument_shouldDelete() {
        //ARRANGE
        Optional<VerificationDocument> optionalDocument = Optional.of(documentPage3);
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(optionalDocument);

        //ACT
        documentService.deleteDocument(DOCUMENT_ID);

        //VERIFY
        verify(documentRepository).deleteById(DOCUMENT_ID);
    }

    @Test
    @DisplayName("If document for deleting was not found then throw exception")
    void deleteDocument_ifDocumentNotFound_thenThrow(){
        //ARRANGE
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowableAssert.ThrowingCallable changeLimitMethod = () -> documentService.deleteDocument(DOCUMENT_ID);

        //VERIFY
        assertThatThrownBy(changeLimitMethod).isInstanceOf(EntityNotFoundException.class);
        verify(documentRepository, never()).deleteById(DOCUMENT_ID);
    }

    @Test
    @DisplayName("If client has no any documents then throw")
    void getDocuments_shouldThrow() {
        //ARRANGE
        when(documentRepository.findByClientIdAndDocumentNameLike(CLIENT_ID, "page3")).thenReturn(null);
        when(documentRepository.findByClientIdAndDocumentNameLike(CLIENT_ID, "registrationPage")).thenReturn(null);

        //ACT
        ThrowableAssert.ThrowingCallable changeLimitMethod = () -> documentService.getDocuments(CLIENT_ID);

        //VERIFY
        assertThatThrownBy(changeLimitMethod).isInstanceOf(EntityNotFoundException.class);

    }

    @Test
    @DisplayName("If at least one document exists then return dto")
    void getDocuments_shouldReturnDto() {
        //ARRANGE
        when(documentRepository.findByClientIdAndDocumentNameLike(CLIENT_ID, "page3")).thenReturn(documentPage3);
        when(documentRepository.findByClientIdAndDocumentNameLike(CLIENT_ID, "registrationPage")).thenReturn(documentRegistrationPage);

        //ACT
        ResponseVerificationDocumentsDto documentsDto = documentService.getDocuments(CLIENT_ID);

        //VERITY
        verifyDocuments(documentsDto);
    }

    private void verifyDocuments(ResponseVerificationDocumentsDto documentsDto) {

        assertThat(documentsDto.getPage3()).isEqualTo(documentPage3);
        assertThat(documentsDto.getRegistrationPage()).isEqualTo(documentRegistrationPage);
    }

    private VerificationDocument createVerification (UUID clientId, MockMultipartFile verificationDocument)
            throws IOException {
        return VerificationDocument.builder()
                .clientId(clientId)
                .creationDate(LocalDate.now())
                .fileFormat(verificationDocument.getContentType())
                .documentName(verificationDocument.getName())
                .file(verificationDocument.getBytes())
                .build();
    }
}
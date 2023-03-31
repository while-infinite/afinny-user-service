package by.afinny.userservice.service.impl;

import by.afinny.userservice.document.VerificationDocument;
import by.afinny.userservice.dto.ResponseVerificationDocumentsDto;
import by.afinny.userservice.exception.DocumentsAlreadyExistException;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.IncorrectParameterException;
import by.afinny.userservice.repository.mongodb.VerificationDocumentRepository;
import by.afinny.userservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final VerificationDocumentRepository documentRepository;

    @Override
    public void deleteDocument(UUID documentId) {

        log.info("deleteDocument() method invoke");
        if (documentRepository.findById(documentId).isPresent()) {
            documentRepository.deleteById(documentId);
        } else {
            throw new EntityNotFoundException("document with id " + documentId + " wasn't found");
        }
    }

    @Override
    public ResponseVerificationDocumentsDto getDocuments(UUID clientID) {

        log.info("getDocuments() method invoked");
        ResponseVerificationDocumentsDto documents =  ResponseVerificationDocumentsDto.builder()
                .page3(documentRepository.findByClientIdAndDocumentNameLike(clientID, "page3"))
                .registrationPage(documentRepository.findByClientIdAndDocumentNameLike(clientID, "registrationPage"))
                .build();
        if (documents.getPage3() == null && documents.getRegistrationPage() == null) {
            throw new EntityNotFoundException("client has not uploaded documents yet");
        } else {
            return documents;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadingDocuments(UUID clientId, MultipartFile page3, MultipartFile registrationPage) throws IOException {
        log.info("uploadingDocuments() is invoked");
        if (page3 == null || page3.isEmpty()
                || registrationPage == null || registrationPage.isEmpty()) {
            throw new IncorrectParameterException("Verification documents are null or empty");
        }
        List<VerificationDocument> documents = documentRepository.findByClientId(clientId);
        if (!documents.isEmpty()) {
            throw new DocumentsAlreadyExistException("Verification documents have already been uploaded", clientId);
        }
        documentRepository.saveAll(List.of(createVerification(clientId, page3),
                createVerification(clientId, registrationPage)));
    }

    private VerificationDocument createVerification(UUID clientId, MultipartFile verificationDocument)
            throws IOException {
        return VerificationDocument.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .creationDate(LocalDate.now())
                .fileFormat(verificationDocument.getContentType())
                .documentName(verificationDocument.getName())
                .file(verificationDocument.getBytes())
                .build();
    }
}

package by.afinny.userservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import by.afinny.userservice.dto.ResponseVerificationDocumentsDto;

import java.util.UUID;

public interface DocumentService {

    void uploadingDocuments(UUID clientId, MultipartFile page3, MultipartFile registrationPage)
            throws IOException;
    void deleteDocument(UUID documentId);

    ResponseVerificationDocumentsDto getDocuments(UUID clientID);
}

package by.afinny.userservice.controller;

import by.afinny.userservice.dto.ResponseVerificationDocumentsDto;
import by.afinny.userservice.dto.VerificationDocumentsDto;
import by.afinny.userservice.service.DocumentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("auth/verification-documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    public final static String VERIFICATION_DOCUMENTS_URL = "/auth/verification-documents";
    public final static String UPLOADING_DOCUMENT_URL = "/new";
    public final static String DELETE_DOCUMENT_URL = "/{documentId}";
    public final static String GET_DOCUMENT_URL = "/{clientId}";


    @PostMapping(value = "new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadingDocuments(@RequestParam UUID clientId,
                                                   @ModelAttribute("files") VerificationDocumentsDto verificationDocumentsDto)
            throws IOException {
        documentService.uploadingDocuments(clientId, verificationDocumentsDto.getPage3(),
                verificationDocumentsDto.getRegistrationPage());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("{clientId}")
    public ResponseEntity<ResponseVerificationDocumentsDto> getDocuments(@PathVariable UUID clientId) {
        return ResponseEntity.ok(documentService.getDocuments(clientId));
    }
}

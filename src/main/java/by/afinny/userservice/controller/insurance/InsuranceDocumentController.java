package by.afinny.userservice.controller.insurance;

import by.afinny.userservice.service.insurance.InsuranceDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("auth/insurance-documents")
@RequiredArgsConstructor
public class InsuranceDocumentController {

    public final static String DELETE_DOCUMENT_URL = "/auth/insurance-documents/{documentId}";
    public final static String NEW_DOCUMENT_URL = "/auth/insurance-documents/new";

    private final InsuranceDocumentService insuranceDocumentsService;

    @DeleteMapping("{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {

        insuranceDocumentsService.deleteDocument(documentId);
        return ResponseEntity.status(204).build();
    }

    @PostMapping(value = "new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadingDocuments(@RequestParam UUID clientId,
                                                   @ModelAttribute(value = "file") AutoInsuranceDocumentDto autoInsuranceDocument) {
        insuranceDocumentsService.uploadingDocument(clientId, autoInsuranceDocument);
        return ResponseEntity.ok().build();
    }

}

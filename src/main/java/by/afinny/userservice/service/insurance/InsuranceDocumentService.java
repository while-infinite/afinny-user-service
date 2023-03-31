package by.afinny.userservice.service.insurance;

import by.afinny.userservice.document.AutoInsuranceDocument;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;

import java.util.UUID;

public interface InsuranceDocumentService {

    void deleteDocument(UUID documentId);

    void uploadingDocument(UUID clientId, AutoInsuranceDocumentDto autoInsuranceDocumentDto);

}

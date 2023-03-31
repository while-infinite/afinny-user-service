package by.afinny.userservice.service.insurance.impl;

import by.afinny.userservice.document.AutoInsuranceDocument;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.dto.insurance.AutoInsuranceDocumentDto;
import by.afinny.userservice.repository.mongodb.InsuranceDocumentRepository;
import by.afinny.userservice.service.insurance.InsuranceDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceDocumentServiceImpl implements InsuranceDocumentService {

    private final InsuranceDocumentRepository insuranceDocumentRepository;

    @Override
    public void deleteDocument(UUID documentId) {

        log.info("deleteDocument() method invoke");
        if (insuranceDocumentRepository.findById(documentId).isPresent()) {
            insuranceDocumentRepository.deleteById(documentId);
        } else {
            throw new EntityNotFoundException("document with id " + documentId + " wasn't found");
        }
    }

    public void uploadingDocument(UUID clientId, AutoInsuranceDocumentDto autoInsuranceDocumentDto) {
        log.info("uploadingDocuments() is invoked");
        try {
            insuranceDocumentRepository.save(createInsuranceDocument(clientId, autoInsuranceDocumentDto));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AutoInsuranceDocument createInsuranceDocument(UUID clientId, AutoInsuranceDocumentDto autoInsuranceDocumentDto)
            throws IOException {
        return AutoInsuranceDocument.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .documentName(autoInsuranceDocumentDto.getDocumentName())
                .creationDate(LocalDate.now())
                .file(autoInsuranceDocumentDto.getFile().getBytes())
                .build();
    }
}

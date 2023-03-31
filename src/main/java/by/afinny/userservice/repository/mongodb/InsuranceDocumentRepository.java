package by.afinny.userservice.repository.mongodb;

import by.afinny.userservice.document.AutoInsuranceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface InsuranceDocumentRepository extends MongoRepository<AutoInsuranceDocument, UUID> {

}

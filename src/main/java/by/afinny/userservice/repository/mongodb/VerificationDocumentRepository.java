package by.afinny.userservice.repository.mongodb;

import by.afinny.userservice.document.VerificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationDocumentRepository extends MongoRepository<VerificationDocument, UUID> {

    void deleteById(UUID documentId);
    List<VerificationDocument> findByClientId(UUID clientId);
    VerificationDocument findByClientIdAndDocumentNameLike(UUID clientId, String documentName);
}

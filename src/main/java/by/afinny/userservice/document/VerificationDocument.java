package by.afinny.userservice.document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "Verification")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter(AccessLevel.PUBLIC)
public class VerificationDocument {

    @Id
    private UUID id;
    private UUID clientId;
    private LocalDate creationDate;
    private String documentName;
    private String fileFormat;
    private byte[] file;
}

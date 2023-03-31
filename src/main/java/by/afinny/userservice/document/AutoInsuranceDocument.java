package by.afinny.userservice.document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "auto_insurance")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class AutoInsuranceDocument {

    @Id
    private UUID id;
    private UUID clientId;
    private String documentName;
    private LocalDate creationDate;
    private byte[] file;

}

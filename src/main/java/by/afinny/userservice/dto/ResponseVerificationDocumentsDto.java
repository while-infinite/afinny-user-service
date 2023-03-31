package by.afinny.userservice.dto;

import by.afinny.userservice.document.VerificationDocument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class ResponseVerificationDocumentsDto {

    VerificationDocument page3;
    VerificationDocument registrationPage;
}

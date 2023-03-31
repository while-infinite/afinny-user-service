package by.afinny.userservice.dto;

import by.afinny.userservice.entity.ClientStatus;
import java.time.LocalDate;
import java.util.UUID;
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
public class ResponseNonClientDto {

    private UUID id;
    private Boolean smsNotification;
    private Boolean pushNotification;
    private String mobilePhone;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String passportNumber;
    private String securityQuestion;
    private String securityAnswer;
    private String email;
    private ClientStatus clientStatus;
    private Boolean countryOfResidence;
    private LocalDate appRegistrationDate;
    private LocalDate accessionDate;
}

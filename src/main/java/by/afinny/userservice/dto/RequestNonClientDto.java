package by.afinny.userservice.dto;

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
public class RequestNonClientDto {

    private String mobilePhone;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String passportNumber;
    private Boolean countryOfResidence;
}

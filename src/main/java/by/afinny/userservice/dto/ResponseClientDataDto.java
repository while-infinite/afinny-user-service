package by.afinny.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseClientDataDto {

    private String firstName;
    private String lastName;
    private String middleName;
    private String mobilePhone;
    private String email;
    private String passportNumber;
    private String clientId;
    private String clientStatus;
    private Boolean countryOfResidence;
}

package by.afinny.userservice.dto;

import by.afinny.userservice.entity.ClientStatus;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class ResponseClientDto {

    private UUID id;
    private String mobilePhone;
    private ClientStatus clientStatus;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private String email;
    private Boolean smsNotification;
    private Boolean pushNotification;
    private LocalDate appRegistrationDate;
}


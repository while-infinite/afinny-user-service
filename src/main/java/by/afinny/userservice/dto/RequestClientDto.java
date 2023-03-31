package by.afinny.userservice.dto;

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
public class RequestClientDto {

    private UUID id;
    private String mobilePhone;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private String email;
}

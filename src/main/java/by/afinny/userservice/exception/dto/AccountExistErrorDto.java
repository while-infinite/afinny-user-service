package by.afinny.userservice.exception.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PUBLIC)
@Getter
public class AccountExistErrorDto extends ErrorDto {

    private final String clientStatus;

    public AccountExistErrorDto(String clientStatus, String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.clientStatus = clientStatus;
    }
}

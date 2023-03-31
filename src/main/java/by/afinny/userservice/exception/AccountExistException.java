package by.afinny.userservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
public class AccountExistException extends RegistrationException {

  private final String status;

  public AccountExistException(String errorCode, String errorMessage, String status) {
    super(errorCode, errorMessage);
    this.status = status;
  }
}


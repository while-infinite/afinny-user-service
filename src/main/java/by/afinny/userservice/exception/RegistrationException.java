package by.afinny.userservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class RegistrationException extends RuntimeException {

  private final String errorCode;
  private final String errorMessage;
}

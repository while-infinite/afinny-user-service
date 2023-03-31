package by.afinny.userservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
public class VerificationCodeException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
}
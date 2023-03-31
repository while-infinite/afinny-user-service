package by.afinny.userservice.exception.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class VerificationErrorDto {
    private final String errorMessage;
}
package by.afinny.userservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter(AccessLevel.PUBLIC)
public class DocumentsAlreadyExistException extends RuntimeException {
    private final UUID clientId;

    public DocumentsAlreadyExistException(String message, UUID clientId) {
        super(message);
        this.clientId = clientId;
    }
}

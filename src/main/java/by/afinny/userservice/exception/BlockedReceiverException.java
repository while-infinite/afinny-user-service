package by.afinny.userservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
@AllArgsConstructor
public class BlockedReceiverException extends RuntimeException {

    public static final String BLOCK_SECONDS_FIELD = "blockSeconds";

    private Long blockSeconds;
}

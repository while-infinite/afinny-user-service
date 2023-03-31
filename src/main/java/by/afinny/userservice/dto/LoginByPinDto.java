package by.afinny.userservice.dto;

import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
public class LoginByPinDto {

    private String fingerprint;
    private UUID clientId;
}

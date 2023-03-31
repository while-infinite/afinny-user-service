package by.afinny.userservice.dto;

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
public class NotificationDto {

    private String email;
    private Boolean smsNotification;
    private Boolean pushNotification;
    private Boolean emailSubscription;
}

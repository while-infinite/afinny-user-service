package by.afinny.userservice.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(name = Verification.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class Verification {

    public static final String TABLE_NAME = "verification";

    @Id
    @Column(name = "mobile_phone", length = 11)
    private String mobilePhone;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "code_expiration")
    private LocalDateTime codeExpiration;

    @Column(name = "wrong_attempts_counter")
    private Integer wrongAttemptsCounter;

    @Column(name = "user_block_expiration")
    private LocalDateTime userBlockExpiration;

}


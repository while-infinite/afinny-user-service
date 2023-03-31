package by.afinny.userservice.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = SmsBlockSending.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class SmsBlockSending {

    public static final String TABLE_NAME = "sms_block_sending";


    @Id
    @Column(name = "mobile_phone", length = 11)
    private String mobilePhone;

    @Column(name = "sending_count", nullable = false)
    private Integer sendingCount;

    @Column(name = "sms_block_expiration")
    private LocalDateTime smsBlockExpiration;
}

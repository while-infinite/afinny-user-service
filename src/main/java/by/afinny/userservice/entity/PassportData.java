package by.afinny.userservice.entity;

import javax.persistence.Column;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = PassportData.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class PassportData {

    public static final String TABLE_NAME = "passport_data";

    @Id
    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "issuance_date")
    private Date issuanceDate;

    @Column(name = "expiry_date")
    private Date expiryDate;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "birth_date")
    private Date birthDate;
}


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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = Client.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class Client {

    public static final String TABLE_NAME = "client";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "middle_name", nullable = false, length = 30)
    private String middleName;

    @Column(name = "last_name", length = 30)
    private String lastName;

    @Column(name = "country_of_residence")
    private Boolean countryOfResidence;

    @Column(name = "accession_date", nullable = false)
    private LocalDate accessionDate;

    @Column(name = "mobile_phone", nullable = false, length = 11)
    private String mobilePhone;

    @Column(name = "employer_identification_number", nullable = false, length = 30)
    private String employerIdentificationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_status", nullable = false)
    private ClientStatus clientStatus;

    @OneToOne
    @JoinColumn(name = "passport_number", nullable = false)
    private PassportData passportData;
}


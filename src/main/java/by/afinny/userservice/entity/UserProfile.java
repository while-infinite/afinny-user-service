package by.afinny.userservice.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = UserProfile.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class UserProfile {

    public static final String TABLE_NAME = "user_profile";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sms_notification", nullable = false)
    private Boolean smsNotification;

    @Column(name = "push_notification")
    private Boolean pushNotification;

    @Column(name = "email_subscription")
    private Boolean emailSubscription;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "security_question", length = 50, nullable = false)
    private String securityQuestion;

    @Column(name = "security_answer", length = 50, nullable = false)
    private String securityAnswer;

    @Column(name = "app_registration_date")
    private LocalDate appRegistrationDate;

    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    @JoinColumn(name = "client_id", unique = true)
    private Client client;
}


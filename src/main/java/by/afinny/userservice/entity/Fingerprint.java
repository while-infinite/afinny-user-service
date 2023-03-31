package by.afinny.userservice.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = Fingerprint.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class Fingerprint {

    public static final String TABLE_NAME = "fingerprint";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "fingerprint", nullable = false, length = 32)
    private String fingerprint;
}

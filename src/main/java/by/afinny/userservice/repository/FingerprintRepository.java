package by.afinny.userservice.repository;

import by.afinny.userservice.entity.Fingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FingerprintRepository extends JpaRepository<Fingerprint, UUID> {

    Optional<Fingerprint> findByClientIdAndFingerprint(UUID clientId, String fingerprint);
}

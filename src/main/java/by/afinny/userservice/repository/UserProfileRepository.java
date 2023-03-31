package by.afinny.userservice.repository;

import by.afinny.userservice.entity.UserProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByClientMobilePhone(String phone);

    Optional<UserProfile> findByClientPassportDataPassportNumber(String passportData);

    Optional<UserProfile> findByClientId(UUID clientId);
}


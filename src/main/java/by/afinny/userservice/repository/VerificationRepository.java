package by.afinny.userservice.repository;

import by.afinny.userservice.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, String> {

    Optional<Verification> findByMobilePhone (String receiver);
}


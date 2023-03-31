package by.afinny.userservice.repository;

import by.afinny.userservice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

  Optional<Client> findClientByMobilePhone(String phone);

  Optional<Client> findClientById(UUID id);

  Optional<Client> findClientByPassportDataPassportNumber(String passportNumber);
}


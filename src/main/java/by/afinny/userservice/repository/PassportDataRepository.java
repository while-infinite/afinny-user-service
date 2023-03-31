package by.afinny.userservice.repository;

import by.afinny.userservice.entity.PassportData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassportDataRepository extends JpaRepository<PassportData, String> {
}

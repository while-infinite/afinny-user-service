package by.afinny.userservice.repository;

import by.afinny.userservice.entity.SmsBlockSending;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsBlockSendingRepository extends JpaRepository<SmsBlockSending, String> {
}

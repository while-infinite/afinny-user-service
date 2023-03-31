package by.afinny.userservice.openfeign.credit;

import by.afinny.userservice.dto.credit.CreditDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "CREDIT/auth/credits")
public interface CreditClient {

    @GetMapping
    ResponseEntity<List<CreditDto>> getActiveCredits(@RequestParam UUID clientId);
}

package by.afinny.userservice.openfeign.deposit;

import by.afinny.userservice.dto.deposit.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "DEPOSIT/accounts")
public interface AccountClient {

    @GetMapping
    ResponseEntity<List<AccountDto>> getActiveAccounts(@RequestParam UUID clientId);
}

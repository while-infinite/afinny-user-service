package by.afinny.userservice.controller.deposit;

import by.afinny.userservice.dto.ClientByPhoneDto;
import by.afinny.userservice.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("client")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    public static final String GET_CLIENT_URL = "/client";
    public static final String MOBILE_PHONE_PARAMETER = "mobilePhone";

    public final ClientService clientService;

    @GetMapping
    ResponseEntity<ClientByPhoneDto> getClientByPhone(@RequestParam(name = "mobilePhone") String mobilePhone) {
        log.info("getClientByPhone() in controller method invoke");
        ClientByPhoneDto client = clientService.getClientByPhone(mobilePhone);
        return ResponseEntity.ok(client);
    }
}

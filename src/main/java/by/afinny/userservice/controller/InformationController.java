package by.afinny.userservice.controller;


import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.service.InformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("auth/information")
@RequiredArgsConstructor
public class InformationController {

    public static final String INFORMATION_URL = "/auth/information";
    public static final String CLIENT_ID_PARAMETER = "clientId";

    private final InformationService informationService;


    @GetMapping()
    public ResponseEntity<ResponseClientDataDto> getClientData(@RequestParam UUID clientId) {

        ResponseClientDataDto clientData = informationService.getClientData(clientId);
        return ResponseEntity.ok(clientData);
    }
}

package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.ClientByPhoneDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.mapper.ClientMapper;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    @Override
    public ClientByPhoneDto getClientByPhone(String mobilePhone) {
        log.info("getClientByPhone() method invoke");
        Client client = clientRepository.findClientByMobilePhone(mobilePhone)
                .orElseThrow(
                () -> new EntityNotFoundException("no client with the mobile phone " + mobilePhone + " was found"));
        return clientMapper.clientToClientByPhoneDto(client);
    }
}

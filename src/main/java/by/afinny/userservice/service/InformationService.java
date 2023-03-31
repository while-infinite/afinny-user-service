package by.afinny.userservice.service;

import by.afinny.userservice.dto.ResponseClientDataDto;

import java.util.UUID;

public interface InformationService {

    ResponseClientDataDto getClientData(UUID clientId);
}

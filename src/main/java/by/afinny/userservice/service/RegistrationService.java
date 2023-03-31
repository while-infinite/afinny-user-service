package by.afinny.userservice.service;


import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;

public interface RegistrationService {

  ClientDto verifyMobilePhone(String phone);

  void registerExistingClient(RequestClientDto user);

  void registerNonClient(RequestNonClientDto requestNonClientDto);

  void verifyPassportNumber(PassportDto passportNumber);
}
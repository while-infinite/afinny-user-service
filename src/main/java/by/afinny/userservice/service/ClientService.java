package by.afinny.userservice.service;

import by.afinny.userservice.dto.ClientByPhoneDto;

public interface ClientService {

    ClientByPhoneDto getClientByPhone(String mobilePhone);
}

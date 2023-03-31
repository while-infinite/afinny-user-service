package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.ClientByPhoneDto;
import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ClientMapper {

  ClientDto clientToDto(Client client);

  @Mapping(source = "client.id", target = "clientId")
  ClientByPhoneDto clientToClientByPhoneDto(Client client);
}


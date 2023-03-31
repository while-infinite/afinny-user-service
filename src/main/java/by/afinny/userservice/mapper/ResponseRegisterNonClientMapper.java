package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.ResponseNonClientDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ResponseRegisterNonClientMapper {

    @Mapping(source = "client.id", target = "id")
    @Mapping(source = "client.clientStatus", target = "clientStatus")
    @Mapping(source = "passportData.passportNumber", target = "passportNumber")
    ResponseNonClientDto toResponseClientDto(Client client, UserProfile userProfile, PassportData passportData);
}

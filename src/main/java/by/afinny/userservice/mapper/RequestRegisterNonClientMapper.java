package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper
public interface RequestRegisterNonClientMapper {

    UserProfile dtoToUserProfile(RequestNonClientDto requestNonClientDto);

    Client dtoToClient(RequestNonClientDto requestNonClientDto);

    PassportData dtoToPassportData(RequestNonClientDto requestNonClientDto);
}

package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.dto.ResponseClientDto;
import by.afinny.userservice.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserProfileMapper {

    @Mapping(source = "id", target = "client.id")
    @Mapping(source = "mobilePhone", target = "client.mobilePhone")
    @Mapping(ignore = true, target = "id")
    UserProfile requestClientDtoToUserProfile(RequestClientDto requestClientDto);

    @Mapping(source = "client.clientStatus", target = "clientStatus")
    @Mapping(source = "client.mobilePhone", target = "mobilePhone")
    @Mapping(source = "client.id", target = "id")
    ResponseClientDto userProfileToResponseClientDto(UserProfile userProfile);

    @Mapping(source = "client.firstName", target = "firstName")
    @Mapping(source = "client.lastName", target = "lastName")
    @Mapping(source = "client.middleName", target = "middleName")
    @Mapping(source = "client.mobilePhone", target = "mobilePhone")
    @Mapping(source = "client.passportData.passportNumber", target = "passportNumber")
    @Mapping(expression = "java(userProfile.getClient().getId().toString())",target = "clientId")
    @Mapping(source = "client.clientStatus", target = "clientStatus")
    @Mapping(source = "client.countryOfResidence", target = "countryOfResidence")
    ResponseClientDataDto userProfileToResponseClientDataDto(UserProfile userProfile);
}


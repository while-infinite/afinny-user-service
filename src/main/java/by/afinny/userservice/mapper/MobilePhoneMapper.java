package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.entity.Client;
import org.mapstruct.Mapper;

@Mapper
public interface MobilePhoneMapper {

    MobilePhoneDto toMobilePhoneDto(Client client);
}

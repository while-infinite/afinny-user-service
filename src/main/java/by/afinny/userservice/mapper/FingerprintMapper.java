package by.afinny.userservice.mapper;

import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.entity.Fingerprint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface FingerprintMapper {

    @Mapping(source = "clientId", target = "client.id")
    Fingerprint toFingerprint(FingerprintDto fingerprintDto);
}

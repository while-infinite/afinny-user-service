package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.mapper.UserProfileMapper;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.InformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InformationServiceImpl implements InformationService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public ResponseClientDataDto getClientData(UUID clientId) {
        log.info("getClient() method is invoked");
        UserProfile foundUserProfile = userProfileRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        return userProfileMapper.userProfileToResponseClientDataDto(foundUserProfile);
    }
}

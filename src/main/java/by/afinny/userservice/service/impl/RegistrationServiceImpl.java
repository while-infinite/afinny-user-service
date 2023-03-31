package by.afinny.userservice.service.impl;

import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.dto.credit.CreditDto;
import by.afinny.userservice.dto.deposit.AccountDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.entity.UserProfile;
import by.afinny.userservice.exception.AccountExistException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.mapper.ClientMapper;
import by.afinny.userservice.mapper.RequestRegisterNonClientMapper;
import by.afinny.userservice.mapper.ResponseRegisterNonClientMapper;
import by.afinny.userservice.mapper.UserProfileMapper;
import by.afinny.userservice.openfeign.credit.CreditClient;
import by.afinny.userservice.openfeign.deposit.AccountClient;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.repository.PassportDataRepository;
import by.afinny.userservice.repository.UserProfileRepository;
import by.afinny.userservice.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final ClientRepository clientRepository;
    private final UserProfileRepository userProfileRepository;
    private final PassportDataRepository passportDataRepository;

    private final ClientMapper clientMapper;
    private final UserProfileMapper userProfileMapper;
    private final RequestRegisterNonClientMapper requestRegisterNonClientMapper;
    private final ResponseRegisterNonClientMapper responseRegisterNonClientMapper;

    private final CreditClient creditClient;
    private final AccountClient accountClient;

    private final PasswordEncoder passwordEncoder;

    @Override
    public ClientDto verifyMobilePhone(String phone) {
        log.info("checkPhone() method is invoked");
        Client foundClient = clientRepository.findClientByMobilePhone(phone)
                .orElseGet(() -> createClientWithNotClientStatus(phone));

        if (isClient(foundClient) && isRegistered(foundClient)) {
            log.debug("The client is already registered in the app");
            throw new AccountExistException(
                    Integer.toString(HttpStatus.CONFLICT.value()),
                    "Account already exist!",
                    foundClient.getClientStatus().toString());
        }

        return clientMapper.clientToDto(foundClient);
    }

    @Override
    public void registerExistingClient(RequestClientDto user) {
        log.info("register() method is invoked");
        Client client = getVerifiedForRegistrationClientById(user.getId(), user.getMobilePhone());
        client.setClientStatus(defineClientStatus(user.getId()));
        UserProfile registeringUserProfile = userProfileMapper.requestClientDtoToUserProfile(user);
        userProfileRepository.findByClientId(user.getId())
                .ifPresent(userProfile -> registeringUserProfile.setId(userProfile.getId()));
        setDefaultSettings(registeringUserProfile);
        setEncodedPassword(user.getPassword(), registeringUserProfile);
        registeringUserProfile.setClient(client);
        userProfileRepository.save(registeringUserProfile);
    }

    @Transactional
    @Override
    public void registerNonClient(RequestNonClientDto requestNonClientDto) {
        log.info("registerNonClient() method is invoked");
        String encode = encodePassword(requestNonClientDto.getPassword());
        requestNonClientDto.setPassword(encode);
        log.debug("Encoded password: " + encode);

        PassportData passportData = requestRegisterNonClientMapper.dtoToPassportData(requestNonClientDto);
        UserProfile userProfile = requestRegisterNonClientMapper.dtoToUserProfile(requestNonClientDto);
        Client client = requestRegisterNonClientMapper.dtoToClient(requestNonClientDto);

        PassportData savedPassportData = passportDataRepository.save(passportData);
        log.debug("Saved passport data" + savedPassportData);
        setFieldsToClient(client, savedPassportData);

        Client savedClient = clientRepository.save(client);
        log.debug("Saved client" + savedClient);
        setFieldsToUserProfile(userProfile, savedClient);

        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        log.debug("Saved userProfile" + savedUserProfile);
    }

    @Override
    public void verifyPassportNumber(PassportDto passportDto) {
        log.info("verifyPassportNumber() method invoked");
        String passportNumber = passportDto.getPassportNumber();
        Optional<Client> optionalClient = clientRepository.findClientByPassportDataPassportNumber(passportNumber);
        if (optionalClient.isPresent()) {
            throw new RegistrationException(Integer.toString(HttpStatus.BAD_REQUEST.value()),
                    "Пользователь с таким номером паспорта уже существует. " +
                            "Позвоните по номеру телефона +7 321 321 5690 или обратитесь в ближайшее отделение Банка");
        }
    }

    private boolean isRegistered(Client foundClient) {
        return !foundClient.getClientStatus().equals(ClientStatus.NOT_REGISTERED);
    }

    private boolean isClient(Client foundClient) {
        return !foundClient.getClientStatus().equals(ClientStatus.NOT_CLIENT);
    }

    private Client createClientWithNotClientStatus(String phone) {
        Client client = new Client();
        client.setClientStatus(ClientStatus.NOT_CLIENT);
        client.setMobilePhone(phone);
        return client;
    }

    private Client getVerifiedForRegistrationClientById(UUID id, String mobilePhone) {
        Client client = clientRepository.findClientById(id).orElseThrow(() ->
                new RegistrationException(
                        Integer.toString(HttpStatus.BAD_REQUEST.value()),
                        "Client not found"));
        if (!client.getMobilePhone().equals(mobilePhone)) {
            throw new RegistrationException(
                    Integer.toString(HttpStatus.BAD_REQUEST.value()), "Client not found");
        }
        if (!client.getClientStatus().equals(ClientStatus.NOT_REGISTERED)) {
            throw new RegistrationException(
                    Integer.toString(HttpStatus.BAD_REQUEST.value()),
                    "Client already has been registered");
        }
        return client;
    }

    private void setFieldsToClient(Client client, PassportData passportData) {
        client.setPassportData(passportData);
        client.setClientStatus(ClientStatus.NOT_ACTIVE);
        client.setAccessionDate(LocalDate.now());
    }

    private void setFieldsToUserProfile(UserProfile userProfile, Client client) {
        userProfile.setAppRegistrationDate(LocalDate.now());
        userProfile.setSmsNotification(true);
        userProfile.setPushNotification(true);
        userProfile.setEmailSubscription(false);
        userProfile.setClient(client);
    }

    private void setEncodedPassword(String rawPassword, UserProfile registeringUserProfile) {
        String encodedPassword = encodePassword(rawPassword);
        registeringUserProfile.setPassword(encodedPassword);
        log.debug("Encoded password: " + encodedPassword);
    }

    private void setDefaultSettings(UserProfile registeringUserProfile) {
        registeringUserProfile.setAppRegistrationDate(LocalDate.now());
        log.debug("Established date app registration: " + registeringUserProfile.getAppRegistrationDate());
        registeringUserProfile.setSmsNotification(true);
        registeringUserProfile.setPushNotification(true);
        registeringUserProfile.setEmailSubscription(false);
    }

    private ClientStatus defineClientStatus(UUID clientId) {
        List<CreditDto> activeCredits = creditClient.getActiveCredits(clientId).getBody();
        List<AccountDto> activeAccounts = accountClient.getActiveAccounts(clientId).getBody();
        if ((activeCredits != null && activeCredits.isEmpty()) &&
                (activeAccounts != null && activeAccounts.isEmpty())) {
            return ClientStatus.NOT_ACTIVE;
        }
        return ClientStatus.ACTIVE;
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}


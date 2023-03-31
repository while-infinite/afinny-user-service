package by.afinny.userservice.unit.service;

import by.afinny.userservice.dto.ClientByPhoneDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.mapper.ClientMapper;
import by.afinny.userservice.mapper.ClientMapperImpl;
import by.afinny.userservice.repository.ClientRepository;
import by.afinny.userservice.service.impl.ClientServiceImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class ClientServiceImplTest {

    @InjectMocks
    private ClientServiceImpl clientService;
    @Mock
    private ClientRepository clientRepository;
    @Spy
    private ClientMapper clientMapper = new ClientMapperImpl();

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";
    private final String MOBILE_PHONE = "+79999999999";

    private ClientByPhoneDto clientByPhoneDto;
    private Client client;

    @BeforeEach
    public void setUp() {
        client = Client.builder()
                .id(CLIENT_ID)
                .firstName(FIRST_NAME)
                .middleName(MIDDLE_NAME)
                .lastName(LAST_NAME)
                .countryOfResidence(true)
                .mobilePhone(MOBILE_PHONE)
                .employerIdentificationNumber("1234567890")
                .build();
    }

    @Test
    @DisplayName("If client was found by mobile phone, then return clientByPhoneDto")
    public void getClientByPhone_shouldReturnClientByPhoneDto() {
        //Arrange
        when(clientRepository.findClientByMobilePhone(MOBILE_PHONE)).thenReturn(Optional.of(client));
        //ACT
        clientByPhoneDto = clientService.getClientByPhone(MOBILE_PHONE);
        //VERIFY
        verifyClientByPhoneDto(clientByPhoneDto);
    }

    @Test
    @DisplayName("If client wasn't found by mobile phone, then throw EntityNotFoundException")
    public void getClientByPhone_ifClientNotExists_thenThrowEntityNotFoundException() {
        //Arrange
        when(clientRepository.findClientByMobilePhone(MOBILE_PHONE)).thenThrow(EntityNotFoundException.class);
        //ACT
        ThrowableAssert.ThrowingCallable getClient = () -> clientService.getClientByPhone(MOBILE_PHONE);
        //VERIFY
        Assertions.assertThatThrownBy(getClient).isNotNull();
    }

    private void verifyClientByPhoneDto(ClientByPhoneDto clientDto) {
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto).isNotNull();
            softAssertions.assertThat(clientDto.getClientId()).isEqualTo(client.getId());
            softAssertions.assertThat(clientDto.getFirstName()).isEqualTo(client.getFirstName());
            softAssertions.assertThat(clientDto.getMiddleName()).isEqualTo(client.getMiddleName());
            softAssertions.assertThat(clientDto.getLastName()).isEqualTo(client.getLastName());
        });
    }
}

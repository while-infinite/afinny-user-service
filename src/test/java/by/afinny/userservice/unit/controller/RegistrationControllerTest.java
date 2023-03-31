package by.afinny.userservice.unit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import by.afinny.userservice.controller.RegistrationController;
import by.afinny.userservice.dto.ClientDto;
import by.afinny.userservice.dto.PassportDto;
import by.afinny.userservice.dto.RequestClientDto;
import by.afinny.userservice.dto.RequestNonClientDto;
import by.afinny.userservice.dto.ResponseClientDto;
import by.afinny.userservice.dto.ResponseNonClientDto;
import by.afinny.userservice.entity.ClientStatus;
import by.afinny.userservice.exception.AccountExistException;
import by.afinny.userservice.exception.RegistrationException;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.RegistrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(RegistrationController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegistrationControllerTest {

    @MockBean
    private RegistrationService registrationService;

    private final String MOBILE_PHONE = "79024327692";
    private final UUID CLIENT_ID = UUID.fromString("e6376def-e541-4f03-aa0e-b7fc6fe4e1aa");

    private ClientDto clientDto;
    private RequestClientDto requestClientDto;
    private ResponseClientDto responseClientDto;
    private RequestNonClientDto requestNonClientDto;
    private ResponseNonClientDto responseNonClientDto;
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        mockMvc = standaloneSetup(new RegistrationController(registrationService))
                .setControllerAdvice(ExceptionHandlerController.class).build();

        clientDto = ClientDto.builder()
                .id(CLIENT_ID)
                .mobilePhone(MOBILE_PHONE)
                .clientStatus(ClientStatus.NOT_REGISTERED).build();

        requestClientDto = RequestClientDto.builder()
                .id(CLIENT_ID)
                .mobilePhone(MOBILE_PHONE)
                .password("jzxc!23jsn")
                .securityQuestion("Месяц свадьбы родителей")
                .securityAnswer("Август")
                .email("pajaroni@teleg.eu").build();

        responseClientDto = ResponseClientDto.builder()
                .id(requestClientDto.getId())
                .mobilePhone(requestClientDto.getMobilePhone())
                .clientStatus(ClientStatus.ACTIVE)
                .password(requestClientDto.getPassword())
                .email(requestClientDto.getEmail())
                .smsNotification(true)
                .pushNotification(true).build();

        requestNonClientDto = RequestNonClientDto.builder()
                .mobilePhone(MOBILE_PHONE)
                .password("f)fsEFL__asw980")
                .securityQuestion("В каком году купил первую машину")
                .securityAnswer("2011")
                .email("ngnipalm@vusra.com")
                .firstName("Антон")
                .middleName("Викторович")
                .lastName("Горохов")
                .passportNumber("9237347017")
                .countryOfResidence(true).build();

        responseNonClientDto = ResponseNonClientDto.builder()
                .id(CLIENT_ID)
                .smsNotification(false)
                .pushNotification(false)
                .mobilePhone(requestNonClientDto.getMobilePhone())
                .password(requestNonClientDto.getPassword())
                .passportNumber(requestNonClientDto.getPassportNumber())
                .email(requestNonClientDto.getEmail())
                .clientStatus(ClientStatus.CLOSED).build();
    }

    @Test
    @DisplayName("If successfully verified phone number then return user")
    void verifyMobilePhone_shouldReturnUserDto() throws Exception {
        //ARRANGE
        when(registrationService.verifyMobilePhone(MOBILE_PHONE)).thenReturn(clientDto);
        //ACT
        MvcResult result = mockMvc.perform(
                        get("/registration")
                                .param("mobilePhone", MOBILE_PHONE))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyMobilePhoneParameter(result.getRequest().getParameter("mobilePhone"));
        verifyBody(asJsonString(clientDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If user already registered in the app then return CONFLICT")
    void verifyMobilePhone_ifUserExist_thenReturnConflict() throws Exception {
        //ARRANGE
        AccountExistException accountExistException = new AccountExistException("409",
                "Account already exist!",
                ClientStatus.NOT_ACTIVE.toString());
        when(registrationService.verifyMobilePhone(MOBILE_PHONE)).thenThrow(accountExistException);
        //ACT
        MvcResult result = mockMvc.perform(
                        get("/registration")
                                .param("mobilePhone", MOBILE_PHONE))
                .andExpect(status().isConflict())
                .andReturn();
        //VERIFY
        verifyMobilePhoneParameter(result.getRequest().getParameter("mobilePhone"));
    }

    @Test
    @DisplayName("If client has been successfully registered then return ok")
    void registerExistingClient_shouldReturnOk() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                        patch("/registration/user-profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestClientDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If registration has been failed then return BAD REQUEST")
    void registerExistingClient_ifRegistrationFailed_thenReturnBadRequest() throws Exception {
        //ARRANGE
        doThrow(new RegistrationException(
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "Bad Request"))
                .when(registrationService).registerExistingClient(any(RequestClientDto.class));
        //ACT & VERIFY
        mockMvc.perform(
                        patch("/registration/user-profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestClientDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If non client successfully registered then return client")
    void registerNonClient_shouldReturnOk() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                        post("/registration/user-profile/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestNonClientDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If registration for non client has been failed then return UNAUTHORIZED")
    void registerNonClient_ifRegistrationFailed_thenReturnUnauthorized() throws Exception {
        //ARRANGE
        doThrow(new RegistrationException(Integer.toString(HttpStatus.UNAUTHORIZED.value()),
                "Registration failure"))
                .when(registrationService).registerNonClient(any(RequestNonClientDto.class));
        //ACT & VERIFY
        ResultActions perform = mockMvc.perform(
                        post("/registration/user-profile/new")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestNonClientDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("If verifying proceed then return ok")
    void verifyPassportNumber_shouldReturnOk() throws Exception {

        mockMvc.perform(
                        post("/registration/user-profile/verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(requestClientDto)))
                .andExpect(status().isOk());
    }


    private void verifyMobilePhoneParameter(String mobilePhone) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(mobilePhone)
                    .withFailMessage("Mobile phone parameter should be set")
                    .isNotNull();
            softAssertions.assertThat(mobilePhone)
                    .withFailMessage("Mobile phone parameter should be " + MOBILE_PHONE + " instead of " + mobilePhone)
                    .isEqualTo(MOBILE_PHONE);
        });
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}

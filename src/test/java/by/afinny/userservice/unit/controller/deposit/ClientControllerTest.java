package by.afinny.userservice.unit.controller.deposit;

import by.afinny.userservice.controller.deposit.ClientController;
import by.afinny.userservice.dto.ClientByPhoneDto;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.ClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ClientController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientControllerTest {

    @MockBean
    private ClientService clientService;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";
    private final String MOBILE_PHONE = "+79999999999";

    private MockMvc mockMvc;
    private ClientByPhoneDto clientByPhoneDto;

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(new ClientController(clientService))
                .setControllerAdvice(ExceptionHandlerController.class).build();
        clientByPhoneDto = ClientByPhoneDto.builder()
                .clientId(CLIENT_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .middleName(MIDDLE_NAME)
                .build();
    }

    @Test
    @DisplayName("If client point correct phone, then return OK and client by phone dto")
    void getClientByPhone_ifClientFound_thenReturnOkAndReturnClientByPhoneDto() throws Exception {
        //Arrange
        when(clientService.getClientByPhone(MOBILE_PHONE)).thenReturn(clientByPhoneDto);
        //ACT
        MvcResult result = mockMvc.perform(get("/client")
                        .param("mobilePhone", MOBILE_PHONE))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(clientByPhoneDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If client point incorrect phone, then throw entity not found exception")
    void getClientByPhone_ifClientNotFound_thenReturnBadRequest() throws Exception {
        //Arrange
        when(clientService.getClientByPhone(MOBILE_PHONE)).thenThrow(EntityNotFoundException.class);
        // ACT & VERIFY
        mockMvc.perform(get("/client")
                        .param("mobilePhone", MOBILE_PHONE))
                .andExpect(status().isBadRequest());
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }
}

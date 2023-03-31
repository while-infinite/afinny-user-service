package by.afinny.userservice.unit.controller;

import by.afinny.userservice.controller.InformationController;
import by.afinny.userservice.dto.ResponseClientDataDto;
import by.afinny.userservice.exception.EntityNotFoundException;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.InformationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(InformationController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InformationControllerTest {

    @MockBean
    private InformationService informationService;

    private final UUID CLIENT_ID = UUID.fromString("e6376def-e541-4f03-aa0e-b7fc6fe4e1aa");

    private ResponseClientDataDto responseClientDataDto;
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        mockMvc = standaloneSetup(new InformationController(informationService))
                .setControllerAdvice(ExceptionHandlerController.class).build();

        responseClientDataDto = ResponseClientDataDto.builder()
                .firstName("Anton")
                .middleName("Viktorovich")
                .lastName("Gorohov")
                .mobilePhone("9237347017")
                .email("ngnipalm@vusra.com")
                .build();
    }

    @Test
    @DisplayName("If client exists then return client data")
    void getClientData_shouldReturnClient() throws Exception {
        //ARRANGE
        when(informationService.getClientData(any(UUID.class)))
                .thenReturn(responseClientDataDto);
        //ACT
        MvcResult result = mockMvc.perform(
                        get(InformationController.INFORMATION_URL)
                                .param(InformationController.CLIENT_ID_PARAMETER, String.valueOf(CLIENT_ID)))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(responseClientDataDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If client does not exist then return status BAD REQUEST")
    void getClientData_ifClientNotFound_thenReturnBadRequest() throws Exception {
        //ARRANGE
        when(informationService.getClientData(any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);
        //ACT & VERIFY
        mockMvc.perform(
                        get(InformationController.INFORMATION_URL)
                                .param(InformationController.CLIENT_ID_PARAMETER, String.valueOf(CLIENT_ID)))
                .andExpect(status().isBadRequest());
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}
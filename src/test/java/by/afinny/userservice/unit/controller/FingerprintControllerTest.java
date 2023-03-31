package by.afinny.userservice.unit.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import by.afinny.userservice.controller.FingerprintController;
import by.afinny.userservice.dto.FingerprintDto;
import by.afinny.userservice.exception.handler.ExceptionHandlerController;
import by.afinny.userservice.service.FingerprintService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(FingerprintController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FingerprintControllerTest {

    @MockBean
    private FingerprintService fingerprintService;

    private final UUID CLIENT_ID = UUID.randomUUID();

    private MockMvc mockMvc;
    private FingerprintDto requestFingerprintDto;

    @BeforeAll
    public void setUp() {
        mockMvc = standaloneSetup(new FingerprintController(fingerprintService))
                .setControllerAdvice(ExceptionHandlerController.class).build();
        requestFingerprintDto = FingerprintDto.builder()
                .fingerprint("fingerprint")
                .clientId(CLIENT_ID)
                .build();
    }

    @Test
    @DisplayName("If fingerprint successfully save then return client id and status OK")
    void createFingerprint_shouldSave() throws Exception {
        //ARRANGE
        ArgumentCaptor<FingerprintDto> fingerprintDtoArgumentCaptor = ArgumentCaptor.forClass(FingerprintDto.class);
        //ACT
        ResultActions perform = mockMvc.perform(post("/fingerprint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestFingerprintDto)));
        //VERIFY
        verify(fingerprintService, times(1)).createFingerprint(fingerprintDtoArgumentCaptor.capture());
        perform.andExpect(status().isOk());
        verifyFingerprint(fingerprintDtoArgumentCaptor.getValue());
    }

    private void verifyFingerprint(FingerprintDto fingerprintDto) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(fingerprintDto.getFingerprint()).isEqualTo(requestFingerprintDto.getFingerprint());
            softAssertions.assertThat(fingerprintDto.getClientId()).isEqualTo(requestFingerprintDto.getClientId());
        });
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
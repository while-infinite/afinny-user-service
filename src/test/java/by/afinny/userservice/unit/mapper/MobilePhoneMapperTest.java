package by.afinny.userservice.unit.mapper;

import by.afinny.userservice.dto.MobilePhoneDto;
import by.afinny.userservice.entity.Client;
import by.afinny.userservice.entity.PassportData;
import by.afinny.userservice.mapper.MobilePhoneMapperImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class MobilePhoneMapperTest {

    @InjectMocks
    MobilePhoneMapperImpl mobilePhoneMapper;

    private Client client;
    private MobilePhoneDto mobilePhoneDto;

    @BeforeAll
    void setUp() {
        client = Client.builder()
                .id(UUID.fromString("fa4729c2-ef3b-45a6-b5a1-e7763125518a"))
                .mobilePhone("9664561223")
                .passportData(
                        PassportData.builder()
                                .passportNumber("12345678").build())
                .build();
    }

    @Test
    @DisplayName("Verify notification dto fields setting")
    void toMobilePhoneDto_shouldReturnMobilePhoneDto() {
        //ACT
        mobilePhoneDto = mobilePhoneMapper.toMobilePhoneDto(client);
        //VERIFY
        assertSoftly(softAssertions -> softAssertions.assertThat(mobilePhoneDto.getMobilePhone()).isEqualTo(client.getMobilePhone()));
    }
}
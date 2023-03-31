package by.afinny.userservice.dto.deposit;

import by.afinny.userservice.dto.deposit.constant.CurrencyCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class AccountDto {

    private String accountNumber;
    private UUID clientId;
    private BigDecimal currentBalance;
    private LocalDate openDate;
    private LocalDate closeDate;
    private Boolean isActive;
    private String salaryProject;
    private CurrencyCode currencyCode;
}

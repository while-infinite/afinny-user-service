package by.afinny.userservice.dto.credit;

import by.afinny.userservice.dto.credit.constant.CreditType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class CreditDto {

    private String id;
    private CreditType type;
    private BigDecimal creditLimit;
    private String currencyCode;
    private BigDecimal interestRate;
    private Boolean personalGuarantees;
    private Short gracePeriodMonths;

    private String accountNumber;
    private BigDecimal principalDebt;
    private BigDecimal interestDebt;

    private String agreementId;
    private LocalDate agreementDate;
    private LocalDate terminationDate;
}

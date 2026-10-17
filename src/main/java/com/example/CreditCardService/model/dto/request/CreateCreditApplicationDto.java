package com.example.CreditCardService.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCreditApplicationDto {
    @NotNull(message = "Client ID required")
    private UUID clientId;

    private BigDecimal requestedCreditLimit;
    private Double currentDebtLoad;
    private Boolean hasCurrentDelinquency;
    private BigDecimal monthlyIncome;
    private String placeOfWork;
    private Integer currentJobTenureMonths;
    private String clientComment;
}

package com.example.CreditCardService.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCreditApplicationDto {


    @DecimalMin(value = "0.01", message = "Credit limit must be positive")
    private BigDecimal requestedCreditLimit;

    @Min(value = 0, message = "Current debt load can`t be negative")
    private Double currentDebtLoad;

    private Boolean hasCurrentDelinquency;

    @DecimalMin(value = "0.01", message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    private String placeOfWork;

    @Min(value = 0, message = "Job Tenure cant be negative")
    private Integer currentJobTenureMonths;

    private String clientComment;
}

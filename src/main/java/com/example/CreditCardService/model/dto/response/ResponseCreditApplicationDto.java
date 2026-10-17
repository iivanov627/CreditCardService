package com.example.CreditCardService.model.dto.response;

import com.example.CreditCardService.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCreditApplicationDto {
    private UUID id;
    private UUID clientId;

    private BigDecimal requestedCreditLimit;

    private BigDecimal monthlyIncome;

    private String placeOfWork;
    private Integer currentJobTenureMonths;
    private String clientComment;

    private Status applicationStatus;

    private LocalDate createDate;


    private LocalDateTime lastChange;

    private LocalDateTime submittedAt;
}

package com.example.CreditCardService.model.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SnapshotDto {
    private UUID scoringResultId;
    private Integer snapshotAge;
    private BigDecimal snapshotMonthlyIncome;
    private Integer snapshotJobTenureMonths;
    private Double snapshotCurrentDebtLoad;
    private Boolean snapshotHasCurrentDelinquency;
    private BigDecimal snapshotRequestedCreditLimit;
}

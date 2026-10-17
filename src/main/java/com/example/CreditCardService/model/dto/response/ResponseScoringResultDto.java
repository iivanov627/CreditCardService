package com.example.CreditCardService.model.dto.response;

import com.example.CreditCardService.model.enums.ReasonCode;
import com.example.CreditCardService.model.enums.Status;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResponseScoringResultDto {
    private UUID id;
    private UUID applicationId;
    private Integer score;
    private List<ReasonCode> reasonCodes;
    private Status decision;
    private BigDecimal recommendedLimit;
}

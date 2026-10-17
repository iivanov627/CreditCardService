package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.enums.ReasonCode;

public record RuleEvaluation(Integer score, ReasonCode reasonCode) {
}

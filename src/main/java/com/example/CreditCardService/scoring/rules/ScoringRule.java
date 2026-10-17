package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.CreditApplication;

public interface ScoringRule {
    RuleEvaluation evaluate(CreditApplication application);
}

package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.ReasonCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class IncomeRule implements ScoringRule {
    private BigDecimal minIncome = BigDecimal.valueOf(100000);

    @Override
    public RuleEvaluation evaluate(CreditApplication application) {
        if (application.getMonthlyIncome().compareTo(minIncome) < 0) {
            return new RuleEvaluation(-100, ReasonCode.LOW_INCOME);
        }
        return new RuleEvaluation(200, ReasonCode.HIGH_INCOME);
    }
}

package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.ReasonCode;
import org.springframework.stereotype.Component;


@Component
public class JobTenureMonthsRules implements ScoringRule {
    @Override
    public RuleEvaluation evaluate(CreditApplication application) {
        if (application.getCurrentJobTenureMonths() < 24) {
            return new RuleEvaluation(0, ReasonCode.INSUFFICIENT_EMPLOYMENT_HISTORY);
        }
        return new RuleEvaluation(200, ReasonCode.SUFFICIENT_EMPLOYMENT_HISTORY);
    }
}

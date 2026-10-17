package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.ReasonCode;
import org.springframework.stereotype.Component;


@Component
public class DelinquencyRule implements ScoringRule {
    @Override
    public RuleEvaluation evaluate(CreditApplication application) {
        if (application.getHasCurrentDelinquency()) {
            return new RuleEvaluation(0, ReasonCode.CURRENT_OVERDUE);
        }
        return new RuleEvaluation(200, ReasonCode.NO_OVERDUE);
    }
}

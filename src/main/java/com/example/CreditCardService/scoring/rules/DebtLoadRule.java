package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.ReasonCode;
import org.springframework.stereotype.Component;


@Component
public class DebtLoadRule implements ScoringRule {
    @Override
    public RuleEvaluation evaluate(CreditApplication application) {
        Double debtLoad = application.getCurrentDebtLoad();

        if (debtLoad > 0.5) {
            return new RuleEvaluation(-300, ReasonCode.CRITICAL_DEBT_LOAD);
        }
        if (debtLoad > 0.44) {
            return new RuleEvaluation(-50, ReasonCode.HIGH_DEBT_LOAD);
        }
        if (debtLoad > 0.36) {
            return new RuleEvaluation(50, ReasonCode.ACCEPTABLE_DEBT_LOAD);
        }
        return new RuleEvaluation(200, ReasonCode.EXCELLENT_DEBT_LOAD);
    }
}

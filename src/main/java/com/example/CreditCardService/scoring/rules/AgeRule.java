package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.ReasonCode;
import org.springframework.stereotype.Component;

@Component
public class AgeRule implements ScoringRule {

    @Override
    public RuleEvaluation evaluate(CreditApplication application) {
        int age = application.getClient().getAge();

        if (age < 22 || age > 65) {
            return new RuleEvaluation(0, ReasonCode.AGE_RESTRICTION);
        }

        return new RuleEvaluation(80, ReasonCode.PRIME_AGE);
    }
}

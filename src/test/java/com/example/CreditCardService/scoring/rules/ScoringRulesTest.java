package com.example.CreditCardService.scoring.rules;

import com.example.CreditCardService.model.entity.Client;
import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.ReasonCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoringRulesTest {
    private final AgeRule ageRule = new AgeRule();
    private final DebtLoadRule debtLoadRule = new DebtLoadRule();
    private final DelinquencyRule delinquencyRule = new DelinquencyRule();
    private final IncomeRule incomeRule = new IncomeRule();
    private final JobTenureMonthsRules jobTenureRule = new JobTenureMonthsRules();

    @Test
    void ageRuleHandlesEligibleAndRestrictedAges() {
        assertEquals(new RuleEvaluation(80, ReasonCode.PRIME_AGE), ageRule.evaluate(application(30)));
        assertEquals(new RuleEvaluation(0, ReasonCode.AGE_RESTRICTION), ageRule.evaluate(application(21)));
        assertEquals(new RuleEvaluation(0, ReasonCode.AGE_RESTRICTION), ageRule.evaluate(application(66)));
    }

    @Test
    void debtLoadRuleHandlesBoundaries() {
        assertEquals(new RuleEvaluation(200, ReasonCode.EXCELLENT_DEBT_LOAD),
                debtLoadRule.evaluate(application(30, 0.36, false, 100_000, 24)));
        assertEquals(new RuleEvaluation(50, ReasonCode.ACCEPTABLE_DEBT_LOAD),
                debtLoadRule.evaluate(application(30, 0.3601, false, 100_000, 24)));
        assertEquals(new RuleEvaluation(-50, ReasonCode.HIGH_DEBT_LOAD),
                debtLoadRule.evaluate(application(30, 0.45, false, 100_000, 24)));
        assertEquals(new RuleEvaluation(-300, ReasonCode.CRITICAL_DEBT_LOAD),
                debtLoadRule.evaluate(application(30, 0.5001, false, 100_000, 24)));
    }

    @Test
    void delinquencyRuleAwardsOnlyApplicationsWithoutCurrentOverdue() {
        assertEquals(new RuleEvaluation(0, ReasonCode.CURRENT_OVERDUE),
                delinquencyRule.evaluate(application(30, 0.2, true, 100_000, 24)));
        assertEquals(new RuleEvaluation(200, ReasonCode.NO_OVERDUE),
                delinquencyRule.evaluate(application(30, 0.2, false, 100_000, 24)));
    }

    @Test
    void incomeRuleHandlesThreshold() {
        assertEquals(new RuleEvaluation(-100, ReasonCode.LOW_INCOME),
                incomeRule.evaluate(application(30, 0.2, false, 99_999, 24)));
        assertEquals(new RuleEvaluation(200, ReasonCode.HIGH_INCOME),
                incomeRule.evaluate(application(30, 0.2, false, 100_000, 24)));
    }

    @Test
    void jobTenureRuleHandlesThreshold() {
        assertEquals(new RuleEvaluation(0, ReasonCode.INSUFFICIENT_EMPLOYMENT_HISTORY),
                jobTenureRule.evaluate(application(30, 0.2, false, 100_000, 23)));
        assertEquals(new RuleEvaluation(200, ReasonCode.SUFFICIENT_EMPLOYMENT_HISTORY),
                jobTenureRule.evaluate(application(30, 0.2, false, 100_000, 24)));
    }

    @Test
    void multiplePositiveAndNegativeRulesProduceExpectedTotals() {
        CreditApplication strong = application(30, 0.2, false, 120_000, 36);
        int strongScore = ageRule.evaluate(strong).score()
                + debtLoadRule.evaluate(strong).score()
                + delinquencyRule.evaluate(strong).score()
                + incomeRule.evaluate(strong).score()
                + jobTenureRule.evaluate(strong).score();
        assertEquals(880, strongScore);

        CreditApplication weak = application(30, 0.6, true, 50_000, 6);
        int weakScore = ageRule.evaluate(weak).score()
                + debtLoadRule.evaluate(weak).score()
                + delinquencyRule.evaluate(weak).score()
                + incomeRule.evaluate(weak).score()
                + jobTenureRule.evaluate(weak).score();
        assertEquals(-320, weakScore);
    }

    private CreditApplication application(int age) {
        return application(age, 0.2, false, 100_000, 24);
    }

    private CreditApplication application(
            int age,
            double debtLoad,
            boolean delinquent,
            long monthlyIncome,
            int jobTenureMonths
    ) {
        Client client = new Client();
        client.setBirthdate(LocalDate.now().minusYears(age));

        CreditApplication application = new CreditApplication();
        application.setClient(client);
        application.setCurrentDebtLoad(debtLoad);
        application.setHasCurrentDelinquency(delinquent);
        application.setMonthlyIncome(BigDecimal.valueOf(monthlyIncome));
        application.setCurrentJobTenureMonths(jobTenureMonths);
        application.setRequestedCreditLimit(BigDecimal.valueOf(300_000));
        return application;
    }
}

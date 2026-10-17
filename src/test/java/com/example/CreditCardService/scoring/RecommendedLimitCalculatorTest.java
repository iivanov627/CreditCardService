package com.example.CreditCardService.scoring;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.scoring.service.RecommendedLimitCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendedLimitCalculatorTest {
    private final RecommendedLimitCalculator calculator = new RecommendedLimitCalculator();

    @Test
    void rejectedScoreGetsZeroRecommendedLimit() {
        BigDecimal limit = calculator.calculateRecommendedLimit(application("500000", "100000", 0.2), 499);

        assertEquals(BigDecimal.ZERO, limit);
    }

    @Test
    void approvedLimitUsesRequestedLimitAndSolvencyCap() {
        BigDecimal limit = calculator.calculateRecommendedLimit(application("300000", "120000", 0.2), 800);

        assertEquals(0, new BigDecimal("300000").compareTo(limit));
        assertTrue(limit.compareTo(new BigDecimal("300000")) <= 0);
    }

    @Test
    void scoreCoefficientReducesLimitAndRoundsDownToThousands() {
        BigDecimal limit = calculator.calculateRecommendedLimit(application("500000", "100000", 0.5), 700);

        assertEquals(0, new BigDecimal("150000").compareTo(limit));
    }

    @Test
    void highDebtLoadCanReduceRecommendedLimitToZero() {
        BigDecimal limit = calculator.calculateRecommendedLimit(application("500000", "100000", 0.7), 800);

        assertEquals(0, BigDecimal.ZERO.compareTo(limit));
    }

    private CreditApplication application(String requestedLimit, String monthlyIncome, double debtLoad) {
        CreditApplication application = new CreditApplication();
        application.setRequestedCreditLimit(new BigDecimal(requestedLimit));
        application.setMonthlyIncome(new BigDecimal(monthlyIncome));
        application.setCurrentDebtLoad(debtLoad);
        return application;
    }
}

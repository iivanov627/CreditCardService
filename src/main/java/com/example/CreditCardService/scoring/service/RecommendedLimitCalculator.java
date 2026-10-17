package com.example.CreditCardService.scoring.service;

import com.example.CreditCardService.model.entity.CreditApplication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RecommendedLimitCalculator {


    public BigDecimal calculateRecommendedLimit(CreditApplication application, int score) {
        BigDecimal scoringCoefficient;
        if (score < 500) {
            return BigDecimal.ZERO;
        } else if (score < 650) {
            scoringCoefficient = new BigDecimal("0.50");
        } else if (score < 750) {
            scoringCoefficient = new BigDecimal("0.75");
        } else scoringCoefficient = BigDecimal.ONE;

        BigDecimal remainingDebtCapacity = new BigDecimal("0.60")
                .subtract(BigDecimal.valueOf(application.getCurrentDebtLoad()))
                .max(BigDecimal.ZERO);

        BigDecimal availablePayment = application.getMonthlyIncome().multiply(remainingDebtCapacity);
        BigDecimal solvencyLimit = availablePayment.divide(new BigDecimal("0.05"));

        BigDecimal baseLimit = application.getRequestedCreditLimit().min(solvencyLimit);

        return baseLimit.multiply(scoringCoefficient).setScale(-3, RoundingMode.DOWN);
    }

}

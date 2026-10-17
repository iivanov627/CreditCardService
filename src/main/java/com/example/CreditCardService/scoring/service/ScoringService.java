package com.example.CreditCardService.scoring.service;

import com.example.CreditCardService.dao.CreditApplicationRepository;
import com.example.CreditCardService.exception.CustomException.NotFoundException;
import com.example.CreditCardService.model.dto.request.SnapshotDto;
import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.entity.ScoringResult;
import com.example.CreditCardService.model.entity.SnapshotData;
import com.example.CreditCardService.model.enums.ReasonCode;
import com.example.CreditCardService.model.enums.Status;
import com.example.CreditCardService.scoring.rules.RuleEvaluation;
import com.example.CreditCardService.scoring.rules.ScoringRule;
import com.example.CreditCardService.service.ScoringResultService;
import com.example.CreditCardService.service.SnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ScoringService {

    private final List<ScoringRule> rules;
    private final SnapshotService snapshotService;
    private final CreditApplicationRepository creditApplicationRepository;
    private final ScoringResultService scoringResultService;
    private final RecommendedLimitCalculator recommendedLimitCalculator;

    @Autowired
    public ScoringService(List<ScoringRule> rules,
                          CreditApplicationRepository creditApplicationRepository,
                          SnapshotService snapshotService,
                          ScoringResultService scoringResultService,
                          RecommendedLimitCalculator recommendedLimitCalculator) {
        this.rules = rules;
        this.creditApplicationRepository = creditApplicationRepository;
        this.snapshotService = snapshotService;
        this.scoringResultService = scoringResultService;
        this.recommendedLimitCalculator = recommendedLimitCalculator;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> scoringJob(UUID applicationId) {

        log.debug("Start of scoring application {}", applicationId);

        //TODO убрать
        CreditApplication application = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));


        List<RuleEvaluation> evaluations = rules.stream()
                .map(rule -> rule.evaluate(application))
                .toList();

        int totalScore = evaluations.stream()
                .mapToInt(RuleEvaluation::score)
                .sum();

        List<ReasonCode> reasonCodes = evaluations.stream()
                .map(RuleEvaluation::reasonCode)
                .toList();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        Status status = totalScore < 500
                ? Status.REJECTED
                : Status.APPROVED;

        BigDecimal recommendedLimit = recommendedLimitCalculator
                .calculateRecommendedLimit(application, totalScore);

        application.setApplicationStatus(status);
        creditApplicationRepository.save(application);

        ScoringResult scoringResult = new ScoringResult();

        //TODO вынести в мапер или куда то
        scoringResult.setApplication(application);
        scoringResult.setScore(totalScore);
        scoringResult.setReasonCodes(reasonCodes);
        scoringResult.setRecommendedLimit(recommendedLimit);
        scoringResult.setDecision(status);

        scoringResult = scoringResultService.createScoringResult(scoringResult);


        //TODO вынести в мапер или куда то
        SnapshotDto snapshotDto = SnapshotDto.builder()
                .scoringResultId(scoringResult.getId())
                .snapshotAge(application.getClient().getAge())
                .snapshotMonthlyIncome(application.getMonthlyIncome())
                .snapshotJobTenureMonths(application.getCurrentJobTenureMonths())
                .snapshotCurrentDebtLoad(application.getCurrentDebtLoad())
                .snapshotHasCurrentDelinquency(application.getHasCurrentDelinquency())
                .snapshotRequestedCreditLimit(application.getRequestedCreditLimit())
                .build();

        SnapshotData data = snapshotService.createSnapshot(snapshotDto);

        scoringResult.setSnapshotData(data);

        scoringResultService.updateScoringResult(scoringResult.getId(), scoringResult);

        log.debug("End of scoring application {}, final score = {}, decision - {}",
                applicationId,
                totalScore,
                status);


        //TODO ничего не возвращать
        return CompletableFuture.completedFuture(null);

    }


}

package com.example.CreditCardService.service;

import com.example.CreditCardService.dao.ScoringResultRepository;
import com.example.CreditCardService.exception.CustomException.NotFoundException;
import com.example.CreditCardService.model.dto.response.ResponseScoringResultDto;
import com.example.CreditCardService.model.entity.ScoringResult;
import com.example.CreditCardService.model.entity.SnapshotData;
import com.example.CreditCardService.model.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ScoringResultService {
    private final ScoringResultRepository scoringResultRepository;

    @Autowired
    public ScoringResultService(ScoringResultRepository scoringResultRepository) {
        this.scoringResultRepository = scoringResultRepository;
    }

    public ScoringResult getByIdEntity(UUID id) {
        return scoringResultRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Scoring result not found"));
    }

    public ScoringResult getByApplicationId(UUID applicationId) {
        return scoringResultRepository.findByApplicationId(applicationId)
                .orElse(null);
    }


    public ResponseScoringResultDto getById(UUID id) {
        ScoringResult result = getByIdEntity(id);
        return ResponseScoringResultDto.builder()
                .id(result.getId())
                .applicationId(result.getApplication().getId())
                .score(result.getScore())
                .reasonCodes(result.getReasonCodes())
                .decision(result.getDecision())
                .recommendedLimit(result.getRecommendedLimit())
                .build();
    }

    public ScoringResult createScoringResult(ScoringResult scoringResult) {
        return scoringResultRepository.save(scoringResult);
    }

    public ScoringResult updateScoringResult(UUID id, ScoringResult scoringResult) {
        ScoringResult existing = getByIdEntity(id);

        if (scoringResult.getApplication() != null) {
            existing.setApplication(scoringResult.getApplication());
        }
        if (scoringResult.getScore() != null) {
            existing.setScore(scoringResult.getScore());
        }
        if (scoringResult.getReasonCodes() != null) {
            existing.setReasonCodes(scoringResult.getReasonCodes());
        }
        if (scoringResult.getDecision() != null) {
            existing.setDecision(scoringResult.getDecision());
        }
        if (scoringResult.getRecommendedLimit() != null) {
            existing.setRecommendedLimit(scoringResult.getRecommendedLimit());
        }
        if (scoringResult.getSnapshotData() != null) {
            updateSnapshotData(existing, scoringResult.getSnapshotData());
        }

        return scoringResultRepository.save(existing);

    }

    private void updateSnapshotData(ScoringResult scoringResult, SnapshotData update) {
        SnapshotData current = scoringResult.getSnapshotData();

        if (current == null) {
            update.setScoringResult(scoringResult);
            scoringResult.setSnapshotData(update);
            return;
        }

        if (update.getSnapshotAge() != null) {
            current.setSnapshotAge(update.getSnapshotAge());
        }
        if (update.getSnapshotMonthlyIncome() != null) {
            current.setSnapshotMonthlyIncome(update.getSnapshotMonthlyIncome());
        }
        if (update.getSnapshotJobTenureMonths() != null) {
            current.setSnapshotJobTenureMonths(update.getSnapshotJobTenureMonths());
        }
        if (update.getSnapshotCurrentDebtLoad() != null) {
            current.setSnapshotCurrentDebtLoad(update.getSnapshotCurrentDebtLoad());
        }
        if (update.getSnapshotHasCurrentDelinquency() != null) {
            current.setSnapshotHasCurrentDelinquency(update.getSnapshotHasCurrentDelinquency());
        }
        if (update.getSnapshotRequestedCreditLimit() != null) {
            current.setSnapshotRequestedCreditLimit(update.getSnapshotRequestedCreditLimit());
        }
    }


    //TODO то что не используется убрать
    public ScoringResult changeStatus(UUID id, Status status) {
        ScoringResult scoringResult = getByIdEntity(id);
        scoringResult.setDecision(status);
        return scoringResultRepository.save(scoringResult);
    }
}

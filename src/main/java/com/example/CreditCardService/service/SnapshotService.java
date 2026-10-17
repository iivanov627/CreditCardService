package com.example.CreditCardService.service;

import com.example.CreditCardService.dao.SnapshotRepository;
import com.example.CreditCardService.exception.CustomException.NotFoundException;
import com.example.CreditCardService.model.dto.request.SnapshotDto;
import com.example.CreditCardService.model.entity.SnapshotData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SnapshotService {
    private final SnapshotRepository snapshotRepository;
    private final ScoringResultService scoringResultService;

    @Autowired
    public SnapshotService(SnapshotRepository snapshotRepository, ScoringResultService scoringResultService) {
        this.snapshotRepository = snapshotRepository;
        this.scoringResultService = scoringResultService;
    }

    //TODO dto mb
    public SnapshotData getById(UUID id) {
        return snapshotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Snapshot not found"));
    }

    public SnapshotData createSnapshot(SnapshotDto data) {
        return snapshotRepository.save(SnapshotData.builder()
                .scoringResult(scoringResultService.getByIdEntity(data.getScoringResultId()))
                .snapshotAge(data.getSnapshotAge())
                .snapshotMonthlyIncome(data.getSnapshotMonthlyIncome())
                .snapshotJobTenureMonths(data.getSnapshotJobTenureMonths())
                .snapshotCurrentDebtLoad(data.getSnapshotCurrentDebtLoad())
                .snapshotHasCurrentDelinquency(data.getSnapshotHasCurrentDelinquency())
                .snapshotRequestedCreditLimit(data.getSnapshotRequestedCreditLimit())
                .build());
    }
}

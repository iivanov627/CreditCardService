package com.example.CreditCardService.dao;

import com.example.CreditCardService.model.entity.SnapshotData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SnapshotRepository extends JpaRepository<SnapshotData, UUID> {
    SnapshotData findByScoringResultId(UUID id);
}

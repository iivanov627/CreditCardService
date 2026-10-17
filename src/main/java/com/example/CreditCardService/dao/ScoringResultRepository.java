package com.example.CreditCardService.dao;

import com.example.CreditCardService.model.entity.ScoringResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoringResultRepository extends JpaRepository<ScoringResult, UUID> {
    Optional<ScoringResult> findByApplicationId(UUID id);
}

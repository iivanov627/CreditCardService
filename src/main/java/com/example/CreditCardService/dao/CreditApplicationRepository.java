package com.example.CreditCardService.dao;

import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditApplicationRepository extends JpaRepository<CreditApplication, UUID> {
    List<CreditApplication> findAllByClientId(UUID id);

    List<CreditApplication> findAllByClientIdAndApplicationStatus(UUID id, Status status);


}

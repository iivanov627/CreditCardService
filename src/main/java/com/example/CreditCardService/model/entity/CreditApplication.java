package com.example.CreditCardService.model.entity;


import com.example.CreditCardService.model.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table (name = "credit_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private BigDecimal requestedCreditLimit;
    private Double currentDebtLoad;
    private Boolean hasCurrentDelinquency;

    private BigDecimal monthlyIncome;

    private String placeOfWork;
    private Integer currentJobTenureMonths;
    private String clientComment;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Status applicationStatus;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate createDate;

    @UpdateTimestamp
    private LocalDateTime lastChange;

    private LocalDateTime submittedAt;

}

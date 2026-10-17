package com.example.CreditCardService.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "snapshot_data")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SnapshotData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scoring_result_id", nullable = false, unique = true)
    private ScoringResult scoringResult;


    @Column(name = "snapshot_age", nullable = false)
    private Integer snapshotAge;

    @Column(name = "snapshot_monthly_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal snapshotMonthlyIncome;

    @Column(name = "snapshot_job_tenure_months", nullable = false)
    private Integer snapshotJobTenureMonths;

    @Column(name = "snapshot_current_debt_load")
    private Double snapshotCurrentDebtLoad;

    @Column(name = "snapshot_has_current_delinquency")
    private Boolean snapshotHasCurrentDelinquency;

    @Column(name = "snapshot_requested_credit_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal snapshotRequestedCreditLimit;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

package com.example.CreditCardService.model.entity;


import com.example.CreditCardService.model.enums.ReasonCode;
import com.example.CreditCardService.model.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scoring_result")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScoringResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_application_id", nullable = false, unique = true)
    private CreditApplication application;

    @OneToOne(mappedBy = "scoringResult", cascade = CascadeType.ALL, optional = false)
    private SnapshotData snapshotData;

    @Column(nullable = false)
    private Integer score;

    @ElementCollection
    @CollectionTable(
            name = "scoring_result_reason_codes",
            joinColumns = @JoinColumn(name = "scoring_result_id", nullable = false)
    )
    @Column(name = "reason_code", nullable = false, length = 64)
    @Enumerated(EnumType.STRING)
    private List<ReasonCode> reasonCodes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status decision;

    @Column(name = "recommended_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal recommendedLimit;


}

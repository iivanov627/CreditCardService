package com.example.CreditCardService;

import com.example.CreditCardService.dao.ClientRepository;
import com.example.CreditCardService.dao.CreditApplicationRepository;
import com.example.CreditCardService.dao.ScoringResultRepository;
import com.example.CreditCardService.dao.SnapshotRepository;
import com.example.CreditCardService.exception.CustomException.BusinessException;
import com.example.CreditCardService.model.dto.request.AsyncResponseDto;
import com.example.CreditCardService.model.entity.Client;
import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.entity.ScoringResult;
import com.example.CreditCardService.model.entity.SnapshotData;
import com.example.CreditCardService.model.enums.AsyncStatus;
import com.example.CreditCardService.model.enums.Status;
import com.example.CreditCardService.service.CreditApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ScoringWorkflowIntegrationTest {
    @Autowired
    private CreditApplicationService applicationService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private CreditApplicationRepository applicationRepository;
    @Autowired
    private ScoringResultRepository scoringResultRepository;
    @Autowired
    private SnapshotRepository snapshotRepository;

    @BeforeEach
    void cleanDatabase() {
        snapshotRepository.deleteAll();
        scoringResultRepository.deleteAll();
        applicationRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void submittedApplicationCompletesAsApprovedAndPersistsResultAndSnapshot() throws Exception {
        CreditApplication application = saveApplication(
                Status.SUBMITTED, 30, "120000", 0.2, false, 36, "300000");

        AsyncResponseDto<Void> accepted = applicationService.startingScoring(application.getId());

        assertEquals(AsyncStatus.PROCESSING, accepted.getStatus());
        assertEquals(Status.SCORING,
                applicationRepository.findById(application.getId()).orElseThrow().getApplicationStatus());
        CreditApplication completed = awaitFinalStatus(application.getId());
        assertEquals(Status.APPROVED, completed.getApplicationStatus());

        ScoringResult result = scoringResultRepository.findByApplicationId(application.getId()).orElseThrow();
        assertEquals(880, result.getScore());
        assertEquals(Status.APPROVED, result.getDecision());
        assertEquals(new BigDecimal("300000.00"), result.getRecommendedLimit());
        assertTrue(applicationService.getScoringResult(application.getId())
                .getBody().getReasonCodes().size() >= 5);

        SnapshotData snapshot = snapshotRepository.findByScoringResultId(result.getId());
        assertNotNull(snapshot);
        assertEquals(30, snapshot.getSnapshotAge());
        assertEquals(new BigDecimal("120000.00"), snapshot.getSnapshotMonthlyIncome());
        assertEquals(36, snapshot.getSnapshotJobTenureMonths());
        assertEquals(0.2, snapshot.getSnapshotCurrentDebtLoad());
        assertEquals(false, snapshot.getSnapshotHasCurrentDelinquency());
        assertEquals(new BigDecimal("300000.00"), snapshot.getSnapshotRequestedCreditLimit());
    }

    @Test
    void weakApplicationCompletesAsRejected() throws Exception {
        CreditApplication application = saveApplication(
                Status.SUBMITTED, 30, "50000", 0.6, true, 6, "300000");

        applicationService.startingScoring(application.getId());

        CreditApplication completed = awaitFinalStatus(application.getId());
        assertEquals(Status.REJECTED, completed.getApplicationStatus());

        ScoringResult result = scoringResultRepository.findByApplicationId(application.getId()).orElseThrow();
        assertEquals(-320, result.getScore());
        assertEquals(Status.REJECTED, result.getDecision());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getRecommendedLimit());
        assertTrue(applicationService.getScoringResult(application.getId())
                .getBody().getReasonCodes().containsAll(java.util.List.of(
                com.example.CreditCardService.model.enums.ReasonCode.CRITICAL_DEBT_LOAD,
                com.example.CreditCardService.model.enums.ReasonCode.CURRENT_OVERDUE,
                com.example.CreditCardService.model.enums.ReasonCode.LOW_INCOME
        )));
    }

    @Test
    void draftCannotStartScoring() {
        CreditApplication application = saveApplication(
                Status.DRAFT, 30, "120000", 0.2, false, 36, "300000");

        assertThrows(BusinessException.class,
                () -> applicationService.startingScoring(application.getId()));

        assertTrue(scoringResultRepository.findByApplicationId(application.getId()).isEmpty());
        assertEquals(Status.DRAFT,
                applicationRepository.findById(application.getId()).orElseThrow().getApplicationStatus());
    }

    @Test
    void completedApplicationCannotBeScoredTwice() throws Exception {
        CreditApplication application = saveApplication(
                Status.SUBMITTED, 30, "120000", 0.2, false, 36, "300000");

        applicationService.startingScoring(application.getId());
        awaitFinalStatus(application.getId());

        assertThrows(BusinessException.class,
                () -> applicationService.startingScoring(application.getId()));
        assertEquals(1, scoringResultRepository.findAll().size());
    }

    @Test
    void asyncFailureIsStoredAndReturnedByResultEndpoint() throws Exception {
        // Missing required scoring input makes DebtLoadRule throw inside the background job.
        CreditApplication application = saveApplication(
                Status.SUBMITTED, 30, "120000", null, false, 36, "300000");

        applicationService.startingScoring(application.getId());

        CreditApplication failed = awaitFinalStatus(application.getId());
        assertEquals(Status.FAILED, failed.getApplicationStatus());
        assertTrue(scoringResultRepository.findByApplicationId(application.getId()).isEmpty());
        assertEquals(AsyncStatus.FAILED, applicationService.getScoringResult(application.getId()).getStatus());
    }

    private CreditApplication awaitFinalStatus(UUID id) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (System.nanoTime() < deadline) {
            CreditApplication application = applicationRepository.findById(id).orElseThrow();
            Status status = application.getApplicationStatus();
            if (status == Status.APPROVED || status == Status.REJECTED || status == Status.FAILED) {
                return application;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Scoring did not complete within 10 seconds for application " + id);
    }

    private CreditApplication saveApplication(
            Status status,
            int age,
            String income,
            Double debtLoad,
            boolean delinquent,
            int tenureMonths,
            String requestedLimit
    ) {
        String unique = UUID.randomUUID().toString();
        Client client = new Client();
        client.setName("Test");
        client.setSurname("Client");
        client.setMiddleName("Integration");
        client.setBirthdate(LocalDate.now().minusYears(age));
        client.setPhoneNumber("+7(900)123 45 67");
        client.setEmail(unique + "@example.test");
        client.setSeries("1234");
        client.setPassportNumber(unique.substring(0, 6));
        client = clientRepository.saveAndFlush(client);

        CreditApplication application = new CreditApplication();
        application.setClient(client);
        application.setApplicationStatus(status);
        application.setRequestedCreditLimit(new BigDecimal(requestedLimit));
        application.setCurrentDebtLoad(debtLoad);
        application.setHasCurrentDelinquency(delinquent);
        application.setMonthlyIncome(new BigDecimal(income));
        application.setPlaceOfWork("Example employer");
        application.setCurrentJobTenureMonths(tenureMonths);
        return applicationRepository.saveAndFlush(application);
    }
}

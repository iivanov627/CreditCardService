package com.example.CreditCardService.service;

import com.example.CreditCardService.dao.CreditApplicationRepository;
import com.example.CreditCardService.exception.CustomException.BusinessException;
import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.enums.AsyncStatus;
import com.example.CreditCardService.model.enums.Status;
import com.example.CreditCardService.scoring.service.ScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditApplicationScoringServiceTest {
    @Mock
    private CreditApplicationRepository applicationRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private ScoringService scoringService;
    @Mock
    private ScoringResultService scoringResultService;

    private CreditApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CreditApplicationService(
                applicationRepository,
                clientService,
                scoringService,
                scoringResultService,
                new ModelMapper()
        );
    }

    @Test
    void startScoringClaimsSubmittedApplicationAndReturnsProcessing() {
        UUID id = UUID.randomUUID();
        CreditApplication application = submittedApplication(id);
        when(applicationRepository.findById(id)).thenReturn(java.util.Optional.of(application));
        when(scoringService.scoringJob(id)).thenReturn(new CompletableFuture<>());

        var response = service.startingScoring(id);

        assertEquals(id, response.getId());
        assertEquals(AsyncStatus.PROCESSING, response.getStatus());
        assertEquals(Status.SCORING, application.getApplicationStatus());
        verify(applicationRepository).save(application);
        verify(scoringService).scoringJob(id);
    }

    @Test
    void nonSubmittedApplicationCannotStartScoring() {
        UUID id = UUID.randomUUID();
        CreditApplication application = submittedApplication(id);
        application.setApplicationStatus(Status.DRAFT);
        when(applicationRepository.findById(id)).thenReturn(java.util.Optional.of(application));

        assertThrows(BusinessException.class, () -> service.startingScoring(id));

        verify(scoringService, never()).scoringJob(id);
    }

    @Test
    void asyncFailureIsPersistedAsFailedStatus() {
        UUID id = UUID.randomUUID();
        CreditApplication application = submittedApplication(id);
        when(applicationRepository.findById(id)).thenReturn(java.util.Optional.of(application));
        when(scoringService.scoringJob(id))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("test failure")));

        service.startingScoring(id);

        assertEquals(Status.FAILED, application.getApplicationStatus());
        verify(applicationRepository, times(2)).save(application);
    }

    @Test
    void resultPollingReportsFailedInsteadOfProcessing() {
        UUID id = UUID.randomUUID();
        CreditApplication application = new CreditApplication();
        application.setId(id);
        application.setApplicationStatus(Status.FAILED);
        when(applicationRepository.findById(id)).thenReturn(java.util.Optional.of(application));
        when(scoringResultService.getByApplicationId(id)).thenReturn(null);

        var response = service.getScoringResult(id);

        assertEquals(AsyncStatus.FAILED, response.getStatus());
    }

    private CreditApplication submittedApplication(UUID id) {
        CreditApplication application = new CreditApplication();
        application.setId(id);
        application.setApplicationStatus(Status.SUBMITTED);
        return application;
    }
}

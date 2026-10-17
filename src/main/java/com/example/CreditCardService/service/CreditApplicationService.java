package com.example.CreditCardService.service;

import com.example.CreditCardService.dao.CreditApplicationRepository;
import com.example.CreditCardService.exception.CustomException.BusinessException;
import com.example.CreditCardService.exception.CustomException.NotFoundException;
import com.example.CreditCardService.model.dto.request.AsyncResponseDto;
import com.example.CreditCardService.model.dto.request.CreateCreditApplicationDto;
import com.example.CreditCardService.model.dto.request.UpdateCreditApplicationDto;
import com.example.CreditCardService.model.dto.response.ResponseCreditApplicationDto;
import com.example.CreditCardService.model.dto.response.ResponseScoringResultDto;
import com.example.CreditCardService.model.entity.Client;
import com.example.CreditCardService.model.entity.CreditApplication;
import com.example.CreditCardService.model.entity.ScoringResult;
import com.example.CreditCardService.model.enums.AsyncStatus;
import com.example.CreditCardService.model.enums.Status;
import com.example.CreditCardService.scoring.service.ScoringService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CreditApplicationService {
    private final CreditApplicationRepository applicationRepository;
    private final ClientService clientService;
    private final ScoringService scoringService;
    private final ScoringResultService scoringResultService;
    private final ModelMapper modelMapper;

    @Autowired
    public CreditApplicationService(CreditApplicationRepository applicationRepository,
                                    ClientService clientService,
                                    ScoringService scoringService,
                                    ScoringResultService scoringResultService,
                                    ModelMapper modelMapper) {
        this.applicationRepository = applicationRepository;
        this.clientService = clientService;
        this.scoringService = scoringService;
        this.scoringResultService = scoringResultService;
        this.modelMapper = modelMapper;
    }

    public ResponseCreditApplicationDto createApplication(CreateCreditApplicationDto dto) {
        Client client = clientService.getEntityById(dto.getClientId());

        int activeApplication = 0;
        activeApplication += applicationRepository
                .findAllByClientIdAndApplicationStatus(
                        dto.getClientId(),
                        Status.DRAFT
                ).size();

        activeApplication += applicationRepository
                .findAllByClientIdAndApplicationStatus(
                        dto.getClientId(),
                        Status.SUBMITTED
                ).size();

        activeApplication += applicationRepository
                .findAllByClientIdAndApplicationStatus(
                        dto.getClientId(),
                        Status.SCORING
                ).size();


        if (activeApplication > 0) {
            throw new BusinessException("max active applications = 1");
        }


        if (client.getAge() < 18) {
            throw new BusinessException("client must be over 18 years old");
        }

        CreditApplication application = new CreditApplication();

        //TODO mapper nado zaebalo
        application.setRequestedCreditLimit(dto.getRequestedCreditLimit());
        application.setCurrentDebtLoad(dto.getCurrentDebtLoad());
        application.setHasCurrentDelinquency(dto.getHasCurrentDelinquency());
        application.setMonthlyIncome(dto.getMonthlyIncome());
        application.setPlaceOfWork(dto.getPlaceOfWork());
        application.setCurrentJobTenureMonths(dto.getCurrentJobTenureMonths());
        application.setClientComment(dto.getClientComment());

        application.setClient(client);
        application.setApplicationStatus(Status.DRAFT);
        return modelMapper.map(applicationRepository.save(application), ResponseCreditApplicationDto.class);
    }

    public ResponseCreditApplicationDto getApplicationById(UUID id) {
        return toResponseDto(getApplicationEntityById(id));
    }

    public List<ResponseCreditApplicationDto> getClientApplications(UUID id, Status status) {
        List<CreditApplication> applications;
        if (status == null) {
            applications = applicationRepository.findAllByClientId(id);
        } else {
            applications = applicationRepository.findAllByClientIdAndApplicationStatus(id, status);
        }
        return applications.stream().map(this::toResponseDto).toList();
    }

    public ResponseCreditApplicationDto updateApplication(UUID id, UpdateCreditApplicationDto dto) {
        CreditApplication application = getApplicationEntityById(id);

        if (!application.getApplicationStatus().equals(Status.DRAFT)) {
            throw new BusinessException("Only draft applications can be changed");
        }

        if (dto.getRequestedCreditLimit() != null) {
            application.setRequestedCreditLimit(dto.getRequestedCreditLimit());
        }
        if (dto.getCurrentDebtLoad() != null) {
            application.setCurrentDebtLoad(dto.getCurrentDebtLoad());
        }
        if (dto.getHasCurrentDelinquency() != null) {
            application.setHasCurrentDelinquency(dto.getHasCurrentDelinquency());
        }
        if (dto.getMonthlyIncome() != null) {
            application.setMonthlyIncome(dto.getMonthlyIncome());
        }
        if (dto.getPlaceOfWork() != null) {
            application.setPlaceOfWork(dto.getPlaceOfWork());
        }
        if (dto.getCurrentJobTenureMonths() != null) {
            application.setCurrentJobTenureMonths(dto.getCurrentJobTenureMonths());
        }
        if (dto.getClientComment() != null) {
            application.setClientComment(dto.getClientComment());
        }

        return toResponseDto(applicationRepository.save(application));
    }

    public ResponseCreditApplicationDto submitApplication(UUID id) {
        CreditApplication application = getApplicationEntityById(id);

        validateForSubmission(application);

        if (!application.getApplicationStatus().equals(Status.DRAFT)) {
            throw new BusinessException("Only draft applications can be submit");
        }

        application.setApplicationStatus(Status.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());
        return toResponseDto(applicationRepository.save(application));
    }

    public ResponseCreditApplicationDto cancelApplication(UUID id) {
        CreditApplication application = getApplicationEntityById(id);

        if (!application.getApplicationStatus().equals(Status.DRAFT)
                && !application.getApplicationStatus().equals(Status.SUBMITTED)) {
            throw new BusinessException("Cant cancel this application");
        }

        application.setApplicationStatus(Status.CANCELLED);
        return toResponseDto(applicationRepository.save(application));
    }

    public AsyncResponseDto<Void> startingScoring(UUID id) {
        CreditApplication application = getApplicationEntityById(id);
        if (application.getApplicationStatus() != Status.SUBMITTED) {
            throw new BusinessException("Only submitted applications can be scored");
        }

        application.setApplicationStatus(Status.SCORING);
        applicationRepository.save(application);


        //TODO убрать пусть статус меняется врнутри скоринга, он возвращает войд

        //TODO передовать не айди а саму заявку
        scoringService.scoringJob(id)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        application.setApplicationStatus(Status.FAILED);
                        applicationRepository.save(application);
                        log.error("Scoring application {} ended with error", id, error);
                    }
                });

        return AsyncResponseDto.<Void>builder()
                .id(id)
                .status(AsyncStatus.PROCESSING)
                .build();

    }

    @Transactional(readOnly = true)
    public AsyncResponseDto<ResponseScoringResultDto> getScoringResult(UUID id) {
        CreditApplication application = getApplicationEntityById(id);
        ScoringResult result = scoringResultService.getByApplicationId(id);
        if (result == null) {
            AsyncStatus asyncStatus;

            //TODO вынести в мапер или куда то
            if (application.getApplicationStatus() == Status.SCORING) {
                asyncStatus = AsyncStatus.PROCESSING;
            } else if (application.getApplicationStatus() == Status.FAILED) {
                asyncStatus = AsyncStatus.FAILED;
            } else
                asyncStatus = AsyncStatus.NOT_STARTED;

            return AsyncResponseDto.<ResponseScoringResultDto>builder()
                    .id(id)
                    .status(asyncStatus)
                    .body(null)
                    .build();
        }

        //TODO вынести в мапер или куда то
        ResponseScoringResultDto dto = ResponseScoringResultDto.builder()
                .id(result.getId())
                .applicationId(id)
                .score(result.getScore())
                .reasonCodes(List.copyOf(result.getReasonCodes()))
                .decision(result.getDecision())
                .recommendedLimit(result.getRecommendedLimit())
                .build();

        return AsyncResponseDto.<ResponseScoringResultDto>builder()
                .id(id)
                .status(AsyncStatus.COMPLETED)
                .body(dto)
                .build();
    }


    public CreditApplication getApplicationEntityById(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application with this id not found"));
    }


    //TODO то что не используется убрать
    public CreditApplication changeStatus(UUID id, Status status) {
        CreditApplication application = getApplicationEntityById(id);
        application.setApplicationStatus(status);
        return applicationRepository.save(application);
    }

    private ResponseCreditApplicationDto toResponseDto(CreditApplication application) {
        ResponseCreditApplicationDto response = modelMapper.map(application, ResponseCreditApplicationDto.class);
        response.setClientId(application.getClient().getId());
        return response;
    }


    //TODO rework this
    private void validateForSubmission(CreditApplication application) {

        if (application.getRequestedCreditLimit() == null) {
            throw new BusinessException("Requested credit is required");
        }

        if (application.getRequestedCreditLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Requested credit must be positive");
        }

        if (application.getCurrentDebtLoad() == null) {
            throw new BusinessException("Current debt load is required");
        }

        if (application.getCurrentDebtLoad() < 0) {
            throw new BusinessException("Current debt load can`t be negative");
        }

        if (application.getHasCurrentDelinquency() == null) {
            throw new BusinessException("Current Delinquency is required");
        }

        if (application.getMonthlyIncome() == null) {
            throw new BusinessException("Monthly income is required");
        }

        if (application.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Monthly income must be over 0");
        }

        if (application.getPlaceOfWork() == null ||
                application.getPlaceOfWork().isBlank()) {
            throw new BusinessException("Place of work is required");
        }

        if (application.getCurrentJobTenureMonths() == null) {
            throw new BusinessException("Job tenure is required");
        }

        if (application.getCurrentJobTenureMonths() < 0) {
            throw new BusinessException("Job tenure must be over 0");
        }
    }

}

package com.example.CreditCardService.controller;


import com.example.CreditCardService.model.dto.request.AsyncResponseDto;
import com.example.CreditCardService.model.dto.request.CreateCreditApplicationDto;
import com.example.CreditCardService.model.dto.response.ResponseCreditApplicationDto;
import com.example.CreditCardService.model.dto.request.UpdateCreditApplicationDto;
import com.example.CreditCardService.model.dto.response.ResponseScoringResultDto;
import com.example.CreditCardService.model.enums.Status;
import com.example.CreditCardService.service.CreditApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CreditApplicationController {
    private final CreditApplicationService applicationService;

    @Autowired
    public CreditApplicationController(CreditApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/applications")
    public ResponseEntity<ResponseCreditApplicationDto> createApplication(
            @Valid @RequestBody CreateCreditApplicationDto dto
    ) {
        ResponseCreditApplicationDto creditApplication = applicationService.createApplication(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creditApplication);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ResponseCreditApplicationDto> getApplication(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                applicationService
                        .getApplicationById(id));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ResponseCreditApplicationDto>> getClientApplications(
            @RequestParam UUID clientId,
            @RequestParam(required = false) Status status
    ) {
        return ResponseEntity.ok(
                applicationService.getClientApplications(clientId, status)
        );
    }

    @PatchMapping("/applications/{id}")
    public ResponseEntity<ResponseCreditApplicationDto> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCreditApplicationDto dto
    ) {
        ResponseCreditApplicationDto application = applicationService.updateApplication(id, dto);
        return ResponseEntity.ok(application);
    }

    @PostMapping("/applications/{id}/submit")
    public ResponseEntity<ResponseCreditApplicationDto> submitApplication(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(applicationService.submitApplication(id));
    }

    @PostMapping("/applications/{id}/cancel")
    public ResponseEntity<ResponseCreditApplicationDto> cancelApplication(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(applicationService.cancelApplication(id));
    }

    @PostMapping("/applications/{id}/scoring")
    public ResponseEntity<AsyncResponseDto<Void>> startScoringApplication(
            @PathVariable UUID id
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(applicationService.startingScoring(id));
    }

    @GetMapping("/applications/{id}/scoring")
    public ResponseEntity<AsyncResponseDto<ResponseScoringResultDto>> getResultOfScoring(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(applicationService.getScoringResult(id));
    }
}

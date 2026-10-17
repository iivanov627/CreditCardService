package com.example.CreditCardService.controller;

import com.example.CreditCardService.model.dto.request.AsyncResponseDto;
import com.example.CreditCardService.model.enums.AsyncStatus;
import com.example.CreditCardService.service.CreditApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditApplicationControllerTest {
    @Mock
    private CreditApplicationService applicationService;

    private CreditApplicationController controller;

    @BeforeEach
    void setUp() {
        controller = new CreditApplicationController(applicationService);
    }

    @Test
    void startingScoringReturnsAccepted() {
        UUID id = UUID.randomUUID();
        AsyncResponseDto<Void> response = AsyncResponseDto.<Void>builder()
                .id(id)
                .status(AsyncStatus.PROCESSING)
                .build();
        when(applicationService.startingScoring(id)).thenReturn(response);

        var httpResponse = controller.startScoringApplication(id);

        assertEquals(HttpStatus.ACCEPTED, httpResponse.getStatusCode());
        assertEquals(response, httpResponse.getBody());
        verify(applicationService).startingScoring(id);
    }
}

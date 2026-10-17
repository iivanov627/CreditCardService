package com.example.CreditCardService.exception.handler.response;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> errors,
        String path
) {
}

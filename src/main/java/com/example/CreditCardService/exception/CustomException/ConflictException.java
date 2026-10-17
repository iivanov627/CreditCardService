package com.example.CreditCardService.exception.CustomException;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

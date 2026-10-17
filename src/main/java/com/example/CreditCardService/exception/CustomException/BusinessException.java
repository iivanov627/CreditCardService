package com.example.CreditCardService.exception.CustomException;

public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }
}

package com.example.CreditCardService.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseClientDto {
    private UUID id;

    private String name;
    private String surname;
    private String middleName;

    private LocalDate birthdate;

    private String phoneNumber;

    private String email;

    private String series;

    private String passportNumber;

    private LocalDate createDate;
}

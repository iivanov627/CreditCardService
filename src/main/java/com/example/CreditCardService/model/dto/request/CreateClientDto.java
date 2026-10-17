package com.example.CreditCardService.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientDto {

    @NotBlank(message = "Name required")
    private String name;

    @NotBlank(message = "Surname required")
    private String surname;

    @NotBlank(message = "Middle name required")
    private String middleName;

    @NotNull(message = "Birthdate required")
    private LocalDate birthdate;

    @NotBlank(message = "Phone number required")
    @Pattern(regexp = "\\+7\\(\\d{3}\\)\\d{3} \\d{2} \\d{2}",
            message = "Неверный формат телефона")
    private String phoneNumber;

    @NotBlank(message = "Email required")
    @Email(message = "Wrong format of email")
    private String email;

    @NotBlank(message = "Series required")
    @Pattern(regexp = "\\d{4}", message = "Series must contain 4 digits")
    private String series;

    @NotBlank(message = "Passport number required")
    @Pattern(regexp = "\\d{6}", message = "Passport number must contain 6 digits")
    private String passportNumber;
}

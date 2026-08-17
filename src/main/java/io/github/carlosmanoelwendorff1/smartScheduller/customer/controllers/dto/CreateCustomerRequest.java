package io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = "Name is required.") @Size(max = 150) String name,
        @Email(message = "Invalid email.") @Size(max = 150) String email,
        @Size(max = 30) String phone,
        @Size(max = 50) String document,
        @Past(message = "Birthday must be in the past.") LocalDate birthday) {
}
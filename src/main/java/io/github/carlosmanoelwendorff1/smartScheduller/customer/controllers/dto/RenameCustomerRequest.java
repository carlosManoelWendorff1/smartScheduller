package io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameCustomerRequest(
        @NotBlank(message = "Name is required.") @Size(max = 150) String name) {
}
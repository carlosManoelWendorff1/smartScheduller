package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters.") String password,
        @NotNull Role role) {
}
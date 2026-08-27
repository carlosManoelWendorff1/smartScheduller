// identity/controllers/dto/LoginRequest.java
package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String password) {
}
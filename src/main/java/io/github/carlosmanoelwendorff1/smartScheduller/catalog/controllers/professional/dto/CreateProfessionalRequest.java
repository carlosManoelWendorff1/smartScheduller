package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProfessionalRequest(@NotBlank @Size(max = 150) String name, UUID userId) {
}
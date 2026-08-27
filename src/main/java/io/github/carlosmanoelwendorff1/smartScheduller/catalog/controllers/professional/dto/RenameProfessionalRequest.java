package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameProfessionalRequest(@NotBlank @Size(max = 150) String name) {
}
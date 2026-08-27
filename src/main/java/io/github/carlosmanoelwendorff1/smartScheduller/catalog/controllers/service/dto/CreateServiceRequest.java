package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateServiceRequest(
                @NotBlank @Size(max = 150) String name,
                @Size(max = 500) String description,
                @Positive int durationMinutes,
                @NotNull @DecimalMin(value = "0.0") BigDecimal price) {
}
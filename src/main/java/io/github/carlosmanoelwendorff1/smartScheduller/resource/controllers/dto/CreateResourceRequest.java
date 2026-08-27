package io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateResourceRequest(@NotBlank @Size(max = 150) String name, @NotBlank @Size(max = 50) String type) {
}